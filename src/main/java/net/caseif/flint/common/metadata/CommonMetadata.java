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
package net.caseif.flint.common.metadata;

import net.caseif.flint.metadata.Metadata;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@link Metadata}.
 *
 * @author Max Roncacé
 */
public class CommonMetadata implements Metadata {

    protected Map<String, Object> data = new HashMap<>();

    private static final EventBus EVENT_BUS = new EventBus();

    protected CommonMetadata() {
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
    public void clear() {
        data.clear();
    }

    public static EventBus getEventBus() {
        return EVENT_BUS;
    }

}
