package net.caseif.flint.common.util.helper;

import static net.caseif.flint.common.util.helper.JsonSerializer.deserializeLocation;
import static net.caseif.flint.common.util.helper.JsonSerializer.serializeLocation;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.util.file.CommonDataFiles;
import net.caseif.flint.util.physical.Location3D;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

public class CommonPlayerHelper {

    public static void setOfflineFlag(UUID player) {
        try {
            File playerStore = CommonDataFiles.OFFLINE_PLAYER_STORE.getFile();
            boolean isNew = false;
            if (!playerStore.exists()) {
                playerStore.createNewFile();
                isNew = true;
            }

            JsonArray json;
            try (FileReader reader = new FileReader(playerStore)) {
                json = isNew ? new JsonArray() : (JsonArray) new JsonParser().parse(reader);
                json.add(new JsonPrimitive(player.toString()));
            }
            try (FileWriter writer = new FileWriter(playerStore)) {
                writer.write(json.toString());
            }
        } catch (IOException ex) {
            CommonCore.logSevere("Failed to mark player as offline!");
            ex.printStackTrace();
        }
    }

    public static boolean checkOfflineFlag(UUID player) {
        try {
            File playerStore = CommonDataFiles.OFFLINE_PLAYER_STORE.getFile();
            if (!playerStore.exists()) {
                return false;
            }
            JsonArray json;
            try (FileReader reader = new FileReader(playerStore)) {
                json = new JsonParser().parse(reader).getAsJsonArray();
            }
            JsonArray newArray = new JsonArray();

            Iterator<JsonElement> it = json.iterator();
            boolean found = false;
            while (it.hasNext()) {
                JsonElement el = it.next();
                if (el.getAsString().equals(player.toString())) {
                    found = true;
                } else {
                    newArray.add(el);
                }
            }

            if (found) {
                try (FileWriter writer = new FileWriter(playerStore)) {
                    writer.write(newArray.toString());
                }
                return true;
            }
            return false;
        } catch (IOException ex) {
            CommonCore.logSevere("Failed to mark player as offline!");
            throw new RuntimeException(ex);
        }
    }

    /**
     * Stores the given {@link Location3D} to persistent storage, associated
     * with the given player.
     *
     * @param player The {@link UUID} of the player to store a
     *     {@link Location3D} for
     * @param location The {@link Location3D} to store
     * @throws IOException If an exception occurs while saving to disk
     */
    public static void storeLocation(UUID player, Location3D location) throws IOException {
        File store = CommonDataFiles.PLAYER_LOCATION_STORE.getFile();
        JsonObject json;
        if (store.exists()) {
            try (FileReader reader = new FileReader(store)) {
                json = new JsonParser().parse(reader).getAsJsonObject();
            }
        } else {
            json = new JsonObject();
        }

        json.add(player.toString(), serializeLocation(location));

        try (FileWriter writer = new FileWriter(store)) {
            writer.append(json.toString());
        }
    }

    /**
     * Gets the given player's stored location from persistent storage and pops
     * it if found.
     *
     * @param player The {@link UUID} of the player to load the location of
     * @return The stored {@link Location3D}
     * @throws IllegalArgumentException If an error occurs during
     *     deserialization of the stored location
     * @throws IOException If an exception occurs while saving to disk
     */
    public static Optional<Location3D> getReturnLocation(UUID player)
            throws IllegalArgumentException, IOException {
        File store = CommonDataFiles.PLAYER_LOCATION_STORE.getFile();
        JsonObject json;
        if (store.exists()) {
            try (FileReader reader = new FileReader(store)) {
                json = new JsonParser().parse(reader).getAsJsonObject();
            }
        } else {
            return Optional.absent();
        }

        if (json.has(player.toString())) {
            Location3D l3d = deserializeLocation(json.getAsJsonObject(player.toString()));
            json.remove(player.toString());

            try (FileWriter writer = new FileWriter(store)) {
                writer.append(json.toString());
            }

            if (!l3d.getWorld().isPresent()) {
                throw new IllegalArgumentException("World not present in stored location of player " + player);
            }

            return Optional.of(l3d);
        } else {
            return Optional.absent();
        }
    }

}
