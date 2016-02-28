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
package net.caseif.flint.common;

import net.caseif.flint.FlintCore;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.component.CommonComponent;
import net.caseif.flint.common.util.PlatformUtils;
import net.caseif.flint.minigame.Minigame;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implements {@link FlintCore}.
 *
 * @author Max Roncacé
 */
public abstract class CommonCore extends FlintCore {

    private static Map<String, Minigame> minigames = new HashMap<>();

    /**
     * The singleton {@link PlatformUtils} instance.
     */
    public static PlatformUtils PLATFORM_UTILS;

    /**
     * Returns the object mapping plugin names to their respective minigames (no
     * rhyme intended).
     *
     * @return The object mapping plugin names to their respective minigames
     */
    public static Map<String, Minigame> getMinigames() {
        return minigames;
    }

    /**
     * Gets the {@link Challenger} with the given {@link UUID} from any existing
     * {@link Minigame}.
     *
     * @param uuid The {@link UUID} of the {@link Challenger} to get
     * @return The {@link Challenger} with the given {@link UUID}
     */
    public static Optional<Challenger> getChallenger(UUID uuid) {
        for (Minigame mg : getMinigames().values()) {
            if (mg.getChallenger(uuid).isPresent()) {
                return mg.getChallenger(uuid);
            }
        }
        return Optional.absent();
    }

    /**
     * Used to log non-issue informational messages about the status of the
     * software.
     *
     * @param message The log message
     */
    public static void logInfo(String message) {
        ((CommonCore) INSTANCE).logInfo0(message);
    }

    protected abstract void logInfo0(String message);

    /**
     * Used to log warning events which may have a minor impact on the
     * performance or functionality of the software.
     *
     * @param message The log message
     */
    public static void logWarning(String message) {
        ((CommonCore) INSTANCE).logWarning0(message);
    }

    protected abstract void logWarning0(String message);

    /**
     * Used to log severe events which may be detrimental to the performance or
     * functionality of the software.
     *
     * @param message The log message
     */
    public static void logSevere(String message) {
        ((CommonCore) INSTANCE).logSevere0(message);
    }

    protected abstract void logSevere0(String message);

    /**
     * Used to log verbose events (only visible if verbose logging is explicitly
     * enabled.
     *
     * @param message The log message
     */
    public static void logVerbose(String message) {
        ((CommonCore) INSTANCE).logVerbose0(message);
    }

    protected abstract void logVerbose0(String message);

    public static void orphan(CommonComponent<?> component) {
        ((CommonCore) INSTANCE).orphan0(component);
    }

    protected abstract void orphan0(CommonComponent<?> component);

}
