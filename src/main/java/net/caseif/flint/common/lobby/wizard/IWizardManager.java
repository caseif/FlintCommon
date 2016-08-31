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
import net.caseif.flint.component.Component;
import net.caseif.flint.util.physical.Location3D;

import java.util.UUID;

/**
 * Interface defining the manager for the integrated lobby wizard.
 */
public interface IWizardManager extends Component<CommonMinigame> {

    @Override
    CommonMinigame getOwner();

    /**
     * Gets whether the player with the given {@link UUID} is present in this
     * {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player to look up
     * @return Whether the player is present in this {@link WizardManager}
     */
    boolean hasPlayer(UUID uuid);

    /**
     * Adds a player to this {@link WizardManager}.
     *
     * @param uuid The {@link UUID} of the player
     * @param location The {@link Location3D location} targeted by the player
     */
    void addPlayer(UUID uuid, Location3D location);

    /**
     * Removes the player with the given {@link UUID} from this
     * {@link CommonWizardManager}.
     *
     * @param uuid The {@link UUID} of the player to remove
     */
    void removePlayer(UUID uuid);

    /**
     * Accepts input from the player with the given {@link UUID}.
     *
     * @param uuid The {@link UUID} of the player to accept input from
     * @param input The input to accept
     * @return The response to the input
     * @throws IllegalArgumentException If the player with the given
     *     {@link UUID} is not currently engaged in a wizard
     */
    String[] accept(UUID uuid, String input);

    /**
     * Withholds a chat message from the player with the given UUID, to be
     * displayed once the wizard has been exited.
     *
     * @param uuid The player to withhold a message from
     * @param sender The name of the player sending the message
     * @param message The message content
     */
    void withholdMessage(UUID uuid, String sender, String message);

}
