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
import com.vaadin.ui.UI;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author kumm
 */
public class SessionHandler {

    private final int sessionTimeoutSeconds;
    private final UserInactivityExtension clientInactivityExtension;
    private final Set<SessionInactivityTimeoutListener> timeoutListeners = new HashSet<SessionInactivityTimeoutListener>();
    private final static String SESSION_KEY_LAST_ACTION_TIME
            = SessionHandler.class.getName() + ":last_client_action_time";

    SessionHandler(UserInactivityExtension clientInactivityExtension, int sessionTimeoutSeconds) {
        this.clientInactivityExtension = clientInactivityExtension;
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
        clientInactivityExtension.addTimeoutListener(new UserInactivityExtension.TimeoutListener() {

            @Override
            public void timeout() {
                onInactivityTimeout();
            }
        });
        clientInactivityExtension.addActionListener(new UserInactivityExtension.ActionListener() {

            @Override
            public void action() {
                onUserAction();
            }
        });
        onUserAction();
    }

    public void addTimeoutListener(SessionInactivityTimeoutListener listener) {
        timeoutListeners.add(listener);
    }

    public void removeTimeoutListener(SessionInactivityTimeoutListener listener) {
        timeoutListeners.remove(listener);
    }

    private void fireTimeoutEvent() {
        for (SessionInactivityTimeoutListener timeoutListener : timeoutListeners) {
            timeoutListener.timeout();
        }
    }

    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public void reschedule() {
        onInactivityTimeout();
    }

    private void onInactivityTimeout() {
        int remainingSeconds = getSecondsLeft();
        System.out.println(System.currentTimeMillis()/1000 + ": timeout left "+remainingSeconds+" on "+UI.getCurrent().getConnectorId());        
        if (remainingSeconds < 1) {
            fireTimeoutEvent();
        } else {
            clientInactivityExtension.scheduleTimeout(remainingSeconds);
        }
    }
    
    public int getSecondsLeft() {
        int elapsedSeconds = (int) Math.round((double)(System.currentTimeMillis() - getLastActionTime()) / 1000);
        int remainingSeconds = sessionTimeoutSeconds - elapsedSeconds;
        return remainingSeconds;
    }

    private void onUserAction() {
        System.out.println(System.currentTimeMillis()/1000 + ": action on "+UI.getCurrent().getConnectorId());
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

    public interface SessionInactivityTimeoutListener extends Serializable {

        void timeout();
    }

}
