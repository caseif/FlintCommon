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
import net.caseif.flint.common.util.file.CommonDataFiles;
import net.caseif.flint.common.util.helper.JsonHelper;
import net.caseif.flint.component.exception.OrphanedComponentException;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.lobby.type.StatusLobbySign;
import net.caseif.flint.util.physical.Location3D;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Implements {@link LobbySign}.
 *
 * @author Max Roncacé
 */
public abstract class CommonLobbySign implements LobbySign, CommonComponent<Arena> {

    public static final String PERSIST_TYPE_KEY = "type";
    public static final String PERSIST_TYPE_STATUS = "status";
    public static final String PERSIST_TYPE_LISTING = "listing";

    public static final String PERSIST_INDEX_KEY = "index";

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
    private void store(boolean remove) {
        try {
            File store = CommonDataFiles.LOBBY_STORE.getFile(getArena().getMinigame());
            JsonObject json = JsonHelper.readOrCreateJson(store);

            JsonObject arena = json.getAsJsonObject(getArena().getId());
            if (arena == null) {
                if (!remove) { // okay to create it since we're newly storing the sign
                    arena = new JsonObject();
                    json.add(getArena().getId(), arena);
                } else { // can't delete something that's not there
                    CommonCore.logWarning("Anomaly: Engine requested removal of lobby sign from store, but arena was "
                            + "not defined");
                    return;
                }
            }

            String locSerial = getLocation().serialize();
            if (remove) {
                if (arena.has(locSerial)) {
                    arena.remove(locSerial);
                } else {
                    CommonCore.logWarning("Engine requested removal of lobby sign from store, but respective section "
                            + "was not defined");
                }
            } else {
                JsonObject sign = new JsonObject();
                arena.add(locSerial, sign);

                String type;
                if (this instanceof StatusLobbySign) {
                    type = PERSIST_TYPE_STATUS;
                } else if (this instanceof ChallengerListingLobbySign) {
                    type = PERSIST_TYPE_LISTING;
                } else {
                    throw new AssertionError("Invalid LobbySign object. Report this immediately.");
                }
                sign.addProperty(PERSIST_TYPE_KEY, type);
                if (this instanceof ChallengerListingLobbySign) {
                    sign.addProperty(PERSIST_INDEX_KEY, ((ChallengerListingLobbySign) this).getIndex());
                }
            }

            try (FileWriter writer = new FileWriter(store)) {
                writer.write(json.toString());
            }
        } catch (IOException ex) {
            CommonCore.logSevere("Failed to write to lobby sign store");
            ex.printStackTrace();
        }
    }

    public void store() {
        store(false);
    }


    /**
     * Removes this {@link LobbySign} from persistent storage.
     */
    public void unstore() {
        store(true);
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

}
