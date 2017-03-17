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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.IOUtils;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.vfs.watcher.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

public class LuceneSearcher implements Searcher {
    private static final Logger LOG = LoggerFactory.getLogger(LuceneSearcher.class);

    private static final int    RESULT_LIMIT = 1000;
    private static final String PATH_FIELD   = "path";
    private static final String NAME_FIELD   = "name";
    private static final String TEXT_FIELD   = "text";

    private final PathResolver     pathResolver;
    private final Set<PathMatcher> excludePathMatchers;
    private final File             indexRootDirectory;
    private       IndexWriter      indexWriter;
    private       SearcherManager  searcherManager;

    @Inject
    public LuceneSearcher(PathResolver pathResolver,
                          @Named("vfs.local.fs_index_root_dir") File indexRootDirectory,
                          @Named("che.fs.index.excludes") Set<PathMatcher> indexExcludes) {
        this.pathResolver = pathResolver;
        this.excludePathMatchers = indexExcludes;
        this.indexRootDirectory = indexRootDirectory;
    }

    @PostConstruct
    public void initialize() {
        try {
            Analyzer analyzer = prepareAnalyzer();
            Directory directory = prepareDirectory();

            indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
            searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());
        } catch (IOException e) {
            LOG.error("Can't properly initialize lucene searcher components", e);
        }
    }

    private Directory prepareDirectory() throws IOException {
        Files.createDirectories(indexRootDirectory.toPath());
        Directory directory;
        try {
            LOG.debug("Opening FS directory {}", indexRootDirectory.toPath());
            directory = FSDirectory.open(indexRootDirectory.toPath(), new SingleInstanceLockFactory());
        } catch (IOException e) {
            LOG.warn("Failed to open FS directory {}, will try to open RAM directory instead", indexRootDirectory.toPath(), e);
            directory = new RAMDirectory();
        }
        return directory;
    }

    private Analyzer prepareAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new WhitespaceTokenizer();
                TokenStream filter = new LowerCaseFilter(tokenizer);
                return new TokenStreamComponents(tokenizer, filter);
            }
        };
    }

    @PreDestroy
    public void terminate() {
        try {
            IOUtils.close(indexWriter, indexWriter.getDirectory(), searcherManager);
            if (!deleteRecursive(indexRootDirectory)) {
                LOG.warn("Unable delete index directory '{}', adding it to FileCleaner", indexRootDirectory);
                FileCleaner.addFile(indexRootDirectory);
            }
        } catch (IOException e) {
            LOG.error("Can't properly terminate lucene searcher components", e);
        }
    }

    @Override
    public SearchResult internalSearch(QueryExpression query) throws ServerException {
        return search(query);
    }

    @Override
    public SearchResult externalSearch(QueryExpression query) throws ServerException {
        String externalPath = query.getPath();
        if (externalPath != null) {
            Path internalPath = pathResolver.toInternalPath(externalPath);
            query.setPath(internalPath.toString());
        }

        SearchResult internalResult = search(query);
        List<SearchResultEntry> externalResultEntries = internalResult.getResults().stream().map(it -> {
            Path tmpInternalPath = Paths.get(it.getFilePath());
            String tmpExternalPath = pathResolver.toExternalPath(tmpInternalPath);
            return new SearchResultEntry(tmpExternalPath);
        }).collect(toList());


        return SearchResult.aSearchResult()
                           .withResults(externalResultEntries)
                           .withTotalHits(internalResult.getTotalHits())
                           .withNextPageQueryExpression(
                                   internalResult.getNextPageQueryExpression().isPresent()
                                   ? internalResult.getNextPageQueryExpression().get()
                                   : null)
                           .withElapsedTimeMillis(internalResult.getElapsedTimeMillis())
                           .build();
    }


    private SearchResult search(QueryExpression query) throws ServerException {
        IndexSearcher luceneSearcher = null;
        try {
            final long startTime = System.currentTimeMillis();
            searcherManager.maybeRefresh();
            luceneSearcher = searcherManager.acquire();

            Query luceneQuery = createLuceneQuery(query);

            ScoreDoc after = null;
            final int numSkipDocs = Math.max(0, query.getSkipCount());
            if (numSkipDocs > 0) {
                after = skipScoreDocs(luceneSearcher, luceneQuery, numSkipDocs);
            }

            final int numDocs = query.getMaxItems() > 0 ? Math.min(query.getMaxItems(), RESULT_LIMIT) : RESULT_LIMIT;
            TopDocs topDocs = luceneSearcher.searchAfter(after, luceneQuery, numDocs);
            final int totalHitsNum = topDocs.totalHits;

            List<SearchResultEntry> results = newArrayList();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                String filePath = luceneSearcher.doc(scoreDoc.doc).getField(PATH_FIELD).stringValue();
                results.add(new SearchResultEntry(filePath));
            }

            final long elapsedTimeMillis = System.currentTimeMillis() - startTime;

            boolean hasMoreToRetrieve = numSkipDocs + topDocs.scoreDocs.length + 1 < totalHitsNum;
            QueryExpression nextPageQueryExpression = null;
            if (hasMoreToRetrieve) {
                nextPageQueryExpression = createNextPageQuery(query, numSkipDocs + topDocs.scoreDocs.length);
            }

            return SearchResult.aSearchResult()
                               .withResults(results)
                               .withTotalHits(totalHitsNum)
                               .withNextPageQueryExpression(nextPageQueryExpression)
                               .withElapsedTimeMillis(elapsedTimeMillis)
                               .build();
        } catch (IOException | ParseException e) {
            LOG.error("Something went wrong, trying to run search", e);
            throw new ServerException(e);
        } finally {
            try {
                searcherManager.release(luceneSearcher);
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private Query createLuceneQuery(QueryExpression query) throws ParseException {
        final BooleanQuery luceneQuery = new BooleanQuery();
        final String name = query.getName();
        final String path = query.getPath();
        final String text = query.getText();
        if (path != null) {
            luceneQuery.add(new PrefixQuery(new Term(PATH_FIELD, path)), BooleanClause.Occur.MUST);
        }
        if (name != null) {
            QueryParser qParser = new QueryParser(NAME_FIELD, new Analyzer() {
                @Override
                protected TokenStreamComponents createComponents(String fieldName) {
                    Tokenizer tokenizer = new WhitespaceTokenizer();
                    TokenStream filter = new LowerCaseFilter(tokenizer);
                    return new TokenStreamComponents(tokenizer, filter);
                }
            });
            qParser.setAllowLeadingWildcard(true);
            luceneQuery.add(qParser.parse(name), BooleanClause.Occur.MUST);
        }

        if (text != null) {
            QueryParser qParser = new QueryParser(TEXT_FIELD, prepareAnalyzer());
            qParser.setAllowLeadingWildcard(true);
            luceneQuery.add(qParser.parse(text), BooleanClause.Occur.MUST);
        }
        return luceneQuery;
    }

    private ScoreDoc skipScoreDocs(IndexSearcher luceneSearcher, Query luceneQuery, int numSkipDocs) throws IOException {
        final int readFrameSize = Math.min(numSkipDocs, RESULT_LIMIT);
        ScoreDoc scoreDoc = null;
        int retrievedDocs = 0;
        TopDocs topDocs;
        do {
            topDocs = luceneSearcher.searchAfter(scoreDoc, luceneQuery, readFrameSize);
            if (topDocs.scoreDocs.length > 0) {
                scoreDoc = topDocs.scoreDocs[topDocs.scoreDocs.length - 1];
            }
            retrievedDocs += topDocs.scoreDocs.length;
        } while (retrievedDocs < numSkipDocs && topDocs.scoreDocs.length > 0);

        if (retrievedDocs > numSkipDocs) {
            int lastScoreDocIndex = topDocs.scoreDocs.length - (retrievedDocs - numSkipDocs);
            scoreDoc = topDocs.scoreDocs[lastScoreDocIndex];
        }

        return scoreDoc;
    }

    private QueryExpression createNextPageQuery(QueryExpression originalQuery, int newSkipCount) {
        return new QueryExpression().setText(originalQuery.getText())
                                    .setName(originalQuery.getName())
                                    .setPath(originalQuery.getPath())
                                    .setSkipCount(newSkipCount)
                                    .setMaxItems(originalQuery.getMaxItems());
    }

    @Override
    public final void add(Path path) {
        if (Files.exists(path)) {
            Document doc = new Document();
            doc.add(new StringField(PATH_FIELD, path.toAbsolutePath().toString(), Field.Store.YES));
            doc.add(new TextField(NAME_FIELD, path.getFileName().toString(), Field.Store.YES));


            if (notInExcludes(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    doc.add(new TextField(TEXT_FIELD, reader));
                    indexWriter.addDocument(doc);
                    return;
                } catch (IOException e) {
                    LOG.error("Can't create reader when adding file {}", path, e);
                }
            }

            try {
                indexWriter.addDocument(doc);
            } catch (IOException e) {
                LOG.error("Can't add file {} to index", path, e);
            }
        }
    }

    @Override
    public final void delete(Path path) {
        try {
            if (Files.exists(path)) {
                Term term = new Term(PATH_FIELD, path.toAbsolutePath().toString());
                indexWriter.deleteDocuments(term);
            }
        } catch (IOException e) {
            LOG.error("Can't remove file {} from index", path, e);
        }
    }

    @Override
    public final void update(Path path) {
        if (Files.exists(path)) {
            Document doc = new Document();
            doc.add(new StringField(PATH_FIELD, path.toAbsolutePath().toString(), Field.Store.YES));
            doc.add(new TextField(NAME_FIELD, path.getFileName().toString(), Field.Store.YES));


            if (notInExcludes(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    doc.add(new TextField(TEXT_FIELD, reader));
                    indexWriter.updateDocument(new Term(PATH_FIELD, path.toString()), doc);
                    return;
                } catch (IOException e) {
                    LOG.error("Can't create reader when updating file {}", path, e);
                }
            }

            try {
                indexWriter.updateDocument(new Term(PATH_FIELD, path.toString()), doc);
            } catch (IOException e) {
                LOG.error("Can't update file {} to index", path, e);
            }
        }
    }

    private boolean notInExcludes(Path path) {
        for (PathMatcher matcher : excludePathMatchers) {
            if (matcher.matches(path)) {
                return false;
            }
        }

        return true;
    }
}
