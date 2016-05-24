package net.caseif.flint.common.util.helper;

import net.caseif.flint.metadata.Metadata;
import net.caseif.flint.metadata.persist.PersistentMetadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class MetadataHelper {

    /**
     * Stores the given {@link PersistentMetadata} recursively into the given
     * {@link JsonObject}.
     *
     * @param json The {@link JsonObject} to store to
     * @param data The {@link Metadata} to store
     */
    public static void storeMetadata(JsonObject json, PersistentMetadata data) {
        for (String key : data.getAllKeys()) {
            if (data.get(key).get() instanceof String) {
                json.addProperty(key, (String) data.get(key).get());
            } else if (data.get(key).get() instanceof PersistentMetadata) {
                JsonObject subsection = new JsonObject();
                storeMetadata(subsection, (PersistentMetadata) data.get(key).get());
                json.add(key, subsection);
            }
        }
    }

    /**
     * Loads data recursively from the given {@link JsonObject} into the given
     * {@link PersistentMetadata}.
     *
     * <p>If {@code parent} is {@code null}, it will default to this arena's
     * global {@link PersistentMetadata}.</p>
     *
     * @param json The {@link JsonObject} to load data from
     * @param parent The {@link PersistentMetadata} object ot load data into
     */
    public static void loadMetadata(JsonObject json, PersistentMetadata parent) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (json.get(entry.getKey()).isJsonObject()) {
                loadMetadata(json.getAsJsonObject(entry.getKey()), parent.createStructure(entry.getKey()));
            } else if (json.get(entry.getKey()).isJsonPrimitive()) {
                parent.set(entry.getKey(), json.get(entry.getKey()).getAsString());
            }
        }
    }

}
