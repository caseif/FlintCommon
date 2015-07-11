package net.caseif.flint.common.event.service;

import net.caseif.flint.Minigame;
import net.caseif.flint.event.FlintEvent;
import net.caseif.flint.event.service.FlintListener;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Represents a listener for a single {@link FlintEvent}.
 *
 * @author Max Roncacé
 */
class ListenerNode {

    private Minigame minigame;
    private Method method;
    private Class<? extends FlintEvent> eventClass;

    ListenerNode(Minigame minigame, Method method, Class<? extends FlintEvent> eventClass) {
        assert Modifier.isStatic(method.getModifiers());
        assert method.getAnnotation(FlintListener.class) != null;
        this.minigame = minigame;
        this.method = method;
        this.eventClass = eventClass;
    }

    Minigame getMinigame() {
        return minigame;
    }

    Method getMethod() {
        return method;
    }

    Class<? extends FlintEvent> getEventClass() {
        return eventClass;
    }

}
