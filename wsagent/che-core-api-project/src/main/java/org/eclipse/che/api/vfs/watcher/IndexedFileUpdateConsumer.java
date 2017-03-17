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

import org.eclipse.che.api.vfs.search.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.function.Consumer;

@Singleton
public class IndexedFileUpdateConsumer implements Consumer<Path> {
    private static final Logger LOG = LoggerFactory.getLogger(IndexedFileDeleteConsumer.class);

    private Searcher searcher;

    @Inject
    public IndexedFileUpdateConsumer(Searcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public void accept(Path path) {
        searcher.update(path.toAbsolutePath());
    }
}
