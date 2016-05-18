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
package net.caseif.flint.common.lobby.wizard;

import net.caseif.flint.common.minigame.CommonMinigame;
import net.caseif.flint.minigame.Minigame;

import java.util.HashMap;
import java.util.UUID;

public abstract class CommonWizardManager implements IWizardManager {

    private CommonMinigame minigame;

    protected final HashMap<UUID, IWizardPlayer> wizardPlayers = new HashMap<>();

    /**
     * Creates a new {@link CommonWizardManager} for the given {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to back the new
     *     {@link CommonWizardManager}
     */
    protected CommonWizardManager(Minigame minigame) {
        this.minigame = (CommonMinigame) minigame;
    }

    @Override
    public CommonMinigame getOwner() {
        return minigame;
    }

    @Override
    public boolean hasPlayer(UUID uuid) {
        return wizardPlayers.containsKey(uuid);
    }

    @Override
    public void removePlayer(UUID uuid) {
        wizardPlayers.remove(uuid);
    }

    @Override
    public String[] accept(UUID uuid, String input) {
        if (wizardPlayers.containsKey(uuid)) {
            return wizardPlayers.get(uuid).accept(input);
        } else {
            throw new IllegalArgumentException("Player with UUID " + uuid.toString() + " is not engaged in a wizard");
        }
    }

    @Override
    public void withholdMessage(UUID uuid, String sender, String message) {
        if (wizardPlayers.containsKey(uuid)) {
            wizardPlayers.get(uuid).withholdMessage(sender, message);
        } else {
            throw new IllegalArgumentException("Player with UUID " + uuid.toString() + " is not engaged in a wizard");
        }
    }

}
