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
package org.eclipse.che.api.vfs.watcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;

@Singleton
public class PathResolver {

    private Path root;

    @Inject
    public PathResolver(@Named("che.user.workspaces.storage") File root) {
        this.root = root.toPath().normalize().toAbsolutePath();
    }

    /**
     * Transform internal path representation into external path representation
     *
     * @param path
     *         internal path representation
     * @return normal path
     */
    public String toExternalPath(Path path) {
        return "/" + root.relativize(path.toAbsolutePath());
    }

    /**
     * Transforms external path representation into internal absolute path representation
     *
     * @param path
     *         external path representation
     * @return internal path
     */
    public Path toInternalPath(String path) {
        return root.resolve(path.startsWith("/")?path.substring(1):path);
    }
}
