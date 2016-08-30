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
package net.caseif.flint.common.serialization;

import static net.caseif.flint.common.metadata.persist.CommonPersistentMetadata.PRIMITIVE_PREFIX;

import net.caseif.flint.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic metadata deserializer implementation.
 */
public class SimpleMetadataSerializer<T> implements Serializer<T> {

    private static final Map<Class<?>, Character> PRIM_PREFIX_MAP = new HashMap<>();

    static {
        PRIM_PREFIX_MAP.put(Boolean.class, 'Z');
        PRIM_PREFIX_MAP.put(Byte.class, 'B');
        PRIM_PREFIX_MAP.put(Short.class, 'S');
        PRIM_PREFIX_MAP.put(Character.class, 'C');
        PRIM_PREFIX_MAP.put(Integer.class, 'I');
        PRIM_PREFIX_MAP.put(Long.class, 'J');
        PRIM_PREFIX_MAP.put(Float.class, 'F');
        PRIM_PREFIX_MAP.put(Double.class, 'D');
    }

    @Override
    public String serialize(Object object) {
        StringBuilder sb = new StringBuilder();
        sb.append(PRIMITIVE_PREFIX);
        if (PRIM_PREFIX_MAP.containsKey(object.getClass())) {
            sb.append(PRIM_PREFIX_MAP.get(object.getClass())).append('_');
        }
        sb.append(object);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String str) {
        if (str.startsWith(PRIMITIVE_PREFIX)) {
            switch (str.charAt(PRIMITIVE_PREFIX.length())) {
                case 'Z':
                    return (T) Boolean.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2));
                case 'B':
                    return (T) Byte.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2));
                case 'S':
                    return (T) Short.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2));
                case 'C':
                    return (T) Character.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2).charAt(0));
                case 'I':
                    return (T) Integer.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2));
                case 'J':
                    return (T) Long.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2));
                case 'F':
                    return (T) Float.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2));
                case 'D':
                    return (T) Double.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2));
            }
        }
        return (T) str;
    }
}
