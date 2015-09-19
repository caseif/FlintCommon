/*
 * New BSD License (BSD-new)
 *
 * Copyright (c) 2015 Maxim Roncacé
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
public abstract class CommonArena extends CommonPersistentMetadataHolder implements Arena, CommonComponent<Minigame> {

    private final CommonMinigame parent;
    private final String id;
    private final String name;
    private final String world;
    private final HashMap<Integer, Location3D> spawns = new HashMap<>();
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
        this.parent = parent;
        this.id = id;
        this.name = name;
        this.world = initialSpawn.getWorld().get();
        this.spawns.put(0, initialSpawn);
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
        checkState();
        return name;
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
        if (!getBoundary().contains(spawn)) {
            throw new IllegalArgumentException("Spawn point must be within arena boundary");
        }
        int id;
        for (id = 0; id <= spawns.size(); id++) {
            if (!spawns.containsKey(id)) {
                spawns.put(id, new Location3D(world, spawn.getX(), spawn.getY(), spawn.getZ()));
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
        if (!spawns.containsKey(index)) {
            throw new IllegalArgumentException("Cannot remove spawn: none exists with given index");
        }
        spawns.remove(index);
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
        for (Map.Entry<Integer, Location3D> e : spawns.entrySet()) {
            if (e.getValue().equals(location)) {
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

    public HashMap<Location3D, LobbySign> getLobbySignMap() {
        return lobbies;
    }

    public HashMap<Integer, Location3D> getSpawnPointMap() {
        return spawns;
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
