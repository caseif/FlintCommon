/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022, Max Roncace <me@caseif.net>
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

package net.caseif.flint.common.util.helper;

import net.caseif.flint.metadata.Metadata;
import net.caseif.flint.metadata.persist.PersistentMetadata;
import net.caseif.flint.util.physical.Location3D;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class JsonSerializer {

    private static String LOC_WORLD_KEY = "world";
    private static String LOC_X_KEY = "x";
    private static String LOC_Y_KEY = "y";
    private static String LOC_Z_KEY = "z";

    /**
     * Stores the given {@link PersistentMetadata} recursively into the given
     * {@link JsonObject}.
     *
     * @param json The {@link JsonObject} to store to
     * @param data The {@link Metadata} to store
     */
    public static void serializeMetadata(JsonObject json, PersistentMetadata data) {
        for (String key : data.getAllKeys()) {
            if (data.get(key).get() instanceof String) {
                json.addProperty(key, (String) data.get(key).get());
            } else if (data.get(key).get() instanceof PersistentMetadata) {
                JsonObject subsection = new JsonObject();
                serializeMetadata(subsection, (PersistentMetadata) data.get(key).get());
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
    public static void deserializeMetadata(JsonObject json, PersistentMetadata parent) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (json.get(entry.getKey()).isJsonObject()) {
                deserializeMetadata(json.getAsJsonObject(entry.getKey()), parent.createStructure(entry.getKey()));
            } else if (json.get(entry.getKey()).isJsonPrimitive()) {
                parent.set(entry.getKey(), json.get(entry.getKey()).getAsString());
            }
        }
    }

    /**
     * Serializes a location into the given {@link JsonObject}.
     *
     * @param location The {@link Location3D} to serialize
     * @return The serialized location data
     */
    public static JsonObject serializeLocation(Location3D location) {
        JsonObject json = new JsonObject();
        if (location.getWorld().isPresent()) {
            json.addProperty(LOC_WORLD_KEY, location.getWorld().get());
        }
        json.addProperty(LOC_X_KEY, location.getX());
        json.addProperty(LOC_Y_KEY, location.getY());
        json.addProperty(LOC_Z_KEY, location.getZ());
        return json;
    }

    /**
     * Deserializes a {@link JsonObject} into a {@link Location3D}.
     *
     * @param json The {@link JsonObject} containing the location data
     * @return The deserialized {@link Location3D}
     */
    public static Location3D deserializeLocation(JsonObject json) {
        String world = json.has(LOC_WORLD_KEY) ? json.get(LOC_WORLD_KEY).getAsString() : null;
        double x = json.get(LOC_X_KEY).getAsDouble();
        double y = json.get(LOC_Y_KEY).getAsDouble();
        double z = json.get(LOC_Z_KEY).getAsDouble();
        return new Location3D(world, x, y, z);
    }

}
