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
package net.caseif.flint.common.event.round;

import net.caseif.flint.event.round.RoundChangeLifecycleStageEvent;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.round.Round;

/**
 * Implementation of {@link RoundChangeLifecycleStageEvent}.
 *
 * @author Max Roncacé
 */
public class CommonRoundChangeLifecycleStageEvent extends CommonRoundEvent implements RoundChangeLifecycleStageEvent {

    private final LifecycleStage before;
    private final LifecycleStage after;

    public CommonRoundChangeLifecycleStageEvent(Round round, LifecycleStage before, LifecycleStage after) {
        super(round);
        this.before = before;
        this.after = after;
    }

    @Override
    public final LifecycleStage getStageBefore() {
        return before;
    }

    @Override
    public final LifecycleStage getStageAfter() {
        return after;
    }

}
