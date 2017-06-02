package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Vitalii Parfonov
 */
@DTO
public interface FoundItemData {

    /**
     *
     * @return
     */
    float getScore();

    /**
     *
     * @param score
     */
    void setScore(float score);

    /**
     *
     * @param score
     * @return
     */
    FoundItemData withScore(float score);


    /**
     *
     * @return
     */
    String getPhrase();


    /**
     *
     * @param phrase
     */
    void setPhrase(String phrase);


    /**
     *
     * @param phrase
     * @return
     */
    FoundItemData withPhrase(String phrase);


    /**
     *
     * @return
     */
    int getEndOffset();


    /**
     *
     * @param endOffset
     */
    void setEndOffset(int endOffset);


    /**
     *
     * @param endOffset
     */
    FoundItemData withEndOffset(int endOffset);


    /**
     *
     * @return
     */
    int getStartOffset();


    /**
     *
     * @param startOffset
     */
    void setStartOffset(int startOffset);


    /**
     *
     * @param startOffset
     */
    FoundItemData withStartOffset(int startOffset);

}
