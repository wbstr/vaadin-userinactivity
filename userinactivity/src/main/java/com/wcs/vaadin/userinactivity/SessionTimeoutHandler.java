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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * User inactivity timeout per session handler.
 *
 * This class tracks last user action time in the vaadin session,
 * and provides a timeout listener for the user inactivity in the session.
 *
 * It can be handy, if you want so support multiple tabs in your application,
 * and don't want to inactivate other tabs, or logout, until your user is active in one tab.
 *
 * Note, just UIs with an initialized SessionTimeoutHandler counts in user activity tracking.
 *
 * @author kumm
 */
public class SessionTimeoutHandler implements Serializable {

    private int sessionTimeoutSeconds;
    private final UserInactivityExtension clientInactivityExtension;
    private final Set<SessionTimeoutListener> timeoutListeners = new HashSet<SessionTimeoutListener>();
    private final UserInactivityExtension.TimeoutListener inactivityTimeoutListener;
    private final UserInactivityExtension.ActionListener inactivityActionListener;
    private final LastActionRegistry lastActionRegistry;
    private boolean running = false;

    SessionTimeoutHandler(UserInactivityExtension clientInactivityExtension, LastActionRegistry lastActionRegistry) {
        this.clientInactivityExtension = clientInactivityExtension;
        this.lastActionRegistry = lastActionRegistry;
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

    /**
     * Adds a session inactivity timeout listener.
     *
     * @param listener timeout listener
     */
    public void addTimeoutListener(SessionTimeoutListener listener) {
        timeoutListeners.add(listener);
    }

    /**
     * Removes a session inactivity timeout listener.
     *
     * @param listener timeout listener
     */
    public void removeTimeoutListener(SessionTimeoutListener listener) {
        timeoutListeners.remove(listener);
    }

    private void fireTimeoutEvent() {
        for (SessionTimeoutListener timeoutListener : timeoutListeners) {
            timeoutListener.timeout();
        }
    }

    /**
     * Returns session inactivity timeout
     *
     * @return timeout in seconds
     */
    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    /**
     * Reschedules a check of session inactivity timeout.
     * It will trigger a timeout event if session inactivity timed out,
     * or schedules the next timeout check.
     * You need this method if you know some action happened meanwhile on an ather UI,
     * but session inactivity is already timed out in your current UI.
     * - for example a stopped countdown on an other UI.
     *
     * @throws IllegalStateException if not started
     */
    public void reschedule() {
        if (!running) {
            throw new IllegalStateException("Not running");
        }
        onInactivityTimeout();
    }

    /**
     * Starts session timeout handling, and user action tracking.
     * It's your responsibility to provide the same timeout value for all tracked UI.
     * Sets runnig state to true.
     *
     * @param sessionTimeoutSeconds Less than 1 means no timeout handling, just lastActionTime tracking
     */
    public void start(int sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
        clientInactivityExtension.addActionListener(inactivityActionListener);
        clientInactivityExtension.addTimeoutListener(inactivityTimeoutListener);
        running = true;
        onUserAction();
    }

    /**
     * Stops session timeout handling, and user action tracking.
     * Sets runnig state to false.
     */
    public void stop() {
        clientInactivityExtension.removeActionListener(inactivityActionListener);
        clientInactivityExtension.removeTimeoutListener(inactivityTimeoutListener);
        clientInactivityExtension.cancel();
        running = false;
    }

    /**
     * Returns running state
     *
     * @return true if running
     */
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

    /**
     * Returns remaining seconds until next check for a timeout event
     *
     * @return remaining time in seconds
     */
    public int getRemainingSeconds() {
        return lastActionRegistry.getRemainingSeconds(sessionTimeoutSeconds);
    }

    private void onUserAction() {
        lastActionRegistry.registerLastActionTime();
        if (sessionTimeoutSeconds > 0) {
            clientInactivityExtension.scheduleTimeout(sessionTimeoutSeconds);
        }
    }

    /**
     * Listener called on session timeout.
     */
    public interface SessionTimeoutListener extends Serializable {

        /**
         * Session timeout elapsed
         */
        void timeout();
    }

}
