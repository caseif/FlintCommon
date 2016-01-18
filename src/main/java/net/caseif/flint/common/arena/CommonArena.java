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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.common.event.internal.metadata.PersistableMetadataMutateEvent;
import net.caseif.flint.common.lobby.CommonLobbySign;
import net.caseif.flint.common.metadata.CommonMetadata;
import net.caseif.flint.common.metadata.persist.CommonPersistentMetadataHolder;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.common.util.helper.rollback.CommonRollbackHelper;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

    protected CommonRollbackHelper rbHelper;

    private final CommonMinigame parent;
    private final String id;
    private String name;
    private final String world;
    private final HashMap<Integer, Location3D> spawns = new HashMap<>();
    private final List<Location3D> shuffledSpawns;
    private final HashMap<Location3D, LobbySign> lobbies = new HashMap<>();

    private Boundary boundary;

    private boolean orphan = false;

    public CommonArena(CommonMinigame parent, String id, String name, Location3D initialSpawn, Boundary boundary)
            throws IllegalArgumentException {
        assert parent != null;
        assert id != null;
        assert name != null;
        checkNotNull(initialSpawn, "Initial spawn for arena \"" + id + "\" must not be null");
        checkArgument(initialSpawn.getWorld().isPresent(),
                "Initial spawn for arena \"" + id + "\" must have world");
        checkArgument(boundary.contains(initialSpawn), "Spawn point must be within arena boundary");

        if (!boundary.getLowerBound().getWorld().isPresent() && !boundary.getUpperBound().getWorld().isPresent()) {
            Location3D newLower = new Location3D(
                    initialSpawn.getWorld().get(),
                    boundary.getLowerBound().getX(),
                    boundary.getLowerBound().getY(),
                    boundary.getLowerBound().getZ()
            );
            boundary = new Boundary(newLower, boundary.getUpperBound());
        }

        this.parent = parent;
        this.id = id;
        this.name = name;
        this.world = initialSpawn.getWorld().get();
        this.spawns.put(0, initialSpawn);
        this.shuffledSpawns = Lists.newArrayList(initialSpawn);
        this.boundary = boundary;
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
            getRollbackHelper().popRollbacks();
        } catch (IOException | SQLException ex) {
            throw new RuntimeException("Failed to rollback arena " + getName(), ex);
        }
    }

    /**
     * Gets the {@link CommonRollbackHelper} associated with this {@link CommonArena}.
     *
     * @return The {@link CommonRollbackHelper} associated with this
     *     {@link CommonArena}
     */
    public CommonRollbackHelper getRollbackHelper() {
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
     * Saves this {@link Arena} to persistent storage.
     *
     * @throws Exception If an {@link Exception} is thrown while saving to disk.
     */
    //TODO: possibly change this design to be more efficient
    public abstract void store() throws Exception;

}
