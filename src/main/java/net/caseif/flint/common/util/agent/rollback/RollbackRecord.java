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
