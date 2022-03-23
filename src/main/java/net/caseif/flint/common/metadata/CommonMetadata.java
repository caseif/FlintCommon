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

package net.caseif.flint.common.metadata;

import net.caseif.flint.metadata.Metadata;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link Metadata}.
 *
 * @author Max Roncacé
 */
public class CommonMetadata implements Metadata {

    private static final EventBus EVENT_BUS = new EventBus();

    protected final Map<String, Object> data = new HashMap<>();

    protected CommonMetadata() {
    }

    @Override
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public boolean has(String key) {
        return containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) throws ClassCastException {
        return Optional.fromNullable((T)data.get(key));
    }

    @Override
    public <T> void set(String key, T value) {
        data.put(key, value);
    }

    @Override
    public Metadata createStructure(String key) throws IllegalArgumentException {
        Preconditions.checkArgument(!data.containsKey(key), "Metadata key " + key + " is already set");
        Metadata structure = new CommonMetadata();
        data.put(key, structure);
        return structure;
    }

    @Override
    public boolean remove(String key) {
        Object result = data.remove(key);
        return result != null;
    }

    @Override
    public ImmutableSet<String> getAllKeys() {
        return ImmutableSet.copyOf(data.keySet());
    }

    @Override
    public ImmutableSet<String> keySet() {
        return ImmutableSet.copyOf(data.keySet());
    }

    @Override
    public ImmutableCollection<?> values() {
        return ImmutableList.copyOf(data.values());
    }

    // this is horrible
    @Override
    public ImmutableSet<? extends Map.Entry<String, ?>> entrySet() {
        return ImmutableSet.copyOf(Collections2.transform(data.entrySet(),
                new Function<Map.Entry<String, ?>, AbstractMap.SimpleImmutableEntry<String, ?>>() {
                    @Override
                    public AbstractMap.SimpleImmutableEntry<String, ?> apply(Map.Entry<String, ?> input) {
                        return new AbstractMap.SimpleImmutableEntry<String, Object>(input);
                    }
                }
        ));
    }

    @Override
    public void clear() {
        data.clear();
    }

    // strictly for metadata mutation events
    public static EventBus getEventBus() {
        return EVENT_BUS;
    }

}
