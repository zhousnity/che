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

import org.eclipse.che.api.core.ServerException;

import java.nio.file.Path;

public interface Searcher {
    /**
     * Return paths of matched items in internal representation
     *
     * @param query
     *         query expression
     * @return results of search
     *
     * @exception ServerException
     */
    SearchResult internalSearch(QueryExpression query) throws ServerException;

    /**
     * Return paths of matched items in external representation
     *
     * @param query
     *         query expression
     * @return results of search
     * @throws ServerException
     */
    SearchResult externalSearch(QueryExpression query) throws ServerException;

    /**
     * Add VirtualFile to index.
     *
     * @param path
     *         path of file
     */
    void add(Path path);

    /**
     * Delete VirtualFile from index.
     *
     * @param path
     *          path of file
     */
    void delete(Path path);

    /**
     * Updated indexed VirtualFile.
     *
     * @param path
     *          path of file
     */
    void update(Path path);
}
