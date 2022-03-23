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

package net.caseif.flint.common.util.factory;

import static com.google.common.base.Preconditions.checkState;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.util.builder.Buildable;
import net.caseif.flint.util.builder.Builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class FactoryRegistry {

    private static final Map<Class<?>, Factory<?>> factoryMap = new HashMap<>();

    public static <T, F extends Factory<? extends T>> void registerFactory(Class<T> target,
                                                                    F factory) {
        checkState(!factoryMap.containsKey(target),
                "Builder for class " + target.getName() + " has already been registered");
        factoryMap.put(target, factory);
    }

    @SuppressWarnings("unchecked")
    public static <T, F extends Factory<T>> F getFactory(Class<T> clazz)
            throws IllegalStateException {
        checkState(factoryMap.containsKey(clazz), "No Factory registration available for class " + clazz.getName());
        return (F) factoryMap.get(clazz);
    }

}
