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
package net.caseif.flint.common.lobby.populator;

import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.lobby.populator.LobbySignPopulator;
import net.caseif.flint.lobby.type.ChallengerListingLobbySign;
import net.caseif.flint.lobby.type.StatusLobbySign;

/**
 * The stock Flint lobby sign populator.
 */
public class StockStatusLobbySignPopulator implements LobbySignPopulator {

    private static final String EMPTY_STRING = "";
    private static final int SIGN_SIZE = 4;

    @Override
    public String first(LobbySign sign) {
        return sign.getArena().getDisplayName();
    }

    @Override
    public String second(LobbySign sign) {
        if (sign.getArena().getRound().isPresent()) {
            return sign.getArena().getRound().get().getLifecycleStage().getId().toUpperCase();
        }
        return EMPTY_STRING;
    }

    @Override
    public String third(LobbySign sign) {
        if (sign.getArena().getRound().isPresent()) {
            long seconds = sign.getArena().getRound().get().getRemainingTime() != -1
                    ? sign.getArena().getRound().get().getRemainingTime()
                    : sign.getArena().getRound().get().getTime();
            return seconds / 60 + ":" + (seconds % 60 >= 10 ? seconds % 60 : "0" + seconds % 60);
        }
        return EMPTY_STRING;
    }

    @Override
    public String fourth(LobbySign sign) {
            if (sign.getArena().getRound().isPresent()) {
                int maxPlayers = sign.getArena().getRound().get().getConfigValue(ConfigNode.MAX_PLAYERS);
                // format player count relative to max
                String players = sign.getArena().getRound().get().getChallengers().size() + "/"
                        + (maxPlayers > 0 ? maxPlayers : "∞");
                // add label to player count (shortened version used if the full one won't fit)
                players += players.length() <= 5 ? " players" : (players.length() <= 7 ? " plyrs" : "");
                return players;
            }
            return EMPTY_STRING;
    }

    public String getPlayer(LobbySign sign, int lineIndex) {
        int startIndex = ((ChallengerListingLobbySign) this).getIndex() * SIGN_SIZE;
        if (sign.getArena().getRound().isPresent()
                && startIndex + lineIndex < sign.getArena().getRound().get().getChallengers().size()) {
            return sign.getArena().getRound().get().getChallengers().get(startIndex + lineIndex).getName();
        }
        return EMPTY_STRING;
    }

}
