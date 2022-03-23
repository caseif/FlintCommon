/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022, Max Roncace <me@caseif.net>
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
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.config.RoundConfigNode;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.populator.LobbySignPopulator;
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
    private final Type type;

    private boolean orphan = false;

    protected CommonLobbySign(Location3D location, CommonArena arena, Type type) {
        this.location = location;
        this.arena = arena;
        this.type = type;
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
    public Type getType() {
        return type;
    }

    @Override
    public void unregister() throws OrphanedComponentException {
        checkState();
        arena.unregisterLobbySign(location);
    }

    @Override
    public void update() {
        checkState();

        RoundConfigNode<LobbySignPopulator> node;
        if (getType() == Type.STATUS) {
            node = ConfigNode.STATUS_LOBBY_SIGN_POPULATOR;
        } else if (getType() == Type.CHALLENGER_LISTING) {
            node = ConfigNode.CHALLENGER_LISTING_LOBBY_SIGN_POPULATOR;
        } else {
            throw new AssertionError();
        }
        LobbySignPopulator pop = getArena().getRound().isPresent()
                ? getArena().getRound().get().getConfigValue(node)
                : getArena().getMinigame().getConfigValue(node);
        updatePhysicalSign(pop.first(this), pop.second(this), pop.third(this), pop.fourth(this));
    }

    protected abstract void updatePhysicalSign(String... lines);

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

    protected String[] getChallengerListingText() {
        if (!validate()) {
            // hehe, illegal "state"
            unregister();
            CommonCore.logWarning("Cannot update lobby sign at (" + "\"" + location.getWorld() + "\", "
                    + location.getX() + ", " + location.getY() + ", " + location.getZ() + "): not a sign. Removing...");
        }
        int startIndex = ((ChallengerListingLobbySign) this).getIndex() * getSignSize();
        boolean round = getArena().getRound().isPresent();

        String[] lines = new String[getSignSize()];
        for (int i = 0; i < getSignSize(); i++) {
            if (round && startIndex + i < getArena().getRound().get().getChallengers().size()) {
                lines[i] = getArena().getRound().get().getChallengers().get(startIndex + i).getName();
            } else {
                lines[i] = "";
            }
        }

        return lines;
    }

    protected String[] getStatusText() {
        if (!validate()) {
            unregister();
            throw new IllegalStateException("Cannot update lobby sign: not a sign. Removing...");
        }

        String[] lines = new String[getSignSize()];

        lines[0] = getArena().getDisplayName();
        if (getArena().getRound().isPresent()) {
            lines[1] = getArena().getRound().get().getLifecycleStage().getId().toUpperCase();
            long seconds = getArena().getRound().get().getRemainingTime() != -1
                    ? getArena().getRound().get().getRemainingTime()
                    : getArena().getRound().get().getTime();
            String time = seconds / 60 + ":" + (seconds % 60 >= 10 ? seconds % 60 : "0" + seconds % 60);
            lines[2] = time;
            // get max player count
            int maxPlayers = getArena().getRound().get().getConfigValue(ConfigNode.MAX_PLAYERS);
            // format player count relative to max
            String players = getArena().getRound().get().getChallengers().size() + "/"
                    + (maxPlayers > 0 ? maxPlayers : "∞");
            // add label to player count (shortened version used if the full one won't fit)
            players += players.length() <= 5 ? " players" : (players.length() <= 7 ? " plyrs" : "");
            lines[3] = players;
        } else {
            for (int i = 1; i < getSignSize(); i++) {
                lines[i] = "";
            }
        }

        return lines;
    }

    protected abstract boolean validate();

    protected abstract int getSignSize();

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
