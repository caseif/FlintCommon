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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.arena.CommonArena;
import net.caseif.flint.common.util.file.CommonDataFiles;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public abstract class CommonRollbackAgent implements IRollbackAgent {

    private static final String SQLITE_PROTOCOL = "jdbc:sqlite:";
    private static final Properties SQL_QUERIES = new Properties();

    private final CommonArena arena;

    private final File rollbackStore;
    private final File stateStore;

    protected CommonRollbackAgent(CommonArena arena) {
        this.arena = arena;

        rollbackStore = CommonDataFiles.ROLLBACK_STORE.getFile(getArena().getMinigame());
        stateStore = CommonDataFiles.ROLLBACK_STATE_STORE.getFile(getArena().getMinigame());

        initializeStateStore();
    }

    static {
        try (InputStream is = CommonRollbackAgent.class.getResourceAsStream("/sql-queries.properties")) {
            SQL_QUERIES.load(is);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load SQL query strings", ex);
        }
    }

    /**
     * Returns the {@link CommonArena} associated with this
     * {@link CommonRollbackAgent}.
     *
     * @return The {@link CommonArena} associated with this
     * {@link CommonRollbackAgent}.
     */
    public CommonArena getArena() {
        return arena;
    }

    /**
     * Creates a rollback database for the arena backing this
     * {@link CommonRollbackAgent}.
     *
     * @throws IOException If an exception occurs while creating the database
     *     file
     * @throws SQLException If an exception occurs while manipulating the
     *     database
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void createRollbackDatabase() throws IOException, SQLException {
        if (!rollbackStore.exists()) {
            rollbackStore.createNewFile();
        }
        if (!stateStore.exists()) {
            stateStore.createNewFile();
        }

        try (Connection conn = DriverManager.getConnection(SQLITE_PROTOCOL + rollbackStore.getAbsolutePath())) {
            try (PreparedStatement st = conn.prepareStatement(SQL_QUERIES.getProperty("create-rollback-table")
                    .replace("{table}", getArena().getId()))
            ) {
                st.executeUpdate();
            }
        }
    }

    @Override
    public Map<Integer, String> loadStateMap() throws IOException {
        Map<Integer, String> stateMap = new HashMap<>();

        JsonObject json = new JsonParser().parse(new FileReader(stateStore)).getAsJsonObject();

        if (!json.has(getArena().getId()) || !json.get(getArena().getId()).isJsonObject()) {
            throw new IOException("Cannot load rollback states for arena " + getArena().getId());
        }

        JsonObject arena = json.getAsJsonObject(getArena().getId());

        for (Map.Entry<String, JsonElement> entry : arena.entrySet()) {
            int id = -1;
            try {
                id = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException ex) {
                CommonCore.logWarning("Cannot load rollback state with ID " + entry.getKey() + " - key is not an int");
            }

            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString()) {
                CommonCore.logWarning("Cannot load rollback state with ID " + id + " - not a string");
                continue;
            }

            stateMap.put(id, entry.getValue().getAsString());
        }

        return stateMap;
    }

    @Override
    public void logChange(RollbackRecord record)
            throws IOException, SQLException {
        String world = record.getLocation().getWorld().isPresent()
                ? record.getLocation().getWorld().get()
                : arena.getWorld();
        Preconditions.checkNotNull(record.getLocation(), "Location required for all record types");
        switch (record.getType()) {
            case BLOCK_CHANGE:
                Preconditions.checkNotNull(record.getTypeData(), "Type required for BLOCK_CHANGED record type");
                break;
            case ENTITY_CREATION:
                Preconditions.checkNotNull(record.getUuid(), "UUID required for ENTITY_CREATED record type");
                break;
            case ENTITY_CHANGE:
                Preconditions.checkNotNull(record.getTypeData(), "Type required for ENTITY_CHANGED record type");
                Preconditions.checkNotNull(record.getStateSerial(), "State required for ENTITY_CHANGED record type");
                break;
            default:
                throw new AssertionError("Undefined record type");
        }
        if (!rollbackStore.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rollbackStore.createNewFile();
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + rollbackStore.getPath())) {
            String querySql;
            switch (record.getType()) {
                case BLOCK_CHANGE:
                    querySql = SQL_QUERIES.getProperty("query-by-location")
                            .replace("{world}", "\"" + world + "\"")
                            .replace("{x}", "" + record.getLocation().getX())
                            .replace("{y}", "" + record.getLocation().getY())
                            .replace("{z}", "" + record.getLocation().getZ());
                    break;
                case ENTITY_CHANGE:
                    querySql = SQL_QUERIES.getProperty("query-by-uuid")
                            .replace("{uuid}", "\"" + record.getUuid().toString() + "\"");
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
            switch (record.getType()) {
                case BLOCK_CHANGE:
                    updateSql = SQL_QUERIES.getProperty("insert-block-rollback-record")
                            .replace("{world}", world)
                            .replace("{x}", "" + record.getLocation().getX())
                            .replace("{y}", "" + record.getLocation().getY())
                            .replace("{z}", "" + record.getLocation().getZ())
                            .replace("{type}", record.getTypeData())
                            .replace("{data}", "" + record.getData());
                    break;
                case ENTITY_CREATION:
                    updateSql = SQL_QUERIES.getProperty("insert-entity-created-rollback-record")
                            .replace("{world}", world)
                            .replace("{uuid}", record.getUuid().toString());
                    break;
                case ENTITY_CHANGE:
                    updateSql = SQL_QUERIES.getProperty("insert-entity-changed-rollback-record")
                            .replace("{world}", world)
                            .replace("{x}", "" + record.getLocation().getX())
                            .replace("{y}", "" + record.getLocation().getY())
                            .replace("{z}", "" + record.getLocation().getZ())
                            .replace("{uuid}", record.getUuid().toString())
                            .replace("{type}", record.getTypeData());
                    break;
                default:
                    throw new AssertionError("Inconsistency detected in method: recordType is in an illegal state. "
                            + "Report this immediately.");
            }
            if (updateSql != null) {
                // replace non-negotiable values
                updateSql = updateSql
                        .replace("{table}", getArena().getId())
                        .replace("{state}", "" + (record.getStateSerial() != null ? 1 : 0))
                        .replace("{record_type}", "" + record.getType().ordinal());
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
            if (record.getStateSerial() != null) {
                saveStateSerial(id, record.getStateSerial());
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void popRollbacks() throws IOException, SQLException {
        if (rollbackStore.exists()) {
            Map<Integer, String> stateMap = loadStateMap();

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
                        RollbackRecord.Type recordType = RollbackRecord.Type.values()[rs.getInt("record_type")];

                        if (world.equals(getArena().getWorld())) {
                            String stateSerial = stateMap.get(id);
                            if (state && stateSerial == null) {
                                CommonCore.logVerbose("Rollback record with ID " + id + " was marked as having "
                                        + "state, but no corresponding serial was found");
                            }

                            switch (recordType) {
                                case BLOCK_CHANGE:
                                    rollbackBlock(id, new Location3D(world, x, y, z), type, data, stateSerial);
                                    break;
                                case ENTITY_CREATION:
                                    rollbackEntityCreation(id, uuid);
                                    break;
                                case ENTITY_CHANGE:
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
            clearStateStore();
        } else {
            throw new IllegalArgumentException("Rollback store does not exist");
        }
    }

    @Override
    public void clearStateStore() throws IOException {
        JsonObject json = new JsonParser().parse(new FileReader(stateStore)).getAsJsonObject();

        if (!json.has(getArena().getId()) || !json.get(getArena().getId()).isJsonObject()) {
            CommonCore.logWarning("State store clear requested, but arena was not present");
            return;
        }

        json.remove(getArena().getId());
        json.add(getArena().getId(), new JsonObject());
        saveState(json);
    }

    @Override
    public void initializeStateStore() {
        try {
            if (!stateStore.exists()) {
                //noinspection ResultOfMethodCallIgnored
                stateStore.createNewFile();
            }
            JsonElement json = new JsonParser().parse(new FileReader(stateStore));
            if (json.isJsonNull()) {
                json = new JsonObject();
            }
            json.getAsJsonObject().add(getArena().getId(), new JsonObject());
            saveState(json.getAsJsonObject());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to intialize state store for arena " + arena.getId(), ex);
        }
    }

    @Override
    public void saveStateSerial(int id, String serial) throws IOException {
        JsonObject json = new JsonParser().parse(new FileReader(stateStore)).getAsJsonObject();

        if (!json.has(getArena().getId())) {
            initializeStateStore();
            saveStateSerial(id, serial); // i'm a bad person
            return;
        }

        json.get(getArena().getId()).getAsJsonObject().addProperty(id + "", serial);
        saveState(json);
    }

    private void saveState(JsonObject json) throws IOException {
        try (FileWriter writer = new FileWriter(stateStore)) {
            writer.write(new Gson().toJson(json));
        }
    }

    protected static List<Arena> checkChangeAtLocation(Location3D location) {
        List<Arena> arenas = new ArrayList<>();
        for (Minigame mg : CommonCore.getMinigames().values()) {
            for (Arena arena : mg.getArenas()) {
                if (arena.getRound().isPresent() && arena.getBoundary().contains(location)) {
                    arenas.add(arena);
                }
            }
        }
        return arenas;
    }

}
