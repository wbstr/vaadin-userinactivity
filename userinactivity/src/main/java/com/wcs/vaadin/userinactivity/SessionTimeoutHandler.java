/*
 * Copyright 2014 kumm.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wcs.vaadin.userinactivity;

import com.vaadin.server.VaadinSession;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author kumm
 */
public class SessionTimeoutHandler implements Serializable {

    private int sessionTimeoutSeconds;
    private final UserInactivityExtension clientInactivityExtension;
    private final Set<SessionTimeoutListener> timeoutListeners = new HashSet<SessionTimeoutListener>();
    private final static String SESSION_KEY_LAST_ACTION_TIME
            = SessionTimeoutHandler.class.getName() + ":last_client_action_time";
    private final UserInactivityExtension.TimeoutListener inactivityTimeoutListener;
    private final UserInactivityExtension.ActionListener inactivityActionListener;
    private boolean running = false;

    SessionTimeoutHandler(UserInactivityExtension clientInactivityExtension) {
        this.clientInactivityExtension = clientInactivityExtension;
        inactivityTimeoutListener = new UserInactivityExtension.TimeoutListener() {

            @Override
            public void timeout() {
                onInactivityTimeout();
            }
        };
        inactivityActionListener = new UserInactivityExtension.ActionListener() {

            @Override
            public void action() {
                onUserAction();
            }
        };
    }

    public void addTimeoutListener(SessionTimeoutListener listener) {
        timeoutListeners.add(listener);
    }

    public void removeTimeoutListener(SessionTimeoutListener listener) {
        timeoutListeners.remove(listener);
    }

    private void fireTimeoutEvent() {
        for (SessionTimeoutListener timeoutListener : timeoutListeners) {
            timeoutListener.timeout();
        }
    }

    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public void reschedule() {
        if (!running) {
            throw new IllegalStateException("Not running");
        }
        if (sessionTimeoutSeconds < 1) {
            return;
        }
        int remainingSeconds = getRemainingSeconds();
        if (remainingSeconds < 1) {
            remainingSeconds = 1;
        }
        clientInactivityExtension.scheduleTimeout(remainingSeconds);
    }

    public void start(int sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
        clientInactivityExtension.addActionListener(inactivityActionListener);
        clientInactivityExtension.addTimeoutListener(inactivityTimeoutListener);
        running = true;
        onUserAction();
    }

    public void stop() {
        clientInactivityExtension.removeActionListener(inactivityActionListener);
        clientInactivityExtension.removeTimeoutListener(inactivityTimeoutListener);
        clientInactivityExtension.cancel();
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    private void onInactivityTimeout() {
        if (sessionTimeoutSeconds < 1) {
            return;
        }
        int remainingSeconds = getRemainingSeconds();
        if (remainingSeconds < 1) {
            fireTimeoutEvent();
        } else {
            clientInactivityExtension.scheduleTimeout(remainingSeconds);
        }
    }

    public int getRemainingSeconds() {
        int elapsedSeconds = (int) Math.round((double) (System.currentTimeMillis() - getLastActionTime()) / 1000);
        int remainingSeconds = sessionTimeoutSeconds - elapsedSeconds;
        return remainingSeconds;
    }

    private void onUserAction() {
        registerLastActionTime();
        if (sessionTimeoutSeconds > 0) {
            clientInactivityExtension.scheduleTimeout(sessionTimeoutSeconds);
        }
    }

    private void registerLastActionTime() {
        VaadinSession.getCurrent().setAttribute(SESSION_KEY_LAST_ACTION_TIME, System.currentTimeMillis());
    }

    public long getLastActionTime() {
        return (Long) VaadinSession.getCurrent().getAttribute(SESSION_KEY_LAST_ACTION_TIME);
    }

    public interface SessionTimeoutListener extends Serializable {

        void timeout();
    }

}
