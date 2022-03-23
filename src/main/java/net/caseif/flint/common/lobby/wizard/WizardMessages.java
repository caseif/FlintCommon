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

package net.caseif.flint.common.lobby.wizard;

import net.caseif.flint.common.util.agent.chat.ChatCode;

/**
 * Static utility class for wizard messages.
 */
public class WizardMessages {

    public static final String INFO_COLOR = ChatCode.TEAL;
    public static final String ERROR_COLOR = ChatCode.LIGHT_RED;
    public static final String EM_COLOR = ChatCode.GOLD;

    // informational messages
    public static final String WELCOME = INFO_COLOR + "Welcome to the lobby sign wizard!";
    public static final String CHAT_WITHHOLDING = INFO_COLOR + "Chat messages will be withheld until the wizard is "
            + "complete.";
    public static final String GET_ARENA = INFO_COLOR + "To start, please type "
            + "the name of the arena you would like to create a lobby sign for. (You may type " + EM_COLOR + " cancel "
            + INFO_COLOR + " at any time to exit the wizard.";
    public static final String GET_TYPE = INFO_COLOR + "Next, please select the type of lobby sign you would like "
            + "to create from the list below (type a number):";
    public static final String GET_TYPE_STATUS = EM_COLOR + "1) " + INFO_COLOR + "Status - Status signs display "
            + "basic information about the round contained by an arena such as timer info and player count.";
    public static final String GET_TYPE_LISTING = EM_COLOR + "2) " + INFO_COLOR + "Player Listing - Player listing "
            + "signs display a portion of the players in a round. If this is selected, you will be asked next to "
            + "define which portion of players to display.";
    public static final String GET_INDEX = INFO_COLOR + " Next, please select which portion of players you would "
            + "like the player listing sign to display. ("
            + EM_COLOR + "1" + INFO_COLOR + " will display players " + EM_COLOR + "1-4" + INFO_COLOR + ", "
            + EM_COLOR + "2" + INFO_COLOR + " will display players " + EM_COLOR + "5-8" + INFO_COLOR + ", "
            + "and so on.)";
    public static final String CONFIRM_1 = INFO_COLOR + "Okay! Your lobby sign will be created with the following "
            + "info:";
    public static final String CONFIRM_2 = INFO_COLOR + "Is this okay? "
            + "(Type " + EM_COLOR + "yes" + INFO_COLOR + " or " + EM_COLOR + "no" + INFO_COLOR + ".)";
    public static final String RESET = INFO_COLOR + "The wizard will now reset.";
    public static final String FINISH = INFO_COLOR + "Your lobby sign was successfully created! The wizard will now "
            + "exit.";
    public static final String MESSAGE_PLAYBACK = INFO_COLOR + "Playing back withheld chat messages...";

    public static final String CANCELLED = ERROR_COLOR + "Lobby sign creation cancelled.";

    // error messages
    public static final String BAD_ARENA = ERROR_COLOR + "No arena by that ID exists! Please enter another arena ID.";
    public static final String BAD_TYPE = ERROR_COLOR + "Invalid sign type! Please select a valid option.";
    public static final String BAD_INDEX = ERROR_COLOR + "Invalid sign index! Please enter a number greater than or "
            + "equal to 1.";
    public static final String BAD_CONFIRMATION = ERROR_COLOR + "Please type " + EM_COLOR + "yes" + INFO_COLOR + " or "
            + EM_COLOR + "no" + INFO_COLOR + ".";
    public static final String ARENA_REMOVED = ERROR_COLOR + "The selected arena has been removed. The wizard will now "
    + "exit.";
    public static final String GENERIC_ERROR = ERROR_COLOR + "An internal exception occurred while creating the lobby "
            + "sign. The wizard will now exit.";

    // other stuff
    public static final String DIVIDER;

    static {
        final int dividerLength = 36;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dividerLength; i++) {
            sb.append("-");
        }
        DIVIDER = sb.toString();
    }

}
