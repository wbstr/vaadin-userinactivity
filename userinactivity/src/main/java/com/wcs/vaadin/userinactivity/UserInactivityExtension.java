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
    private SessionHandler sessionHandler;

    public UserInactivityExtension() {
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
        System.out.println(System.currentTimeMillis()/1000 + ": schedue "+timeout+" on " + UI.getCurrent().getConnectorId());
        getRpcProxy(UserInactivityClientRpc.class).scheduleTimeout(timeout);
    }

    public void cancel() {
        scheduleTimeout(0);
    }
    
    public SessionHandler initSessionHandler(int timeoutSeconds) {
        if (sessionHandler == null) {
            sessionHandler = new SessionHandler(this, timeoutSeconds);
            return sessionHandler;
        } else {
            throw new IllegalStateException("Already sessionHandler inititalized.");
        }
    }
    
    public SessionHandler getSessionHandler() {
        return sessionHandler;
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

    public static UserInactivityExtension extendCurrentUI() {
        UI currentUi = UI.getCurrent();
        UserInactivityExtension instance = findInstance(currentUi);
        if (instance != null) {
            throw new IllegalStateException("Already extended");
        }
        instance = new UserInactivityExtension();
        instance.extend(currentUi);
        return instance;
    }

    public static UserInactivityExtension getCurrent() {
        UI currentUi = UI.getCurrent();
        UserInactivityExtension instance = findInstance(currentUi);
        return instance;
    }

    private static UserInactivityExtension findInstance(UI currentUi) {

        Collection<Extension> extensions = currentUi.getExtensions();
        for (Extension extension : extensions) {
            if (extension instanceof UserInactivityExtension) {
                return (UserInactivityExtension) extension;
            }
        }
        return null;
    }
}
