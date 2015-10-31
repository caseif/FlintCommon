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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

public abstract class CommonRollbackHelper {

    public static final String SQLITE_PROTOCOL = "jdbc:sqlite:";
    public static final Properties SQL_QUERIES = new Properties();

    protected static final int RECORD_TYPE_BLOCK_CHANGED = 0;
    protected static final int RECORD_TYPE_ENTITY_CREATED = 1;
    protected static final int RECORD_TYPE_ENTITY_CHANGED = 2;

    protected final File rollbackStore;
    protected final File stateStore;

    private final CommonArena arena;

    protected CommonRollbackHelper(CommonArena arena, File rollbackStore, File stateStore) {
        this.arena = arena;
        this.rollbackStore = rollbackStore;
        this.stateStore = stateStore;
    }

    static {
        try (InputStream is = CommonRollbackHelper.class.getResourceAsStream("/sql-queries.properties")) {
            SQL_QUERIES.load(is);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load SQL query strings", ex);
        }
    }

    /**
     * Returns the {@link CommonArena} associated with this
     * {@link CommonRollbackHelper}.
     *
     * @return The {@link CommonArena} associated with this
     * {@link CommonRollbackHelper}.
     */
    public CommonArena getArena() {
        return arena;
    }

    /**
     * Creates a rollback database for the arena backing this
     * {@link CommonRollbackHelper}.
     *
     * @throws IOException If an exception occurs while creating the database
     *     file
     * @throws SQLException If an exception occurs while manipulating the
     *     database
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createRollbackDatabase() throws IOException, SQLException {
        if (!rollbackStore.exists()) {
            rollbackStore.delete();
        }
        rollbackStore.createNewFile();
        if (!stateStore.exists()) {
            stateStore.delete();
        }
        stateStore.createNewFile();
        try (Connection conn = DriverManager.getConnection(SQLITE_PROTOCOL + rollbackStore.getAbsolutePath())) {
            try (PreparedStatement st = conn.prepareStatement(SQL_QUERIES.getProperty("create-rollback-table")
                            .replace("{table}", getArena().getId()))
            ) {
                st.executeUpdate();
            }
        }
    }

    protected void logChange(int recordType, Location3D location, UUID uuid, String type, int data,
                             JsonObject state) throws IOException, SQLException {
        String world = location.getWorld().isPresent() ? location.getWorld().get() : arena.getWorld();
        Preconditions.checkNotNull(location, "Location required for all record types");
        switch (recordType) {
            case RECORD_TYPE_BLOCK_CHANGED:
                Preconditions.checkNotNull(type, "Type required for BLOCK_CHANGED record type");
                break;
            case RECORD_TYPE_ENTITY_CREATED:
                Preconditions.checkNotNull(uuid, "UUID required for ENTITY_CREATED record type");
                Preconditions.checkNotNull(type, "Type required for ENTITY_CREATED record type");
                break;
            case RECORD_TYPE_ENTITY_CHANGED:
                Preconditions.checkNotNull(type, "Type required for ENTITY_CHANGED record type");
                Preconditions.checkNotNull(state, "State required for ENTITY_CHANGED record type");
                break;
            default:
                throw new IllegalArgumentException("Undefined record type");
        }
        if (!rollbackStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.createNewFile();
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + rollbackStore.getPath())) {
            String querySql;
            switch (recordType) {
                case RECORD_TYPE_BLOCK_CHANGED:
                    querySql = SQL_QUERIES.getProperty("query-by-location")
                            .replace("{world}", "\"" + world + "\"")
                            .replace("{x}", "" + location.getX())
                            .replace("{y}", "" + location.getY())
                            .replace("{z}", "" + location.getZ());
                    break;
                case RECORD_TYPE_ENTITY_CHANGED:
                    querySql = SQL_QUERIES.getProperty("query-by-uuid")
                            .replace("{uuid}", "\"" + uuid.toString() + "\"");
                    break;
                default:
                    querySql = null;
                    break;
            }
            if (querySql != null) {
                querySql = querySql.replace("{table}", getArena().getId());
                try (
                        PreparedStatement query = conn.prepareStatement(querySql);
                        ResultSet queryResults = query.executeQuery();
                ) {
                    if (queryResults.next()) {
                        return; // subject has already been modified; no need to re-record
                    }
                }
            }

            String updateSql;
            switch (recordType) {
                case RECORD_TYPE_BLOCK_CHANGED:
                    updateSql = SQL_QUERIES.getProperty("insert-block-rollback-record")
                            .replace("{world}", world)
                            .replace("{x}", "" + location.getX())
                            .replace("{y}", "" + location.getY())
                            .replace("{z}", "" + location.getZ())
                            .replace("{type}", type)
                            .replace("{data}", "" + data);
                    break;
                case RECORD_TYPE_ENTITY_CREATED:
                    updateSql = SQL_QUERIES.getProperty("insert-entity-created-rollback-record")
                            .replace("{world}", world)
                            .replace("{uuid}", uuid.toString());
                    break;
                case RECORD_TYPE_ENTITY_CHANGED:
                    updateSql = SQL_QUERIES.getProperty("insert-entity-changed-rollback-record")
                            .replace("{world}", world)
                            .replace("{x}", "" + location.getX())
                            .replace("{y}", "" + location.getY())
                            .replace("{z}", "" + location.getZ())
                            .replace("{uuid}", uuid.toString())
                            .replace("{type}", type);
                    break;
                default:
                    throw new AssertionError("Inconsistency detected in method: recordType is in an illegal state. "
                            + "Report this immediately.");
            }
            if (updateSql != null) {
                // replace non-negotiable values
                updateSql = updateSql
                        .replace("{table}", getArena().getId())
                        .replace("{state}", "" + (state != null ? 1 : 0))
                        .replace("{record_type}", "" + recordType);
            }
            int id;
            try (PreparedStatement ps = conn.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                try (ResultSet gen = ps.getGeneratedKeys()) {
                    if (gen.next()) {
                        id = gen.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated key from update query");
                    }
                }
            }
            if (state != null) {
                JsonObject json = (JsonObject) new JsonParser().parse(new FileReader(stateStore));
                JsonObject arenaSec;
                if (json.get(getArena().getId()).isJsonObject()) {
                    arenaSec = json.getAsJsonObject(getArena().getId());
                } else {
                    arenaSec = new JsonObject();
                    json.add(getArena().getId(), arenaSec);
                }
                if (arenaSec.has(Integer.toString(id))) {
                    throw new AssertionError("Tried to store state with id " + id + ", but "
                            + "index was already present in rollback store! Something's gone terribly "
                            + "wrong."); // technically should never happen but you never know
                }
                arenaSec.add(Integer.toString(id), state);
                try (FileWriter writer = new FileWriter(stateStore)) {
                    writer.write(new Gson().toJson(json));
                }
            }
        }
    }

    protected static Optional<Arena> checkChangeAtLocation(Location3D location) {
        for (Minigame mg : CommonCore.getMinigames().values()) {
            for (Arena arena : mg.getArenas()) {
                if (arena.getBoundary().contains(location)) {
                    return Optional.of(arena);
                }
            }
        }
        return Optional.absent();
    }

    @SuppressWarnings("deprecation")
    public void popRollbacks() throws SQLException {
        if (rollbackStore.exists()) {
            JsonObject json;
            JsonObject arenaSection = null;
            try {
                json = new JsonParser().parse(new FileReader(stateStore)).getAsJsonObject();
                arenaSection = json.getAsJsonObject(getArena().getId());
            } catch (IOException ex) {
                json = null;
                CommonCore.logSevere("State store is corrupt - tile and entity data will not be restored");
                ex.printStackTrace();
                //noinspection ResultOfMethodCallIgnored
                stateStore.delete();
            }

            try (
                    Connection conn = DriverManager.getConnection(SQLITE_PROTOCOL + rollbackStore.getAbsolutePath());
                    PreparedStatement query = conn.prepareStatement(SQL_QUERIES.getProperty("get-all-records")
                            .replace("{table}", getArena().getId()));
                    PreparedStatement drop = conn.prepareStatement(SQL_QUERIES.getProperty("drop-table")
                            .replace("{table}", getArena().getId()));
                    ResultSet rs = query.executeQuery();
            ) {
                cacheEntities();

                while (rs.next()) {
                    try {
                        int id = rs.getInt("id");
                        String world = rs.getString("world");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        UUID uuid = rs.getString("uuid") != null ? UUID.fromString(rs.getString("uuid")) : null;
                        String type = rs.getString("type");
                        int data = rs.getInt("data");
                        boolean state = rs.getBoolean("state");
                        int recordType = rs.getInt("record_type");

                        if (world.equals(getArena().getWorld())) {
                            JsonObject stateSerial = null;
                            if (state) {
                                if (arenaSection != null) {
                                    if (arenaSection.has("" + id)) {
                                        stateSerial = arenaSection.getAsJsonObject("" + id);
                                    } else {
                                        CommonCore.logVerbose("Rollback record with ID " + id + " was marked as having "
                                                + "state, but no corresponding serial was found");
                                    }
                                }
                            }

                            switch (recordType) {
                                case RECORD_TYPE_BLOCK_CHANGED:
                                    rollbackBlock(id, new Location3D(world, x, y, z), type, data, stateSerial);
                                    break;
                                case RECORD_TYPE_ENTITY_CREATED:
                                    rollbackEntityCreation(id, uuid);
                                    break;
                                case RECORD_TYPE_ENTITY_CHANGED:
                                    rollbackEntityChange(id, uuid, new Location3D(world, x, y, z), type, stateSerial);
                                    break;
                                default:
                                    CommonCore.logWarning("Invalid rollback record type at ID " + id);
                            }
                        } else {
                            CommonCore.logVerbose("Rollback record with ID " + id + " in arena " + getArena().getId()
                                    + " has a mismtching world name - refusing to roll back");
                        }
                    } catch (SQLException ex) {
                        CommonCore.logSevere("Failed to read rollback record in arena " + getArena().getId());
                        ex.printStackTrace();
                    }
                }
                drop.executeUpdate();
            }
            if (json != null) {
                json.remove(getArena().getId());
                try (FileWriter writer = new FileWriter(stateStore)) {
                    writer.write(new Gson().toJson(json));
                } catch (IOException ex) {
                    CommonCore.logSevere("Failed to wipe rollback state store! This might hurt...");
                    ex.printStackTrace();
                }
            }
        } else {
            throw new IllegalArgumentException("Rollback store does not exist");
        }
    }

    public abstract void rollbackBlock(int id, Location3D location, String type, int data, JsonObject stateSerial);

    public abstract void rollbackEntityCreation(int id, UUID uuid);

    public abstract void rollbackEntityChange(int id, UUID uuid, Location3D location, String type,
                                              JsonObject stateSerial);

    public abstract void cacheEntities();

}
