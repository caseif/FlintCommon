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

package net.caseif.flint.common.event;

import net.caseif.flint.common.CommonCore;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Singleton exception handler class for Flint events.
 *
 * <p>This is basically only necessary because Google screwed up their exception
 * logging. See https://github.com/google/guava/issues/2093 for more info.</p>
 *
 * @author Max Roncacé
 */
public class FlintSubscriberExceptionHandler implements SubscriberExceptionHandler {

    private static FlintSubscriberExceptionHandler INSTANCE;

    private FlintSubscriberExceptionHandler() {
    }

    /**
     * Gets an instance of this singleton.
     *
     * @return An instance of the singleton.
     */
    public static FlintSubscriberExceptionHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlintSubscriberExceptionHandler();
        }
        return INSTANCE;
    }

    /**
     * Deinitializes this singleton class.
     */
    public static void deinitialize() {
        INSTANCE = null;
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        CommonCore.logSevere("Failed to dispatch event " + context.getEvent().getClass() + " to "
                + context.getSubscriberMethod());
        exception.printStackTrace();
    }

}
