package com.wcs.vaadin.userinactivity.client;

import com.vaadin.shared.annotations.Delayed;
import com.vaadin.shared.communication.ServerRpc;

public interface UserInactivityServerRpc extends ServerRpc {

    public void timeout();
    
    @Delayed(lastOnly = true)
    public void action(boolean fire);
}
