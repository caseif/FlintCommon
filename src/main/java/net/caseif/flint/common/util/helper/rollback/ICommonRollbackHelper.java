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
