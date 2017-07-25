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
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Vitalii Parfonov
 */
@DTO
public interface SearchOccurrenceDto extends SearchOccurrence {

    /**
     *
     * @param score
     * @return
     */
    SearchOccurrenceDto withScore(float score);


    /**
     *
     * @param phrase
     * @return
     */
    SearchOccurrenceDto withPhrase(String phrase);


    /**
     *
     * @param endOffset
     */
    SearchOccurrenceDto withEndOffset(int endOffset);


    /**
     *
     * @param startOffset
     */
    SearchOccurrenceDto withStartOffset(int startOffset);

}
