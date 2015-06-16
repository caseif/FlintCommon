package net.caseif.flint.common.util;

import net.caseif.flint.Minigame;

import java.io.File;

/**
 * Utility methods implemented on a platform-specific basis.
 *
 * @author Max Roncacé
 * @since 1.0.0
 */
public interface PlatformUtils {

    /**
     * Returns the implementation's data folder.
     *
     * @return The implementation's data folder
     * @since 1.0.0
     */
    File getDataFolder();

    /**
     * Returns the data folder for the given {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to get the data folder of
     * @return The data folder for the given {@link Minigame}
     * @since 1.0.0
     */
    File getDataFolder(Minigame minigame);

}
