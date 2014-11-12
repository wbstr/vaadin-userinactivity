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
package com.wcs.vaadin.userinactivity.client;

import com.vaadin.shared.annotations.Delayed;
import com.vaadin.shared.communication.ServerRpc;

public interface UserInactivityServerRpc extends ServerRpc {

    public void timeout();
    
    @Delayed(lastOnly = true)
    public void action(boolean fire);
}
