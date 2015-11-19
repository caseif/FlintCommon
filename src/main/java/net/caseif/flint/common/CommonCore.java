/*
 * New BSD License (BSD-new)
 *
 * Copyright (c) 2015 Maxim Roncacé
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
