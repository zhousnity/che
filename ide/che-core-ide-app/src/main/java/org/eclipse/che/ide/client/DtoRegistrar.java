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
package org.eclipse.che.ide.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.dto.DtoFactoryVisitor;

/** Registers client DTO providers. */
@Singleton
public class DtoRegistrar {

    @Inject
    public DtoRegistrar(DtoFactory dtoFactory, DtoFactoryVisitorRegistry registry) {
        registry.getDtoFactoryVisitors()
                .values()
                .forEach(provider -> ((DtoFactoryVisitor)provider.get()).accept(dtoFactory));
    }
}
