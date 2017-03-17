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
package org.eclipse.che.api.vfs.search.impl;

import org.eclipse.che.api.vfs.search.LuceneSearcher;
import org.eclipse.che.api.vfs.search.QueryExpression;
import org.eclipse.che.api.vfs.search.SearchResult;
import org.eclipse.che.api.vfs.watcher.PathResolver;
import org.eclipse.che.commons.lang.NameGenerator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LuceneSearcherTest {
    private static final String[] TEST_CONTENT = {
            "Apollo set several major human spaceflight milestones",
            "Maybe you should think twice",
            "To be or not to be",
            "In early 1961, direct ascent was generally the mission mode in favor at NASA"
    };

    private File             indexDirectory;
    private Path             root;
    private Set<PathMatcher> indexExcludes;
    private LuceneSearcher   searcher;
    private PathResolver     pathResolver;

    @BeforeMethod
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        indexDirectory = new File(targetDir, NameGenerator.generate("index-", 4));
        assertTrue(indexDirectory.mkdir());

        root = Files.createTempDirectory("root");

        indexExcludes = newHashSet();
        pathResolver = new PathResolver(root.toFile());
        searcher = new LuceneSearcher(pathResolver, indexDirectory, indexExcludes);
        searcher.initialize();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        searcher.terminate();
    }

    @Test
    public void addsSingleFileInIndex() throws Exception {
        Path tmpDir = Files.createTempDirectory(root, "tempDir");
        Path tempFile = Files.createTempFile(tmpDir, "temp", "File");
        Files.write(tempFile, TEST_CONTENT[1].getBytes());
        searcher.add(tempFile);

        List<String> paths = searcher.externalSearch(new QueryExpression().setText("should")).getFilePaths();
        assertEquals(paths, newArrayList(pathResolver.toExternalPath(tempFile)));
    }

    @Test
    public void updatesSingleFileInIndex() throws Exception {
        Path tmpDir = Files.createTempDirectory(root,"tempDir");
        Path tempFile = Files.createTempFile(tmpDir, "temp", "File");
        searcher.add(tempFile);

        List<String> paths = searcher.externalSearch(new QueryExpression().setText("should")).getFilePaths();
        assertTrue(paths.isEmpty());

        Files.write(tempFile, TEST_CONTENT[1].getBytes());
        searcher.update(tempFile);

        paths = searcher.externalSearch(new QueryExpression().setText("should")).getFilePaths();

        assertEquals(newArrayList(pathResolver.toExternalPath(tempFile)), paths);
    }

    @Test
    public void deletesSingleFileFromIndex() throws Exception {
        Path tmpDir = Files.createTempDirectory(root, "tempDir");
        Path tempFile = Files.createTempFile(tmpDir, "temp", "File");
        Files.write(tempFile, TEST_CONTENT[2].getBytes());
        searcher.add(tempFile);

        List<String> paths = searcher.externalSearch(new QueryExpression().setText("be")).getFilePaths();
        assertEquals(newArrayList(pathResolver.toExternalPath(tempFile)), paths);

        searcher.delete(tempFile);

        paths = searcher.externalSearch(new QueryExpression().setText("be")).getFilePaths();
        assertTrue(paths.isEmpty());
    }

    @Test
    public void searchesByWordFragment() throws Exception {
        Path tmpDir = Files.createTempDirectory(root,"tempDir");
        Path tempFile = Files.createTempFile(tmpDir, "temp", "File");
        Files.write(tempFile, TEST_CONTENT[0].getBytes());
        searcher.add(tempFile);

        List<String> paths = searcher.externalSearch(new QueryExpression().setText("*stone*")).getFilePaths();
        assertEquals(paths, newArrayList(pathResolver.toExternalPath(tempFile)));
    }

    @Test
    public void searchesByTextAndFileName() throws Exception {
        Path tmpDir = Files.createTempDirectory(root,"tempDir");
        Path tempFileI = Files.createTempFile(tmpDir, "tempI", "File");
        Path tempFileII = Files.createTempFile(tmpDir, "tempII", "File");
        Files.write(tempFileI, TEST_CONTENT[2].getBytes());
        Files.write(tempFileI, TEST_CONTENT[2].getBytes());
        searcher.add(tempFileI);
        searcher.add(tempFileII);

        List<String> paths =
                searcher.externalSearch(new QueryExpression().setText("be").setName(tempFileI.getFileName().toString())).getFilePaths();
        assertEquals(newArrayList(pathResolver.toExternalPath(tempFileI)), paths);
    }

    @DataProvider
    public Object[][] searchByName() {
        return new Object[][]{
                {"sameName.txt", "sameName.txt"},
                {"notCaseSensitive.txt", "notcasesensitive.txt"},
                {"fullName.txt", "full*"},
                {"file name.txt", "file name"},
                {"prefixFileName.txt", "prefixF*"},
                {"name.with.dot.txt", "name.With.Dot.txt"},
                };
    }

    @Test(dataProvider = "searchByName")
    public void searchFileByName(String fileName, String searchedFileName) throws Exception {
        Path tmpDirI = Files.createTempDirectory(root,"tempDirI");
        Path tmpDirII = Files.createTempDirectory(root,"tempDirII");
        Path tempFileI = Files.createTempFile(tmpDirI, "tempI", "File");
        Path tempFileII = Files.createFile(tmpDirI.resolve(fileName));
        Path tempFileIII = Files.createTempFile(tmpDirI, "tempIII", "File");
        Path tempFileIV = Files.createTempFile(tmpDirII, "tempIV", "File");
        Path tempFileV = Files.createTempFile(tmpDirII, "tempV", "File");

        Files.write(tempFileI, TEST_CONTENT[3].getBytes());
        Files.write(tempFileII, TEST_CONTENT[2].getBytes());
        Files.write(tempFileIII, TEST_CONTENT[1].getBytes());
        Files.write(tempFileIV, TEST_CONTENT[2].getBytes());
        Files.write(tempFileV, TEST_CONTENT[2].getBytes());

        searcher.add(tempFileI);
        searcher.add(tempFileII);
        searcher.add(tempFileIII);
        searcher.add(tempFileIV);
        searcher.add(tempFileV);

        List<String> paths = searcher.externalSearch(new QueryExpression().setName(searchedFileName)).getFilePaths();
        assertEquals(paths, newArrayList(pathResolver.toExternalPath(tempFileII)));
    }

    @Test
    public void searchesByTextAndPath() throws Exception {
        Path tempDirI = Files.createTempDirectory(root,"tempDirI");
        Path tempDirII = Files.createTempDirectory(root,"tempDirII");
        Path tempFileI = Files.createTempFile(tempDirI, "tempI", null);
        Path tempFileII = Files.createTempFile(tempDirII, "tempII", null);
        Files.write(tempFileI, TEST_CONTENT[2].getBytes());
        Files.write(tempFileII, TEST_CONTENT[2].getBytes());
        searcher.add(tempFileI);
        searcher.add(tempFileII);

        List<String> paths = searcher.externalSearch(new QueryExpression().setText("be").setPath(pathResolver.toExternalPath(tempDirI))).getFilePaths();
        assertEquals(newArrayList(pathResolver.toExternalPath(tempFileI)), paths);
    }

    @Test
    public void searchesByTextAndPathAndFileName() throws Exception {
        Path tempDirI = Files.createTempDirectory(root,"tempDirI");
        Path tempDirII = Files.createTempDirectory(root,"tempDirII");
        Path tempFileI = Files.createTempFile(tempDirI, "tempI", null);
        Path tempFileII = Files.createTempFile(tempDirI, "tempII", null);
        Path tempFileIII = Files.createTempFile(tempDirII, "tempIII", null);
        Files.write(tempFileI, TEST_CONTENT[2].getBytes());
        Files.write(tempFileII, TEST_CONTENT[2].getBytes());
        Files.write(tempFileIII, TEST_CONTENT[2].getBytes());
        searcher.add(tempFileI);
        searcher.add(tempFileII);
        searcher.add(tempFileIII);

        List<String> paths = searcher.externalSearch(
                new QueryExpression().setText("be").setPath(pathResolver.toExternalPath(tempDirI)).setName(tempFileI.getFileName().toString()))
                                     .getFilePaths();
        assertEquals(newArrayList(pathResolver.toExternalPath(tempFileI)), paths);
    }


    @Test
    public void excludesFilesFromIndexWithFilter() throws Exception {
        Path tempDirI = Files.createTempDirectory(root,"tempDirI");
        Path tempFileI = Files.createTempFile(tempDirI, "tempI", null);
        Path tempFileII = Files.createTempFile(tempDirI, "tempII", null);
        Path tempFileIII = Files.createTempFile(tempDirI, "tempIII", null);
        Files.write(tempFileI, TEST_CONTENT[2].getBytes());
        Files.write(tempFileII, TEST_CONTENT[2].getBytes());
        Files.write(tempFileIII, TEST_CONTENT[2].getBytes());

        indexExcludes.add(it -> it.getFileName().equals(tempFileIII.getFileName()));

        searcher.add(tempFileI);
        searcher.add(tempFileII);
        searcher.add(tempFileIII);


        List<String> paths = searcher.externalSearch(new QueryExpression().setText("be")).getFilePaths();
        assertEquals(newArrayList(pathResolver.toExternalPath(tempFileI), pathResolver.toExternalPath(tempFileII)), paths);
    }

    @Test
    public void limitsNumberOfSearchResultsWhenMaxItemIsSet() throws Exception {
        Path tempDirI = Files.createTempDirectory(root,"tempDirI");

        for (int i = 0; i < 100; i++) {
            Path file = Files.createFile(tempDirI.resolve(String.format("file%02d", i)));
            Files.write(file, TEST_CONTENT[i % TEST_CONTENT.length].getBytes());
            searcher.add(file);
        }

        SearchResult result = searcher.externalSearch(new QueryExpression().setText("mission").setMaxItems(5));

        assertEquals(25, result.getTotalHits());
        assertEquals(5, result.getFilePaths().size());
    }

    @Test
    public void generatesQueryExpressionForRetrievingNextPageOfResults() throws Exception {
        Path tempDirI = Files.createTempDirectory(root,"tempDirI");

        for (int i = 0; i < 100; i++) {
            Path file = Files.createFile(tempDirI.resolve(String.format("file%02d", i)));
            Files.write(file, TEST_CONTENT[i % TEST_CONTENT.length].getBytes());
            searcher.add(file);
        }

        SearchResult result = searcher.externalSearch(new QueryExpression().setText("spaceflight").setMaxItems(7));

        assertEquals(25, result.getTotalHits());

        Optional<QueryExpression> optionalNextPageQueryExpression = result.getNextPageQueryExpression();
        assertTrue(optionalNextPageQueryExpression.isPresent());

        QueryExpression nextPageQueryExpression = optionalNextPageQueryExpression.get();
        assertEquals("spaceflight", nextPageQueryExpression.getText());
        assertEquals(7, nextPageQueryExpression.getSkipCount());
        assertEquals(7, nextPageQueryExpression.getMaxItems());
    }

    @Test
    public void retrievesSearchResultWithPages() throws Exception {
        Path tempDirI = Files.createTempDirectory(root,"tempDirI");

        for (int i = 0; i < 100; i++) {
            Path file = Files.createFile(tempDirI.resolve(String.format("file%02d", i)));
            Files.write(file, TEST_CONTENT[i % TEST_CONTENT.length].getBytes());
            searcher.add(file);
        }

        SearchResult firstPage = searcher.externalSearch(new QueryExpression().setText("spaceflight").setMaxItems(8));
        assertEquals(8, firstPage.getFilePaths().size());

        QueryExpression nextPageQueryExpression = firstPage.getNextPageQueryExpression().get();
        nextPageQueryExpression.setMaxItems(100);

        SearchResult lastPage = searcher.externalSearch(nextPageQueryExpression);
        assertEquals(17, lastPage.getFilePaths().size());

        assertTrue(Collections.disjoint(firstPage.getFilePaths(), lastPage.getFilePaths()));
    }
}
