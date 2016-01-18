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
