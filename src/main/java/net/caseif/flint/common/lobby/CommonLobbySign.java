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
package net.caseif.flint.common.lobby;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.exception.OrphanedObjectException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.util.physical.Location3D;

/**
 * Implements {@link LobbySign}.
 *
 * @author Max Roncacé
 */
public abstract class CommonLobbySign implements LobbySign {

    private final Location3D location;
    private CommonArena arena; //TODO: make final and store boolean "orphaned" flag instead of nulling this
    // same goes for other Orphanable classes

    protected CommonLobbySign(Location3D location, CommonArena arena) {
        this.location = location;
        this.arena = arena;
    }

    @Override
    public Location3D getLocation() throws OrphanedObjectException {
        checkState();
        return location;
    }

    @Override
    public Arena getArena() throws OrphanedObjectException {
        checkState();
        return arena;
    }

    @Override
    public void unregister() throws OrphanedObjectException {
        arena.unregisterLobbySign(location);
    }

    public Minigame getMinigame() {
        return arena.getMinigame();
    }

    public String getPlugin() {
        return arena.getPlugin();
    }

    /**
     * Stores this {@link LobbySign} to persistent storage.
     */
    public abstract void store();

    /**
     * Removes this {@link LobbySign} from persistent storage.
     */
    public abstract void unstore();

    /**
     * Checks the state of this object.
     *
     * @throws OrphanedObjectException If this object is orphaned
     */
    protected void checkState() throws OrphanedObjectException {
        if (arena == null) {
            throw new OrphanedObjectException(this);
        }
    }

    /**
     * Orphans this object.
     */
    public void orphan() {
        arena = null;
    }

}
