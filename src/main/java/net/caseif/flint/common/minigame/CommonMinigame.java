/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.flint.common.minigame;

import static net.caseif.flint.common.util.helper.JsonSerializer.deserializeLocation;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.event.FlintSubscriberExceptionHandler;
import net.caseif.flint.common.util.file.CommonDataFiles;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implements {@link Minigame}.
 *
 * @author Max Roncacé
 */
public abstract class CommonMinigame implements Minigame {

    private EventBus eventBus;

    private final Map<ConfigNode<?>, Object> config = new HashMap<>();
    private final BiMap<String, Arena> arenas = HashBiMap.create();
    private final BiMap<Arena, Round> rounds = HashBiMap.create(); // guarantees values aren't duplicated

    protected CommonMinigame() {
        // this is more complicated than it could be in order to prevent the JVM
        // from attempting to load a class that may not exist at runtime
        boolean exceptionHandlerSupport = false;
        try {
            Class.forName("com.google.common.eventbus.SubscriberExceptionHandler");
            exceptionHandlerSupport = true;
        } catch (ClassNotFoundException ex) {
            CommonCore.logWarning("Guava version is < 16.0 - SubscriberExceptionHandler is not supported. "
                    + "Exceptions occurring in Flint event handlers may not be logged correctly.");
        }
        eventBus = exceptionHandlerSupport ? BreakingEventBusFactory.getBreakingEventBus() : new EventBus();
    }

    @Override
    @SuppressWarnings("unchecked") // only mutable through setConfigValue(), which guarantees types match
    public <T> T getConfigValue(ConfigNode<T> node) {
        return config.containsKey(node) ? (T) config.get(node) : node.getDefaultValue();
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public <T> void setConfigValue(ConfigNode<T> node, T value) {
        config.put(node, value);
    }

    @Override
    public ImmutableList<Arena> getArenas() {
        return ImmutableList.copyOf(arenas.values());
    }

    @Override
    public Optional<Arena> getArena(String arenaName) {
        return Optional.fromNullable(arenas.get(arenaName.toLowerCase()));
    }

    @Override
    public Arena createArena(String id, Location3D spawnPoint, Boundary boundary) throws IllegalArgumentException {
        return createArena(id, id, spawnPoint, boundary);
    }

    @Override
    public void removeArena(String id) throws IllegalArgumentException {
        id = id.toLowerCase();
        Arena arena = getArenaMap().get(id);
        if (arena != null) {
            removeArena(arena);
        } else {
            throw new IllegalArgumentException("Cannot find arena with ID " + id + " in minigame " + getPlugin());
        }
    }

    @Override
    public void removeArena(Arena arena) throws IllegalArgumentException {
        if (arena.getMinigame() != this) {
            throw new IllegalArgumentException("Cannot remove arena with different parent minigame");
        }
        if (arena.getRound().isPresent()) {
            arena.getRound().get().end();
            CommonCore.logVerbose("Minigame " + getPlugin() + " requested to remove arena " + arena.getId()
                    + " while it still contained a round. Steel will end it automatically, but typically this behavior "
                    + "is not ideal and the round should be ended before the arena is requested for removal.");
        }
        getArenaMap().remove(arena.getId());
        try {
            ((CommonArena) arena).removeFromStore();
        } catch (IOException ex) {
            CommonCore.logSevere("Failed to remove arena with ID " + arena.getId() + " from persistent store");
            ex.printStackTrace();
        }
        ((CommonArena) arena).orphan();
    }

    protected void loadArenas() {
        File arenaStore = CommonDataFiles.ARENA_STORE.getFile(this);
        if (!arenaStore.exists()) {
            return;
        }

        JsonObject json;
        try {
            try (FileReader reader = new FileReader(arenaStore)) {
                JsonElement el = new JsonParser().parse(reader);
                if (el.isJsonObject()) {
                    json = el.getAsJsonObject(); // BIG-ASS TODO
                } else {
                    System.out.println(el);
                    CommonCore.logWarning("Root of arena store is not object. Not reading arenas.");
                    return;
                }
            }

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (json.get(entry.getKey()).isJsonObject()) {
                    JsonObject arenaJson = json.getAsJsonObject(entry.getKey());
                    if (arenaJson.has(CommonArena.PERSISTENCE_NAME_KEY)
                            && arenaJson.has(CommonArena.PERSISTENCE_WORLD_KEY)) {
                        Location3D upperBound = deserializeLocation(
                                arenaJson.getAsJsonObject(CommonArena.PERSISTENCE_BOUNDS_UPPER_KEY)
                        );
                        Location3D lowerBound = deserializeLocation(
                                arenaJson.getAsJsonObject(CommonArena.PERSISTENCE_BOUNDS_LOWER_KEY)
                        );
                        CommonArena arena = CommonCore.getArenaFactory().createArena(
                                this,
                                entry.getKey().toLowerCase(),
                                arenaJson.get(CommonArena.PERSISTENCE_NAME_KEY).getAsString(),
                                new Location3D(arenaJson.get(CommonArena.PERSISTENCE_WORLD_KEY).getAsString(),
                                        lowerBound.getX(), lowerBound.getY(), lowerBound.getZ()),
                                new Boundary(
                                        upperBound,
                                        lowerBound
                                )
                        );
                        arena.getSpawnPointMap().remove(0); // remove initial placeholder spawn
                        arena.configure(arenaJson);
                        getArenaMap().put(arena.getId(), arena);
                    } else {
                        CommonCore.logWarning("Invalid object \"" + entry.getKey() + "\"in arena store");
                    }
                } else {
                    CommonCore.logWarning("Found non-object for key \"" + entry.getKey() + "\" - not loading");
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load existing arenas from disk", ex);
        }
    }


    @Override
    public ImmutableList<Round> getRounds() {
        return ImmutableList.copyOf(rounds.values());
    }

    @Override
    public ImmutableList<Challenger> getChallengers() {
        ImmutableList.Builder<Challenger> builder = ImmutableList.builder();
        for (Round r : getRounds()) { // >tfw no streams
            builder.addAll(r.getChallengers());
        }
        return builder.build();
    }

    @Override
    public Optional<Challenger> getChallenger(UUID uuid) {
        for (Round r : getRounds()) {
            if (r.getChallenger(uuid).isPresent()) {
                return r.getChallenger(uuid);
            }
        }
        return Optional.absent();
    }

    // everything below this line is (are?) internal utility methods

    public Map<ConfigNode<?>, Object> getConfigMap() {
        return config;
    }

    public Map<String, Arena> getArenaMap() {
        return arenas;
    }

    public Map<Arena, Round> getRoundMap() {
        return rounds;
    }

    /**
     * Factory for {@link EventBus}es which would otherwise break the plugin if
     * unsupported.
     *
     * <p>Keeping the code in this class discrete from everything else ensures that
     * the JVM doesn't attempt to load classes which are not available on the
     * current platform.</p>
     *
     * @author Max Roncacé
     */
    private static class BreakingEventBusFactory {

        /**
         * Constructs and returns a new breaking {@link EventBus}.
         *
         * @return A new breaking {@link EventBus}
         */
        private static EventBus getBreakingEventBus() {
            return new EventBus(FlintSubscriberExceptionHandler.getInstance());
        }

    }

}
