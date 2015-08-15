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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.event.internal.metadata.PersistableMetadataMutateEvent;
import net.caseif.flint.common.lobby.CommonLobbySign;
import net.caseif.flint.common.metadata.CommonMetadata;
import net.caseif.flint.common.metadata.persist.CommonPersistentMetadatable;
import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.exception.OrphanedObjectException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
public abstract class CommonArena extends CommonPersistentMetadatable implements Arena {

    protected CommonMinigame parent;
    protected String id;
    protected String name;
    protected String world;
    protected HashBiMap<Integer, Location3D> spawns = HashBiMap.create();
    protected HashMap<Location3D, LobbySign> lobbies = new HashMap<>();
    protected Boundary boundary = null;

    public CommonArena(CommonMinigame parent, String id, String name, Location3D initialSpawn, Boundary boundary)
            throws IllegalArgumentException {
        assert parent != null;
        assert id != null;
        assert name != null;
        if (initialSpawn == null) {
            throw new IllegalArgumentException("Initial spawn for arena \"" + id + "\" must not be null");
        }
        if (!initialSpawn.getWorld().isPresent()) {
            throw new IllegalArgumentException("Initial spawn for arena \"" + id + "\" must have world");
        }
        this.parent = parent;
        this.id = id;
        this.name = name;
        this.world = initialSpawn.getWorld().get();
        this.spawns.put(0, initialSpawn);
        this.boundary = boundary;
        CommonMetadata.getEventBus().register(this);
    }

    @Override
    public String getId() throws OrphanedObjectException {
        checkState();
        return id;
    }

    @Override
    public String getName() throws OrphanedObjectException {
        checkState();
        return name;
    }

    @Override
    public String getWorld() throws OrphanedObjectException {
        checkState();
        return world;
    }

    @Override
    public Boundary getBoundary() throws OrphanedObjectException {
        checkState();
        return boundary;
    }

    @Override
    public void setBoundary(Boundary bound) throws OrphanedObjectException {
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
    public ImmutableBiMap<Integer, Location3D> getSpawnPoints() throws OrphanedObjectException {
        checkState();
        return ImmutableBiMap.copyOf(spawns);
    }

    @Override
    public int addSpawnPoint(Location3D spawn) throws OrphanedObjectException {
        checkState();
        int id;
        for (id = 0; id <= spawns.size(); id++) {
            if (!spawns.containsKey(id)) {
                spawns.put(id, spawn);
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
    public void removeSpawnPoint(int index) throws OrphanedObjectException {
        checkState();
        spawns.remove(index);
        try {
            store();
        } catch (Exception ex) {
            ex.printStackTrace();
            CommonCore.logSevere("Failed to save arena with ID " + getId() + " to persistent storage");
        }
    }

    @Override
    public void removeSpawnPoint(Location3D location) throws OrphanedObjectException {
        checkState();
        for (Map.Entry<Integer, Location3D> e : spawns.entrySet()) {
            if (e.getValue().equals(location)) {
                removeSpawnPoint(e.getKey());
                return;
            }
        }
    }

    @Override
    public Optional<Round> getRound() throws OrphanedObjectException {
        checkState();
        return Optional.fromNullable(parent.getRoundMap().get(this));
    }

    @Override
    public Minigame getMinigame() throws OrphanedObjectException {
        checkState();
        return parent;
    }

    @Override
    public String getPlugin() throws OrphanedObjectException {
        checkState();
        return parent.getPlugin();
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
        sign.orphan();
    }

    /**
     * Checks the state of this object.
     *
     * @throws OrphanedObjectException If this object is orphaned
     */
    protected void checkState() throws OrphanedObjectException {
        if (parent == null) {
            throw new OrphanedObjectException(this);
        }
    }

    /**
     * Orphans this object.
     */
    public void orphan() {
        parent = null;
    }

    /**
     * Saves this {@link Arena} to persistent storage.
     *
     * @throws Exception If an {@link Exception} is thrown while saving to disk.
     */
    //TODO: possibly change this design to be more efficient
    public abstract void store() throws Exception;

}
