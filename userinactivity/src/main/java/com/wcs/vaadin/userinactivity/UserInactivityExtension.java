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

import com.vaadin.server.AbstractClientConnector;
import com.wcs.vaadin.userinactivity.client.UserInactivityServerRpc;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.Extension;
import com.vaadin.ui.UI;
import com.wcs.vaadin.userinactivity.client.UserInactivityClientRpc;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * Extension to track user activity, and provide inactivity timeout event.
 * 
 * @author kumm
 */
public class UserInactivityExtension extends AbstractExtension {

    private final Collection<TimeoutListener> timeoutListeners = new HashSet<TimeoutListener>();
    private final Collection<ActionListener> actionListeners = new HashSet<ActionListener>();
    private SessionTimeoutHandler sessionTimeoutHandler;

    UserInactivityExtension() {
        registerRpc(new UserInactivityServerRpc() {
            @Override
            public void timeout() {
                fireTimeoutEvent();
            }

            @Override
            public void action(boolean fire) {
                if (fire) {
                    fireActionEvent();
                }
            }
        });
    }

    @Override
    protected void extend(AbstractClientConnector target) {
        super.extend(target);
    }

    @Override
    protected Class<? extends ClientConnector> getSupportedParentType() {
        return UI.class;
    }

    /**
     * Adds a user inactivity timeout listener to the extended UI.
     * You should not need this with an initialized sessionTimeoutHandler.
     * Use instead {@link SessionTimeoutHandler.addTimeoutListener}.
     * 
     * @param listener timeout listener
     */
    public void addTimeoutListener(TimeoutListener listener) {
        timeoutListeners.add(listener);
    }

    /**
     * Removes a user inactivity timeout listener from the extended UI.
     * You should not need this with an initialized sessionTimeoutHandler.
     * Use instead {@link SessionTimeoutHandler.removeTimeoutListener}.
     * 
     * @param listener timeout listener
     */
    public void removeTimeoutListener(TimeoutListener listener) {
        timeoutListeners.remove(listener);
    }

    /**
     * Adds a user action listener to the extended UI.
     * @param listener user action listener
     */
    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    /**
     * Removes a user action listener from the extended UI.
     * @param listener user action listener
     */
    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    /**
     * Schedules a user inactivity timeout on the extended UI.
     * On user action this timeout cancelled, and you are responsible to reschedule.
     * {@link SessionTimeoutHandler} manages the schedule automatically, 
     * so you should not use this method with it.
     * 
     * @param timeoutSeconds timeout in seconds
     */
    public void scheduleTimeout(int timeoutSeconds) {
        getRpcProxy(UserInactivityClientRpc.class).scheduleTimeout(timeoutSeconds);
    }

    /**
     * Cancels the sheduled timeout.
     * Does not fail if it's not scheduled.
     * {@link SessionTimeoutHandler} manages the schedule automatically, 
     * so you should not use this method with it.
     */
    public void cancel() {
        scheduleTimeout(0);
    }
    
    /**
     * Initializes session-wise inactivity timeout handling on the extended UI.
     * 
     * @return SessionTimeoutHandler for the extended UI.
     * @throws IllegalStateException if sessionTimeoutHandler already initlialized for the extended UI.
     */
    public SessionTimeoutHandler initSessionTimeoutHandler() {
        if (sessionTimeoutHandler == null) {
            sessionTimeoutHandler = new SessionTimeoutHandler(this);
            return sessionTimeoutHandler;
        } else {
            throw new IllegalStateException("SessionHandler already  inititalized.");
        }
    }
    
    /**
     * Returns the sessionTimeoutHandler for the extended UI.
     * 
     * @return sessionTimeoutHandler or null
     */
    public SessionTimeoutHandler getSessionTimeoutHandler() {
        return sessionTimeoutHandler;
    }

    private void fireTimeoutEvent() {
        for (TimeoutListener listener : timeoutListeners) {
            listener.timeout();
        }
    }

    private void fireActionEvent() {
        for (ActionListener listener : actionListeners) {
            listener.action();
        }
    }

    /**
     * Listener called when user inactivity timeout elapsed.
     */
    public interface TimeoutListener extends Serializable {
        
        /**
         * Inactivitiy timeout elapsed
         */
        void timeout();
    }

    /**
     * Listener called on user action.
     */
    public interface ActionListener extends Serializable {
        
        /**
         * User action occured
         */
        void action();
    }

    /**
     * Creates an instance of the extension, and extends the given UI.
     * 
     * @param ui UI to extend. It should be the current UI.
     * @return The extension instance for the given UI
     * @throws IllegalStateException if the UI is already extended
     */
    public static UserInactivityExtension init(UI ui) {
        UserInactivityExtension instance = get(ui);
        if (instance != null) {
            throw new IllegalStateException("UI already extended.");
        }
        instance = new UserInactivityExtension();
        instance.extend(ui);
        return instance;
    }

    /**
     * Returns the instance of the extension for the given UI.
     * 
     * @param ui the UI. It should be the current UI.
     * @return The extension instance for the given UI, or null
     */
    public static UserInactivityExtension get(UI ui) {
        Collection<Extension> extensions = ui.getExtensions();
        for (Extension extension : extensions) {
            if (extension instanceof UserInactivityExtension) {
                return (UserInactivityExtension) extension;
            }
        }
        return null;
    }
}
