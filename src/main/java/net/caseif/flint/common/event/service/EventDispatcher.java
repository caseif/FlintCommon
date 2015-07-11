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
package net.caseif.flint.common.event.service;

import net.caseif.flint.Minigame;
import net.caseif.flint.common.CommonCore;
import net.caseif.flint.event.FlintEvent;
import net.caseif.flint.event.service.FlintListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for dispatching Flint events.
 *
 * @author Max Roncacé
 */
public class EventDispatcher {

    private static Map<Class<? extends FlintEvent>, List<ListenerNode>> listenerNodes = new HashMap<>();
    private static List<Class<?>> registeredClasses = new ArrayList<>();

    public static void addEventListener(Minigame minigame, Class<?> listenerClass) {
        if (registeredClasses.contains(listenerClass)) {
            CommonCore.logWarning("Plugin " + minigame.getPlugin() + " attempted to register " + listenerClass + " as "
                    + "a Flint listener more than once");
            return;
        }
        int listenerCount = 0;
        for (Method m : listenerClass.getDeclaredMethods()) {
            if (m.getAnnotation(FlintListener.class) != null) {
                if (Modifier.isStatic(m.getModifiers())) {
                    if (m.getParameterTypes().length == 1) {
                        if (FlintEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                            @SuppressWarnings("unchecked")
                            Class<? extends FlintEvent> eventClass
                                    = (Class<? extends FlintEvent>)m.getParameterTypes()[0];
                            List<ListenerNode> nodes = listenerNodes.get(eventClass);
                            if (nodes == null) {
                                listenerNodes.put(eventClass, nodes = new ArrayList<>());
                            }
                            nodes.add(new ListenerNode(minigame, m, eventClass));
                            listenerCount++;
                        } else {
                            CommonCore.logWarning("Plugin " + minigame.getPlugin() + " attempted to register method "
                                    + listenerClass.getName() + "#" + m.getName() + " with invalid parameter type "
                                    + m.getParameterTypes()[0].getName() + "as a Flint event listener");
                        }
                    } else {
                        CommonCore.logWarning("Plugin " + minigame.getPlugin() + " attempted to register method "
                                + listenerClass.getName() + "#" + m.getName() + " with invalid parameter count as a "
                                + "Flint event listener");
                    }
                } else {
                    CommonCore.logWarning("Plugin " + minigame.getPlugin() + " attempted to register instance method "
                            + listenerClass.getName() + "#" + m.getName() + " as a Flint event listener");
                }
            }
        }
        if (listenerCount == 0) {
            CommonCore.logWarning("Probable bug: Plugin " + minigame.getPlugin() + " attempted to register "
                    + listenerClass.getName() + " as a Flint event listener, but class defines no listener methods");
        }
        registeredClasses.add(listenerClass);
    }

    public static void dispatchEvent(FlintEvent event) {
        for (Class<? extends FlintEvent> eventClass : listenerNodes.keySet()) {
            if (eventClass.isAssignableFrom(event.getClass())) {
                for (ListenerNode node : listenerNodes.get(eventClass)) {
                    if (node.getMinigame() == event.getMinigame()) {
                        try {
                            node.getMethod().invoke(null, event);
                        } catch (InvocationTargetException | IllegalAccessException ex) {
                            ex.printStackTrace();
                            CommonCore.logSevere("Failed to dispatch " + event.getClass().getSimpleName() + " to "
                                    + event.getMinigame().getPlugin());
                        }
                    }
                }
            }
        }
    }

    private static void dispatchEvent(FlintEvent event, ListenerNode node) throws InvocationTargetException,
            IllegalAccessException {
        node.getMethod().invoke(null, event);
    }

}
