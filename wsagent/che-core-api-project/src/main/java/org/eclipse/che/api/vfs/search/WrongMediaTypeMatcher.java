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

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.tika.mime.MediaType.APPLICATION_ZIP;
import static org.apache.tika.mime.MediaType.OCTET_STREAM;

/**
 * Filter based on file media type. It filters files with media type specified
 * in {@link WrongMediaTypeMatcher#excludedMediaTypes}. Note: if media type can not
 * be detected a file will not be include in result as well.
 */
public class WrongMediaTypeMatcher implements PathMatcher {
    private final Set<MediaType> excludedMediaTypes = newHashSet(APPLICATION_ZIP, OCTET_STREAM);
    private final Set<String>    excludedTypes      = newHashSet("video", "audio", "image");

    @Override
    public boolean matches(Path path) {
        try (InputStream content = Files.newInputStream(path)) {
            MediaType mimeType = TikaConfig.getDefaultConfig().getDetector().detect(content, new Metadata());
            String type = mimeType.getType();

            return excludedMediaTypes.contains(mimeType) || excludedTypes.contains(type);
        } catch (IOException e) {
            return true;
        }
    }
}
