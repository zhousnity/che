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
package org.eclipse.che.api.vfs.impl.file;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.AbstractVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.ArchiverFactory;
import org.eclipse.che.api.vfs.VirtualFileSystem;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Singleton
public class LocalVirtualFileSystemProvider extends AbstractVirtualFileSystemProvider {
    private final File             rootDirectory;

    @Inject
    public LocalVirtualFileSystemProvider(@Named("che.user.workspaces.storage") File rootDirectory) throws IOException {
        this.rootDirectory = rootDirectory;
        Files.createDirectories(rootDirectory.toPath());
    }

    @Override
    protected VirtualFileSystem createVirtualFileSystem(CloseCallback closeCallback) throws ServerException {
        return new LocalVirtualFileSystem(rootDirectory, new ArchiverFactory(), closeCallback);
    }
}
