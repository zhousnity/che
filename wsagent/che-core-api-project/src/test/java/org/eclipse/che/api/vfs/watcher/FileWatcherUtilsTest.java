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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static java.util.Collections.singleton;
import static org.eclipse.che.api.vfs.watcher.FileWatcherUtils.isExcluded;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FileWatcherUtils}
 */
public class FileWatcherUtilsTest {

    @Test
    public void shouldBeExcluded() throws Exception {
        PathMatcher matcher = Mockito.mock(PathMatcher.class);
        Path path = Mockito.mock(Path.class);
        when(matcher.matches(path)).thenReturn(true);

        boolean condition = isExcluded(singleton(matcher), path);

        assertTrue(condition);
    }

    @Test
    public void shouldNotBeExcluded() throws Exception {
        PathMatcher matcher = Mockito.mock(PathMatcher.class);
        Path path = Mockito.mock(Path.class);
        when(matcher.matches(path)).thenReturn(false);

        boolean condition = isExcluded(singleton(matcher), path);

        assertFalse(condition);
    }
}
