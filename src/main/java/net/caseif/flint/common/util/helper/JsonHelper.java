package net.caseif.flint.common.util.helper;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

public class JsonHelper {

    public static JsonObject readOrCreateJson(File file) throws IOException {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JsonElement el = new JsonParser().parse(reader);
                return el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
            }
        } else {
            Files.createFile(file.toPath());
            return new JsonObject();
        }
    }

    public static Optional<JsonObject> readJson(File file) throws IllegalArgumentException, IOException {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JsonElement el = new JsonParser().parse(reader);
                if (el.isJsonObject()) {
                    return Optional.of(el.getAsJsonObject());
                }
            }
        }
        return Optional.absent();
    }

}
