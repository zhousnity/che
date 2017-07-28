/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.system.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Anton Korneta
 */
public class SystemApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ServiceTerminator.class);
        bind(SystemService.class);
        bind(SystemManager.class);
        bind(SystemEventsWebsocketBroadcaster.class);
        Multibinder.newSetBinder(binder(), ServiceTermination.class);
    }

}
