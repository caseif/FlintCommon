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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.event.FlintSubscriberExceptionHandler;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.Round;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

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
