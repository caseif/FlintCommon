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

package net.caseif.flint.common.arena;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.caseif.flint.common.util.helper.JsonSerializer.deserializeLocation;
import static net.caseif.flint.common.util.helper.JsonSerializer.serializeLocation;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.common.event.internal.metadata.PersistableMetadataMutateEvent;
import net.caseif.flint.common.lobby.CommonLobbySign;
import net.caseif.flint.common.metadata.CommonMetadata;
import net.caseif.flint.common.metadata.persist.CommonPersistentMetadataHolder;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.common.util.agent.rollback.IRollbackAgent;
import net.caseif.flint.common.util.file.CommonDataFiles;
import net.caseif.flint.common.util.helper.JsonHelper;
import net.caseif.flint.common.util.helper.JsonSerializer;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
public abstract class CommonArena extends CommonPersistentMetadataHolder implements Arena, CommonComponent<Minigame> {

    //TODO: move to designated class for constants
    public static final String PERSISTENCE_NAME_KEY = "name";
    public static final String PERSISTENCE_WORLD_KEY = "world";
    public static final String PERSISTENCE_SPAWNS_KEY = "spawns";
    public static final String PERSISTENCE_BOUNDS_UPPER_KEY = "bound.upper";
    public static final String PERSISTENCE_BOUNDS_LOWER_KEY = "bound.lower";
    public static final String PERSISTENCE_METADATA_KEY = "metadata";

    private IRollbackAgent rbHelper;

    private final CommonMinigame parent;
    private final String id;
    private String name;
    private final String world;
    private final HashMap<Integer, Location3D> spawns = new HashMap<>();
    private final List<Location3D> shuffledSpawns;
    private final HashMap<Location3D, LobbySign> lobbies = new HashMap<>();

    private Boundary boundary;

    private boolean orphan = false;

    protected CommonArena(CommonMinigame parent, String id, String name, Location3D[] spawnPoints, Boundary boundary)
            throws IllegalArgumentException {
        assert parent != null;
        assert id != null;
        assert name != null;
        checkArgument(spawnPoints != null && spawnPoints.length > 0,
                "Initial spawn for arena \"" + id + "\" must not be null or empty");
        String world = null;
        for (Location3D spawn : spawnPoints) {
            if (spawn.getWorld().isPresent()) {
                if (world != null) {
                    checkArgument(spawn.getWorld().get().equals(world), "Spawn points must not have different worlds");
                } else {
                    world = spawn.getWorld().get();
                }
            }
        }
        checkArgument(spawnPoints[0].getWorld().isPresent(),
                "At least one spawn point for arena \"" + id + "\" must define a world");
        for (Location3D spawn : spawnPoints) {
            checkArgument(boundary.contains(spawn), "Spawn points must be within arena boundary");
        }

        if (!boundary.getLowerBound().getWorld().isPresent() && !boundary.getUpperBound().getWorld().isPresent()) {
            Location3D newLower = new Location3D(
                    world,
                    boundary.getLowerBound().getX(),
                    boundary.getLowerBound().getY(),
                    boundary.getLowerBound().getZ()
            );
            boundary = new Boundary(newLower, boundary.getUpperBound());
        }

        this.parent = parent;
        this.id = id;
        this.name = name;
        this.world = world;
        for (int i = 0; i < spawnPoints.length; i++) {
            this.spawns.put(i, spawnPoints[i]);
        }
        this.shuffledSpawns = Lists.newArrayList(spawnPoints);
        Collections.shuffle(this.shuffledSpawns);
        this.boundary = boundary;

        this.rbHelper = CommonCore.getFactoryRegistry().getRollbackAgentFactory().createRollbackAgent(this);
        CommonMetadata.getEventBus().register(this);
    }

    @Override
    public Minigame getOwner() throws OrphanedComponentException {
        checkState();
        return parent;
    }

    @Override
    public Minigame getMinigame() throws OrphanedComponentException {
        return getOwner();
    }

    @Override
    public String getId() throws OrphanedComponentException {
        checkState();
        return id;
    }

    @Override
    public String getName() throws OrphanedComponentException {
        return getDisplayName();
    }

    @Override
    public String getDisplayName() throws OrphanedComponentException {
        checkState();
        return name;
    }

    @Override
    public void setDisplayName(String displayName) throws OrphanedComponentException {
        checkState();
        this.name = displayName;
    }

    @Override
    public String getWorld() throws OrphanedComponentException {
        checkState();
        return world;
    }

    @Override
    public Boundary getBoundary() throws OrphanedComponentException {
        checkState();
        return boundary;
    }

    @Override
    public void setBoundary(Boundary bound) throws OrphanedComponentException {
        checkState();
        this.boundary = bound;
        try {
            store();
        } catch (Exception ex) {
            ex.printStackTrace();
            CommonCore.logSevere("Failed to save arena with ID " + getId() + " to persistent storage");
        }
    }

    @Override
    public ImmutableMap<Integer, Location3D> getSpawnPoints() throws OrphanedComponentException {
        checkState();
        return ImmutableMap.copyOf(spawns);
    }

    @Override
    public int addSpawnPoint(Location3D spawn) throws OrphanedComponentException {
        checkState();
        checkArgument(getBoundary().contains(spawn), "Spawn point must be within arena boundary");

        int id;
        for (id = 0; id <= spawns.size(); id++) {
            if (!spawns.containsKey(id)) {
                Location3D spawnLoc = new Location3D(world, spawn.getX(), spawn.getY(), spawn.getZ());
                spawns.put(id, spawnLoc);
                shuffledSpawns.add(spawnLoc);
                Collections.shuffle(shuffledSpawns);
                try {
                    store();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    CommonCore.logSevere("Failed to save arena with ID " + getId() + " to persistent storage");
                }
                return id;
            }
        }
        // logically impossible in theory
        throw new AssertionError("Logic error: could not get next available spawn. Report this immediately.");
    }

    @Override
    public void removeSpawnPoint(int index) throws OrphanedComponentException {
        checkState();
        checkArgument(spawns.containsKey(index), "Cannot remove spawn: none exists with given index");

        Location3D removedSpawn = spawns.remove(index);

        shuffledSpawns.remove(removedSpawn);
        Collections.shuffle(shuffledSpawns);

        try {
            store();
        } catch (Exception ex) {
            ex.printStackTrace();
            CommonCore.logSevere("Failed to save arena with ID " + getId() + " to persistent storage");
        }
    }

    @Override
    public void removeSpawnPoint(Location3D location) throws OrphanedComponentException {
        checkState();
        checkArgument(!location.getWorld().isPresent() || location.getWorld().get().equals(world),
                "Cannot remove spawn: world mismatch in provided location");

        Location3D loc = new Location3D(world, location.getX(), location.getY(), location.getZ());

        for (Map.Entry<Integer, Location3D> e : spawns.entrySet()) {
            if (e.getValue().equals(loc)) {
                removeSpawnPoint(e.getKey());
                return;
            }
        }

        throw new IllegalArgumentException("Cannot remove spawn: none exists at given location");
    }

    @Override
    public Optional<Round> getRound() throws OrphanedComponentException {
        checkState();
        return Optional.fromNullable(parent.getRoundMap().get(this));
    }

    @Override
    public Round createRound() throws IllegalStateException, OrphanedComponentException {
        checkState();
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        ImmutableSet<LifecycleStage> stages = getMinigame().getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES);
        Preconditions.checkState(stages != null && !stages.isEmpty(),
                "Illegal call to nullary createRound method: default lifecycle stages are not set");
        return createRound(getMinigame().getConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES));
    }

    @Override
    public Round createRound(ImmutableSet<LifecycleStage> stages)
            throws IllegalArgumentException, IllegalStateException, OrphanedComponentException {
        checkState();
        Preconditions.checkState(!getRound().isPresent(), "Cannot create a round in an arena already hosting one");
        checkArgument(stages != null && !stages.isEmpty(), "LifecycleStage set must not be null or empty");
        ((CommonMinigame) getMinigame()).getRoundMap()
                .put(this, CommonCore.getFactoryRegistry().getRoundFactory().createRound(this, stages));
        Preconditions.checkState(getRound().isPresent(), "Cannot get created round from arena! This is a bug.");
        return getRound().get();
    }

    @Override
    public Round getOrCreateRound() {
        return getRound().isPresent() ? getRound().get() : createRound();
    }

    @Override
    public Round getOrCreateRound(ImmutableSet<LifecycleStage> stages) {
        return getRound().isPresent() ? getRound().get() : createRound(stages);
    }

    @Subscribe
    public void onMetadataMutate(PersistableMetadataMutateEvent event) {
        if (event.getMetadata() == getMetadata()) { // check whether event pertains to this arena's metadata
            try {
                store(); // re-store the arena
            } catch (Exception ex) {
                ex.printStackTrace();
                CommonCore.logSevere("Failed to save arena with ID " + getId() + " to persistent storage");
            }
        }
    }

    @Override
    public ImmutableList<LobbySign> getLobbySigns() {
        return ImmutableList.copyOf(lobbies.values());
    }

    @Override
    public Optional<LobbySign> getLobbySignAt(Location3D location) throws IllegalArgumentException {
        return Optional.fromNullable(lobbies.get(location));
    }

    @Override
    public void rollback() throws IllegalStateException, OrphanedComponentException {
        checkState();
        try {
            getRollbackAgent().popRollbacks();
        } catch (IOException | SQLException ex) {
            throw new RuntimeException("Failed to rollback arena " + getDisplayName(), ex);
        }
    }

    /**
     * Gets the {@link IRollbackAgent} associated with this {@link CommonArena}.
     *
     * @return The {@link IRollbackAgent} associated with this
     *     {@link CommonArena}
     */
    public IRollbackAgent getRollbackAgent() {
        return rbHelper;
    }

    public HashMap<Location3D, LobbySign> getLobbySignMap() {
        return lobbies;
    }

    public HashMap<Integer, Location3D> getSpawnPointMap() {
        return spawns;
    }

    public ImmutableList<Location3D> getShuffledSpawnPoints() throws OrphanedComponentException {
        checkState();
        return ImmutableList.copyOf(shuffledSpawns);
    }

    /**
     * Unregisters the {@link LobbySign} at the given
     * {@link Location3D location}.
     *
     * <p><em>This method is intended for internal use only.</em></p>
     *
     * @param location The {@link Location3D location} of the {@link LobbySign}
     *     to remove
     */
    public void unregisterLobbySign(Location3D location) {
        CommonLobbySign sign = (CommonLobbySign) lobbies.get(location);
        lobbies.remove(location);
        sign.unstore();
    }

    @Override
    public void checkState() throws OrphanedComponentException {
        if (orphan) {
            throw new OrphanedComponentException(this);
        }
    }

    @Override
    public void orphan() {
        CommonCore.orphan(this);
    }

    @Override
    public void setOrphanFlag() {
        this.orphan = true;
    }

    /**
     * Configures this {@link CommonArena} from the given {@link JsonObject}.
     *
     * @param json The {@link JsonObject} containing data for this
     *     {@link CommonArena}
     */
    public void configure(JsonObject json) {
        {
            JsonObject spawnSection = json.getAsJsonObject(PERSISTENCE_SPAWNS_KEY);
            for (Map.Entry<String, JsonElement> entry : spawnSection.entrySet()) {
                try {
                    int index = Integer.parseInt(entry.getKey());
                    getSpawnPointMap()
                            .put(index, deserializeLocation(spawnSection.getAsJsonObject(entry.getKey())));
                } catch (IllegalArgumentException ignored) {
                    CommonCore.logWarning("Invalid spawn at index " + entry.getKey() + " for arena \"" + getId()
                            + "\"");
                }
            }
        }

        if (json.has(PERSISTENCE_METADATA_KEY) && json.get(PERSISTENCE_METADATA_KEY).isJsonObject()) {
            JsonSerializer.deserializeMetadata(json.getAsJsonObject(PERSISTENCE_METADATA_KEY), getPersistentMetadata());
        }
    }

    /**
     * Stores this arena into persistent storage.
     *
     * @throws IOException If an exception occurs while writing to the
     *     persistent store
     */
    public void store() throws IOException {
        File arenaStore = CommonDataFiles.ARENA_STORE.getFile(getMinigame());

        JsonObject json = JsonHelper.readOrCreateJson(arenaStore);

        JsonObject jsonArena = new JsonObject();
        jsonArena.addProperty(PERSISTENCE_NAME_KEY, getDisplayName());
        jsonArena.addProperty(PERSISTENCE_WORLD_KEY, getWorld());

        JsonObject spawns = new JsonObject();
        for (Map.Entry<Integer, Location3D> entry : getSpawnPoints().entrySet()) {
            spawns.add(entry.getKey().toString(), serializeLocation(entry.getValue()));
        }
        jsonArena.add(PERSISTENCE_SPAWNS_KEY, spawns);

        jsonArena.add(PERSISTENCE_BOUNDS_UPPER_KEY, serializeLocation(getBoundary().getUpperBound()));
        jsonArena.add(PERSISTENCE_BOUNDS_LOWER_KEY, serializeLocation(getBoundary().getLowerBound()));

        JsonObject metadata = new JsonObject();
        JsonSerializer.serializeMetadata(metadata, getPersistentMetadata());
        jsonArena.add(PERSISTENCE_METADATA_KEY, metadata);

        json.add(this.getId(), jsonArena);

        try (FileWriter writer = new FileWriter(arenaStore)) {
            writer.write(json.toString());
        }
    }

    /**
     * Removes this arena from persistent storage.
     *
     * @throws IOException If an exception occurs while writing to the
     *     persistent store
     */
    public void removeFromStore() throws IOException {
        File arenaStore = CommonDataFiles.ARENA_STORE.getFile(getMinigame());

        if (!arenaStore.exists()) {
            throw new IllegalStateException("Arena store does not exist!");
        }

        JsonObject json = new JsonObject();
        try (FileReader reader = new FileReader(arenaStore)) {
            JsonElement el = new JsonParser().parse(reader);
            if (el.isJsonObject()) {
                json = el.getAsJsonObject();
            } else {
                CommonCore.logWarning("Root element of arena store is not object - overwriting");
                Files.delete(arenaStore.toPath());
            }
        }

        json.remove(this.getId());

        try (FileWriter writer = new FileWriter(arenaStore)) {
            writer.write(json.toString());
        }
    }

    public static class Builder implements Arena.Builder {

        private final Minigame mg;
        private String id;
        private String dispName;
        private Location3D[] spawnPoints;
        private Boundary boundary;

        public Builder(Minigame mg) {
            this.mg = mg;
        }

        @Override
        public Arena.Builder id(String id) {
            this.id = id;
            return this;
        }

        @Override
        public Arena.Builder displayName(String displayName) {
            this.dispName = displayName;
            return this;
        }

        @Override
        public Arena.Builder spawnPoints(Location3D... spawnPoints) {
            this.spawnPoints = spawnPoints;
            return this;
        }

        @Override
        public Arena.Builder boundary(Boundary boundary) {
            this.boundary = boundary;
            return this;
        }

        @Override
        public Arena build() throws IllegalStateException {
            Preconditions.checkState(id != null, "ID must be set before building");
            Preconditions.checkState(spawnPoints != null && spawnPoints.length > 0,
                    "Spawn points must be set before building");
            Preconditions.checkState(boundary != null, "Boundary must be set before building");
            return CommonCore.getFactoryRegistry().getArenaFactory().createArena((CommonMinigame) mg, id,
                    dispName != null ? dispName : id, spawnPoints, boundary);
        }

    }

}
