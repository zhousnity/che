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

import org.eclipse.che.commons.lang.IoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link PathResolver}
 */
@RunWith(MockitoJUnitRunner.class)
public class PathResolverTest {
    PathResolver pathResolver;
    Path         root;

    @Before
    public void setUp() throws Exception {
        root = Files.createTempDirectory("root");
        pathResolver = new PathResolver(root.toFile());
    }

    @After
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(root.toFile());
    }

    @Test
    public void shouldGetInternalPath() throws Exception {
        Path path = Files.createTempDirectory(root, "dir");
        String expected = "/"+path.getFileName();

        String actual = pathResolver.toExternalPath(path);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetNormalPath() throws Exception {
        String path = "/" + "dir";
        Path expected = Paths.get(root.toAbsolutePath().toString(), "dir");

        Path actual = pathResolver.toInternalPath(path);

        assertEquals(expected, actual);
    }
}
