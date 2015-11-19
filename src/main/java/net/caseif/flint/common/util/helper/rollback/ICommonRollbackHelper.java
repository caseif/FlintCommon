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
