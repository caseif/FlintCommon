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
public class StockChallengerListingLobbySignPopulator implements LobbySignPopulator {

    private static final String EMPTY_STRING = "";
    private static final int SIGN_SIZE = 4;

    @Override
    public String first(LobbySign sign) {
        return getPlayer(sign, 0);
    }

    @Override
    public String second(LobbySign sign) {
        return getPlayer(sign, 1);
    }

    @Override
    public String third(LobbySign sign) {
        return getPlayer(sign, 2);
    }

    @Override
    public String fourth(LobbySign sign) {
        return getPlayer(sign, 3);
    }

    public String getPlayer(LobbySign sign, int lineIndex) {
        int startIndex = ((ChallengerListingLobbySign) sign).getIndex() * SIGN_SIZE;
        if (sign.getArena().getRound().isPresent()
                && startIndex + lineIndex < sign.getArena().getRound().get().getChallengers().size()) {
            return sign.getArena().getRound().get().getChallengers().get(startIndex + lineIndex).getName();
        }
        return EMPTY_STRING;
    }

}
