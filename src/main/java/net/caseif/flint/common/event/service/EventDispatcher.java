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
