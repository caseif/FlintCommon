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

package net.caseif.flint.common.util.agent.rollback;

import net.caseif.flint.util.physical.Location3D;

import java.util.UUID;

/**
 * Created by mpron on 8/31/2016.
 */
public class RollbackRecord {

    private final int id;
    private final UUID uuid;
    private final Location3D location;
    private final String type;
    private final int data;
    private final String stateSerial;
    private final Type recordType;

    protected RollbackRecord(int id, UUID uuid, Location3D location, String type, int data, String stateSerial,
                             Type recordType) {
        this.id = id;
        this.uuid = uuid;
        this.location = location;
        this.type = type;
        this.data = data;
        this.stateSerial = stateSerial;
        this.recordType = recordType;
    }

    public static RollbackRecord createBlockRecord(int id, Location3D loc, String type, int data, String stateSerial) {
        return new RollbackRecord(id, null, loc, type, data, stateSerial, Type.BLOCK_CHANGE);
    }

    public static RollbackRecord createEntityCreationRecord(int id, UUID uuid) {
        return new RollbackRecord(id, uuid, null, null, 0, null, Type.ENTITY_CREATION);
    }

    public static RollbackRecord createEntityChangeRecord(int id, UUID uuid, Location3D loc, String type,
                                                          String stateSerial) {
        return new RollbackRecord(id, uuid, loc, type, 0, stateSerial, Type.BLOCK_CHANGE);
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location3D getLocation() {
        return location;
    }

    public String getTypeData() {
        return type;
    }

    public int getData() {
        return data;
    }

    public String getStateSerial() {
        return stateSerial;
    }

    public Type getType() {
        return recordType;
    }

    public enum Type {
        BLOCK_CHANGE,
        ENTITY_CREATION,
        ENTITY_CHANGE
    }

}
