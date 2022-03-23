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

package net.caseif.flint.common.util.file;

import net.caseif.flint.common.CommonCore;
import net.caseif.flint.minigame.Minigame;

import java.io.File;

/**
 * Represents a {@link Minigame}-specific data file.
 *
 * @author Max Roncacé
 */
public class MinigameDataFile extends DataFile {

    public MinigameDataFile(String fileName, boolean isDirectory, boolean createIfMissing) {
        super(fileName, isDirectory, createIfMissing);
    }

    public MinigameDataFile(String fileName, boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public MinigameDataFile(String fileName) {
        super(fileName);
    }

    /**
     * Gets the {@link File} backing this {@link MinigameDataFile} for the given
     * {@link Minigame}.
     *
     * @param minigame The {@link Minigame} to retrieve a {@link File} for
     * @return The {@link File} backing this {@link MinigameDataFile} for the
     *     given {@link Minigame}.
     */
    public File getFile(Minigame minigame) {
        return new File(CommonCore.PLATFORM_UTILS.getDataFolder(minigame),
                CommonDataFiles.ROOT_DATA_DIR + File.separatorChar + getFileName());
    }

}
