package com.wcs.vaadin.userinactivity;

import com.vaadin.server.VaadinSession;

/**
 * Default last action time registry.
 * Store in VaadinSession.
 */
public class DefaultLastActionRegistry implements LastActionRegistry {
    private final static String SESSION_KEY_LAST_ACTION_TIME
            = DefaultLastActionRegistry.class.getName() + ":last_client_action_time";

    private VaadinSession getSession() {
        return VaadinSession.getCurrent();
    }

    @Override
    public void registerLastActionTime() {
        //we are under uidl request handling, so session is locked.
        getSession().setAttribute(SESSION_KEY_LAST_ACTION_TIME, System.currentTimeMillis());
    }

    private long getLastActionTime() {
        return (Long) getSession().getAttribute(SESSION_KEY_LAST_ACTION_TIME);
    }

    @Override
    public int getRemainingSeconds(int timeoutSeconds) {
        int elapsedSeconds = (int) Math.round((double) (System.currentTimeMillis() - getLastActionTime()) / 1000);
        return timeoutSeconds - elapsedSeconds;
    }
}
