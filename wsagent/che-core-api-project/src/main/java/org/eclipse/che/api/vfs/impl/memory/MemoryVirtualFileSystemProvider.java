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
package org.eclipse.che.api.vfs.impl.memory;

import org.eclipse.che.api.vfs.AbstractVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.ArchiverFactory;
import org.eclipse.che.api.vfs.VirtualFileSystem;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MemoryVirtualFileSystemProvider extends AbstractVirtualFileSystemProvider {

    @Inject
    public MemoryVirtualFileSystemProvider() {
    }

    @Override
    protected VirtualFileSystem createVirtualFileSystem(CloseCallback closeCallback) {
        return new MemoryVirtualFileSystem(new ArchiverFactory(), closeCallback);
    }
}
