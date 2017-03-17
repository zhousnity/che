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

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;

public class FileWatcherUtils {

    /**
     * Checks if specified path is within excludes
     *
     * @param excludes
     *         set of exclude matchers
     * @param path
     *         path being examined
     * @return true if path is within excludes, false otherwise
     */
    public static boolean isExcluded(Set<PathMatcher> excludes, Path path) {
        for (PathMatcher matcher : excludes) {
            if (matcher.matches(path)) {
                return true;
            }
        }
        return false;
    }
}
