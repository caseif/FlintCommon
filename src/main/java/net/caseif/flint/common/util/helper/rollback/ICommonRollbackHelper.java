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
package net.caseif.flint.common.util.helper.rollback;

import net.caseif.flint.util.physical.Location3D;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public interface ICommonRollbackHelper {

    void createRollbackDatabase() throws IOException, SQLException;

    Map<Integer, String> loadStateMap() throws IOException;

    void initializeStateStore() throws IOException;

    void saveStateSerial(int id, String serial) throws IOException;

    void clearStateStore() throws IOException;

    void logChange(int recordType, Location3D location, UUID uuid, String type, int data, String stateSerial)
            throws IOException, SQLException;

    void popRollbacks() throws IOException, SQLException;

    void rollbackBlock(int id, Location3D location, String type, int data, String stateSerial)
            throws IOException;

    void rollbackEntityChange(int id, UUID uuid, Location3D location, String type, String stateSerial)
            throws IOException;

    void rollbackEntityCreation(int id, UUID uuid);

    void cacheEntities();

}
