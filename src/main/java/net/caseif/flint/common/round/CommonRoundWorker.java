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
package net.caseif.flint.common.round;

import net.caseif.flint.common.event.round.CommonRoundTimerTickEvent;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.lobby.LobbySign;
import net.caseif.flint.round.Round;
import net.caseif.flint.util.physical.Boundary;

/**
 * Used as the {@link Runnable} for {@link Round} timers.
 *
 * @author Max Roncace
 */
public abstract class CommonRoundWorker implements Runnable {

    private final CommonRound round;

    protected CommonRoundWorker(CommonRound round) {
        this.round = round;
    }

    public CommonRound getRound() {
        return round;
    }

    public void run() {
        if (round.isTimerTicking()) {
            handleTick();
        }
        if (!round.isOrphaned()) {
            checkPlayerLocations();

            for (LobbySign sign : round.getArena().getLobbySigns()) {
                if (sign.getType() == LobbySign.Type.STATUS) {
                    sign.update();
                }
            }
        }
    }

    private void handleTick() {
        boolean stageSwitch = round.getLifecycleStage().getDuration() > 0
                && round.getTime() >= round.getLifecycleStage().getDuration();
        if (stageSwitch) {
            if (round.getNextLifecycleStage().isPresent()) {
                round.nextLifecycleStage();
            } else {
                round.end(round.getConfigValue(ConfigNode.ROLLBACK_ON_END), true);
                return;
            }
        } else {
            round.setTime(round.getTime() + 1, false);
        }
        round.getArena().getMinigame().getEventBus().post(new CommonRoundTimerTickEvent(round, round.getTime() - 1,
                stageSwitch ? 0 : round.getTime()));
    }

    /**
     * Verifies that all players are within the arena {@link Boundary}, and
     * takes appropriate action if a player is not.
     */
    protected abstract void checkPlayerLocations();

}
