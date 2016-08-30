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
package net.caseif.flint.common.metadata.persist;

import net.caseif.flint.common.event.internal.metadata.PersistableMetadataMutateEvent;
import net.caseif.flint.common.metadata.CommonMetadata;
import net.caseif.flint.common.serialization.SimpleMetadataSerializer;
import net.caseif.flint.metadata.persist.PersistentMetadata;
import net.caseif.flint.serialization.Serializer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link PersistentMetadata}.
 *
 * @author Max Roncacé
 */
public class CommonPersistentMetadata extends CommonMetadata implements PersistentMetadata {

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
    public static final String PRIMITIVE_PREFIX = "PRIM_";

    private static final SimpleMetadataSerializer SIMPLE_SERIALIZER = new SimpleMetadataSerializer<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) throws ClassCastException {
        Optional<Object> value = super.get(key);
        if (value.isPresent() && value.get() instanceof String) {
            return Optional.of(new SimpleMetadataSerializer<T>().deserialize(value.toString()));
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
        set(key, SIMPLE_SERIALIZER);
    }

    @Override
    public void set(String key, byte value) {
        set(key, SIMPLE_SERIALIZER);
    }

    @Override
    public void set(String key, short value) {
        set(key, SIMPLE_SERIALIZER);
    }

    @Override
    public void set(String key, char value) {
        set(key, SIMPLE_SERIALIZER);
    }

    @Override
    public void set(String key, int value) {
        set(key, SIMPLE_SERIALIZER);
    }

    @Override
    public void set(String key, long value) {
        set(key, SIMPLE_SERIALIZER);
    }

    @Override
    public void set(String key, float value) {
        set(key, SIMPLE_SERIALIZER);
    }

    @Override
    public void set(String key, double value) {
        set(key, SIMPLE_SERIALIZER);
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
    public PersistentMetadata createStructure(String key) {
        Preconditions.checkArgument(!data.containsKey(key), "Metadata key " + key + " is already set");
        PersistentMetadata structure = new CommonPersistentMetadata();
        data.put(key, structure);
        postEvent();
        return structure;
    }

    @Override
    public ImmutableCollection<String> values() {
        return ImmutableList.copyOf(Collections2.transform(data.values(), new Function<Object, String>() {
            @Override
            public String apply(Object input) {
                return (String) input;
            }
        }));
    }

    @Override
    public ImmutableCollection<Object> values(final Function<String, Object> transformer) {
        return ImmutableList.copyOf(Collections2.transform(data.values(), new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return transformer.apply((String) input);
            }
        }));
    }

    @Override
    public ImmutableSet<? extends Map.Entry<String, String>> entrySet() {
        return ImmutableSet.copyOf(Collections2.transform(data.entrySet(),
                new Function<Map.Entry<String, ?>, AbstractMap.SimpleImmutableEntry<String, String>>() {
                    @Override
                    public AbstractMap.SimpleImmutableEntry<String, String> apply(Map.Entry<String, ?> input) {
                        return new AbstractMap.SimpleImmutableEntry<>(input.getKey(), (String) input.getValue());
                    }
                }
        ));
    }

    // this is even more disgusting than the one in CommonMetadata
    @Override
    public ImmutableSet<? extends Map.Entry<String, Object>> entrySet(final Function<String, Object> transformer) {
        return ImmutableSet.copyOf(Collections2.transform(data.entrySet(),
                new Function<Map.Entry<String, Object>, AbstractMap.SimpleImmutableEntry<String, Object>>() {
                    @Override
                    public AbstractMap.SimpleImmutableEntry<String, Object> apply(Map.Entry<String, Object> input) {
                        Object attempt = SIMPLE_SERIALIZER.deserialize(input.getValue().toString());
                        if (attempt instanceof String) {
                            attempt = transformer.apply((String) attempt);
                        }
                        return new AbstractMap.SimpleImmutableEntry<>(input.getKey(), attempt);
                    }
                }
        ));
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
