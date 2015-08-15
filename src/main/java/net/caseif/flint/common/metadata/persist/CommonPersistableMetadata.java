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
package net.caseif.flint.common.metadata.persist;

import net.caseif.flint.common.event.internal.metadata.PersistableMetadataMutateEvent;
import net.caseif.flint.common.metadata.CommonMetadata;
import net.caseif.flint.metadata.persist.PersistableMetadata;
import net.caseif.flint.serialization.Serializer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Implements {@link PersistableMetadata}.
 *
 * @author Max Roncacé
 */
public class CommonPersistableMetadata extends CommonMetadata implements PersistableMetadata {

    /*
     * Primitive values stored persistently are prefixed with a string to denote
     * that they're actually primitives.
     *
     * The reasoning behind this is that otherwise, we'd have no real way of
     * knowing that they're primitives and they'd just be loaded as strings with
     * everything else (e.g. String i = "15").
     *
     * Furthermore, the internal character representation is also part of the
     * respective assigned prefixes.
     */
    private static final String PRIMITIVE_PREFIX = "PRIM_";

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) throws ClassCastException {
        Optional<Object> value = super.get(key);
        if (value.isPresent() && value.get() instanceof String) {
            String str = (String)value.get();
            if (str.startsWith(PRIMITIVE_PREFIX)) {
                switch (str.charAt(PRIMITIVE_PREFIX.length())) {
                    case 'Z':
                        return Optional.of((T)Boolean.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)));
                    case 'B':
                        return Optional.of((T)Byte.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)));
                    case 'S':
                        return Optional.of((T)Short.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)));
                    case 'C':
                        return Optional.of((T)Character.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)
                                .charAt(0)));
                    case 'I':
                        return Optional.of((T)Integer.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)));
                    case 'J':
                        return Optional.of((T)Long.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)));
                    case 'F':
                        return Optional.of((T)Float.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)));
                    case 'D':
                        return Optional.of((T)Double.valueOf(str.substring(PRIMITIVE_PREFIX.length() + 2)));
                    default: // continue to end
                }
            }
        }
        return (Optional<T>)value;
    }

    @Override
    public <T> T get(String key, Serializer<T> serializer) throws ClassCastException, IllegalArgumentException {
        Preconditions.checkArgument(data.get(key) instanceof String, "Metadata key " + key
                + " is not associated with a string");
        return serializer.deserialize((String)data.get(key));
    }

    @Override
    public void set(String key, Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Generic set operation not permitted for PersistableMetadata objects");
    }

    @Override
    public void set(String key, String value) {
        data.put(key, value);
        postEvent();
    }

    @Override
    public void set(String key, boolean value) {
        set(key, PRIMITIVE_PREFIX + "Z_" + value);
    }

    @Override
    public void set(String key, byte value) {
        set(key, PRIMITIVE_PREFIX + "B_" + value);
    }

    @Override
    public void set(String key, short value) {
        set(key, PRIMITIVE_PREFIX + "S_" + value);
    }

    @Override
    public void set(String key, char value) {
        set(key, PRIMITIVE_PREFIX + "C_" + value);
    }

    @Override
    public void set(String key, int value) {
        set(key, PRIMITIVE_PREFIX + "I_" + value);
    }

    @Override
    public void set(String key, long value) {
        set(key, PRIMITIVE_PREFIX + "J_" + value);
    }

    @Override
    public void set(String key, float value) {
        set(key, PRIMITIVE_PREFIX + "F_" + value);
    }

    @Override
    public void set(String key, double value) {
        set(key, PRIMITIVE_PREFIX + "D_" + value);
    }

    @Override
    public <T> void set(String key, T value, Serializer<T> serializer) {
        set(key, serializer.serialize(value));
    }

    @Override
    public void set(String key, List<String> value) {
        data.put(key, value);
        postEvent();
    }

    @Override
    public <T> void set(String key, List<T> value, final Serializer<T> serializer) {
        List<String> transformed = Lists.newArrayList(Collections2.transform(value, new Function<T, String>() {
            @Override
            public String apply(T input) {
                return serializer.serialize(input);
            }
        }));
        set(key, transformed);
    }

    @Override
    public PersistableMetadata createStructure(String key) {
        Preconditions.checkArgument(!data.containsKey(key), "Metadata key " + key + " is already set");
        PersistableMetadata structure = new CommonPersistableMetadata();
        data.put(key, structure);
        postEvent();
        return structure;
    }

    @Override
    public boolean remove(String key) {
        boolean result = super.remove(key);
        if (result) {
            postEvent();
        }
        return result;
    }

    public void clear() {
        super.clear();
        postEvent();
    }

    /**
     * Convenience method for posting a {@link PersistableMetadataMutateEvent}.
     */
    private void postEvent() {
        getEventBus().post(new PersistableMetadataMutateEvent(this));
    }

}
