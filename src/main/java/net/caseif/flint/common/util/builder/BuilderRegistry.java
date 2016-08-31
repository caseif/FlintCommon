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

package net.caseif.flint.common.util.builder;

import static com.google.common.base.Preconditions.checkState;

import net.caseif.flint.common.CommonCore;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.util.builder.Buildable;
import net.caseif.flint.util.builder.Builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

// the generics here took me about an hour and probably made my roommates hate me. totally worth it though.
public class BuilderRegistry {

    private static final BuilderRegistry INSTANCE = new BuilderRegistry();

    private final Map<Class<? extends Buildable<?>>, Constructor<? extends Builder<?>>> ctorMap = new HashMap<>();

    public static BuilderRegistry instance() {
        return INSTANCE;
    }

    public <T extends Buildable<? super U>, U extends Builder<? extends T>> void registerBuilder(Class<T> target,
                                                                                                 Class<U> builder) {
        checkState(!ctorMap.containsKey(target),
                "Builder for class " + target.getName() + " has already been registered");
        if (!Modifier.isStatic(builder.getModifiers())) {
            CommonCore.logSevere("Cannot register non-static class " + builder.getName() + " as Builder");
            return;
        }
        try {
            Constructor<U> ctor = builder.getConstructor(Minigame.class);
            ctorMap.put(target, ctor);
        } catch (NoSuchMethodException ex) {
            CommonCore.logSevere("Failed to find applicable constructor for Builder class " + builder.getName()
                    + " - skipping registration");
            ex.printStackTrace();
        }
    }

    public <T extends Buildable<? super U>, U extends Builder<? extends T>> U createBuilder(Class<T> clazz, Minigame mg)
            throws IllegalStateException {
        checkState(ctorMap.containsKey(clazz), "No Builder registration available for class " + clazz.getName());
        try {
            return (U) ctorMap.get(clazz).newInstance(mg);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Failed to instantiate Builder for class " + clazz.getName());
        }
    }

}
