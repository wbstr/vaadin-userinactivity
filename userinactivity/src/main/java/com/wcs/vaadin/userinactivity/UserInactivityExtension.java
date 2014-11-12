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

    public void addTimeoutListener(TimeoutListener listener) {
        timeoutListeners.add(listener);
    }

    public void removeTimeoutListener(TimeoutListener listener) {
        timeoutListeners.remove(listener);
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    public void scheduleTimeout(int timeout) {
        getRpcProxy(UserInactivityClientRpc.class).scheduleTimeout(timeout);
    }

    public void cancel() {
        scheduleTimeout(0);
    }
    
    public SessionTimeoutHandler initSessionTimeoutHandler() {
        if (sessionTimeoutHandler == null) {
            sessionTimeoutHandler = new SessionTimeoutHandler(this);
            return sessionTimeoutHandler;
        } else {
            throw new IllegalStateException("SessionHandler already  inititalized.");
        }
    }
    
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

    public interface TimeoutListener extends Serializable {

        void timeout();
    }

    public interface ActionListener extends Serializable {

        void action();
    }

    public static UserInactivityExtension init(UI ui) {
        UserInactivityExtension instance = get(ui);
        if (instance != null) {
            throw new IllegalStateException("UI already extended.");
        }
        instance = new UserInactivityExtension();
        instance.extend(ui);
        return instance;
    }

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
