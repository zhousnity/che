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
package org.eclipse.che.api.vfs.search;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * @author Valeriy Svydenko
 */
@RunWith(DataProviderRunner.class)
public class WrongMediaTypeMatcherTest {
    private WrongMediaTypeMatcher wrongMediaTypeMatcher;

    @DataProvider
    public static Object[][] testData() throws Exception {
        Path tempI = Files.createTempFile("1", ".tmp");
        Files.write(tempI, "to be or not to be".getBytes());

        Path tempII = Files.createTempFile("1", ".tmp");
        Files.write(tempI, "<html><head></head></html>".getBytes());

        Path tempIII = Files.createTempFile("1", ".tmp");
        Files.write(tempI, "<a><b/></a>".getBytes());

        Path tempIV = Files.createTempFile("1", ".tmp");
        Files.write(tempI, "public class SomeClass {}".getBytes());

        Path tempV = Files.createTempFile("1", ".tmp");
        Files.write(tempI, new byte[10]);

        return new Object[][]{
                {tempI, false},
                {tempII, false},
                {tempIII, false},
                {tempIV, false},
                {tempV, true}
        };
    }

    @Before
    public void setUp() throws Exception {
        wrongMediaTypeMatcher = new WrongMediaTypeMatcher();
    }

    @UseDataProvider("testData")
    @Test
    public void testFilesShouldAccepted(Path path, boolean expectedResult) throws Exception {
        assertEquals(expectedResult, wrongMediaTypeMatcher.matches(path));
    }
}
