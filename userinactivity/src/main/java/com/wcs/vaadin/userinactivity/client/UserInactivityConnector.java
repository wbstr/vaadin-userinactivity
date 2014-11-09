package com.wcs.vaadin.userinactivity.client;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ApplicationConnection.ApplicationStoppedEvent;
import com.vaadin.client.ApplicationConnection.ApplicationStoppedHandler;
import com.vaadin.client.ApplicationConnection.CommunicationHandler;
import com.vaadin.client.ApplicationConnection.RequestStartingEvent;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import com.wcs.vaadin.userinactivity.UserInactivityExtension;

@Connect(UserInactivityExtension.class)
public class UserInactivityConnector extends AbstractExtensionConnector
        implements CommunicationHandler, ApplicationStoppedHandler, Event.NativePreviewHandler {

    private final UserInactivityServerRpc rpc = RpcProxy.create(UserInactivityServerRpc.class, this);
    private boolean userInitiatedRequest = false;

    private final Timer inactivityTimer = new Timer() {

        @Override
        public void run() {
            onInactivityTimeout();
        }
    };

    private void onInactivityTimeout() {
        rpc.timeout();
        //this will trigger a request, but it's not a user initiated request
        markNextRequestAsUserAction(false);
    }
    
    private void markNextRequestAsUserAction(boolean mark) {
        rpc.action(mark);
        userInitiatedRequest = mark;
    }

    private void schedule(int timeoutSeconds) {
        if (timeoutSeconds > 0) {
            inactivityTimer.schedule(timeoutSeconds * 1000);
        } else {
            inactivityTimer.cancel();
        }
    }

    @Override
    protected void extend(ServerConnector target) {
        ApplicationConnection connection = target.getConnection();
        connection.addHandler(RequestStartingEvent.TYPE, this);
        connection.addHandler(ApplicationStoppedEvent.TYPE, this);
        Event.addNativePreviewHandler(this);
        registerRpc(UserInactivityClientRpc.class, new UserInactivityClientRpc() {

            @Override
            public void scheduleTimeout(int timeoutSeconds) {
                schedule(timeoutSeconds);
            }
        });
    }

    @Override
    public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
            case Event.ONMOUSEDOWN:
            case Event.ONTOUCHSTART:
                markNextRequestAsUserAction(true);
        }
    }

    @Override
    public void onRequestStarting(RequestStartingEvent e) {
        if (userInitiatedRequest) {
            inactivityTimer.cancel();
        }
        userInitiatedRequest = false;
    }

    @Override
    public void onApplicationStopped(ApplicationConnection.ApplicationStoppedEvent event) {
        inactivityTimer.cancel();
    }

    @Override
    public void onResponseHandlingStarted(ApplicationConnection.ResponseHandlingStartedEvent e) {
        //NO-OP
    }

    @Override
    public void onResponseHandlingEnded(ApplicationConnection.ResponseHandlingEndedEvent e) {
        //NO-OP
    }

}
