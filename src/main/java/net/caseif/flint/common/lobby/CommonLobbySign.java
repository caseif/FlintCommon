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
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.util.physical.Location3D;

/**
 * Implements {@link LobbySign}.
 *
 * @author Max Roncacé
 */
public abstract class CommonLobbySign implements LobbySign, CommonComponent<Arena> {

    private final Location3D location;
    private final CommonArena arena;

    private boolean orphan = false;

    protected CommonLobbySign(Location3D location, CommonArena arena) {
        this.location = location;
        this.arena = arena;
    }

    @Override
    public Arena getOwner() throws OrphanedComponentException {
        checkState();
        return arena;
    }

    @Override
    public Arena getArena() throws OrphanedComponentException {
        return getOwner();
    }

    @Override
    public Location3D getLocation() throws OrphanedComponentException {
        checkState();
        return location;
    }

    @Override
    public void unregister() throws OrphanedComponentException {
        checkState();
        arena.unregisterLobbySign(location);
    }

    /**
     * Stores this {@link LobbySign} to persistent storage.
     */
    public abstract void store();

    /**
     * Removes this {@link LobbySign} from persistent storage.
     */
    public abstract void unstore();

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

}
