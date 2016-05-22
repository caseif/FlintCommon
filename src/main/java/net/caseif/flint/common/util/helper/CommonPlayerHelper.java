package net.caseif.flint.common.util.helper;

import net.caseif.flint.common.CommonCore;
import net.caseif.flint.common.util.file.CommonDataFiles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

}
