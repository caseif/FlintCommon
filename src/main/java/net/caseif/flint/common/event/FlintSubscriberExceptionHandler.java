package net.caseif.flint.common.event;

import net.caseif.flint.common.CommonCore;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Singleton exception handler class for Flint events.
 *
 * <p>This is basically only necessary because Google screwed up their exception
 * logging. See https://github.com/google/guava/issues/2093 for more info.</p>
 *
 * @author Max Roncacé
 */
public class FlintSubscriberExceptionHandler implements SubscriberExceptionHandler {

    private static FlintSubscriberExceptionHandler INSTANCE;

    private FlintSubscriberExceptionHandler() {
    }

    /**
     * Gets an instance of this singleton.
     *
     * @return An instance of the singleton.
     */
    public static FlintSubscriberExceptionHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlintSubscriberExceptionHandler();
        }
        return INSTANCE;
    }

    /**
     * Deinitializes this singleton class.
     */
    public static void deinitialize() {
        INSTANCE = null;
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        CommonCore.logSevere("Failed to dispatch event " + context.getSubscriber() + " to "
                + context.getSubscriberMethod());
        exception.printStackTrace();
    }

}
