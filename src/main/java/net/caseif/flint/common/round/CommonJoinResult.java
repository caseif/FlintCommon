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

package net.caseif.flint.common.round;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.round.JoinResult;

import com.google.common.base.Preconditions;

/**
 * Implements {@link JoinResult}.
 *
 * @author Max Roncacé
 */
public class CommonJoinResult implements JoinResult {

    private Status status;

    private Challenger challenger;
    private Throwable throwable;

    public CommonJoinResult(Challenger challenger) {
        this.challenger = challenger;
        this.status = Status.SUCCESS;
    }

    public CommonJoinResult(Throwable throwable) {
        this.throwable = throwable;
        this.status = Status.INTERNAL_ERROR;
    }

    public CommonJoinResult(Status status) {
        assert status != Status.SUCCESS;
        assert status != Status.INTERNAL_ERROR;
        this.status = status;
    }

    @Override
    public Challenger getChallenger() throws IllegalStateException {
        Preconditions.checkState(status == Status.SUCCESS, "Cannot get Challenger if JoinResult status is not SUCCESS");
        return challenger;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Throwable getThrowable() throws IllegalStateException {
        Preconditions.checkState(status == Status.INTERNAL_ERROR,
                "Cannot get Throwable if JoinResult status is not INTERNAL_ERROR");
        return throwable;
    }

}
