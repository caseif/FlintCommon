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
package net.caseif.flint.common;

import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.common.util.CommonMetadatable;

import com.google.common.base.Optional;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import net.caseif.flint.Arena;
import net.caseif.flint.Minigame;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import java.util.Map;

/**
 * Implements {@link Arena}.
 *
 * @author Max Roncacé
 */
//TODO: no persistance yet, need to work on that
public abstract class CommonArena extends CommonMetadatable implements Arena {

    protected CommonMinigame parent;
    protected String id;
    protected String name;
    protected String world;
    protected HashBiMap<Integer, Location3D> spawns = HashBiMap.create();
    protected Boundary boundary = null;

    public CommonArena(CommonMinigame parent, String id, String name, Location3D initialSpawn) {
        this.parent = parent;
        this.id = id;
        this.name = name;
        this.world = initialSpawn.getWorld().get();
        this.spawns.put(0, initialSpawn);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public Optional<Boundary> getBoundary() {
        return Optional.fromNullable(boundary);
    }

    @Override
    public void setBoundary(Boundary bound) {
        this.boundary = bound;
    }

    @Override
    public ImmutableBiMap<Integer, Location3D> getSpawnPoints() {
        return ImmutableBiMap.copyOf(spawns);
    }

    @Override
    public int addSpawnPoint(Location3D spawn) {
        int id;
        for (id = 0; id <= spawns.size(); id++) {
            if (!spawns.containsKey(id)) {
                spawns.put(id, spawn);
                return id;
            }
        }
        return -1;
    }

    @Override
    public void removeSpawnPoint(int index) {
        spawns.remove(index);
    }

    @Override
    public void removeSpawnPoint(Location3D location) {
        for (Map.Entry<Integer, Location3D> e : spawns.entrySet()) {
            if (e.getValue().equals(location)) {
                spawns.remove(e.getKey());
                return;
            }
        }
    }

    @Override
    public Optional<Round> getRound() {
        return Optional.fromNullable(parent.rounds.get(this));
    }

    @Override
    public Minigame getMinigame() {
        return parent;
    }

    @Override
    public String getPlugin() {
        return parent.getPlugin();
    }
}
