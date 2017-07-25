package org.eclipse.che.api.project.shared;

/**
 * @author Vitalii Parfonov
 */

public interface SearchOccurrence {
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
     * @return
     */
    int getStartOffset();

    /**
     *
     * @param startOffset
     */
    void setStartOffset(int startOffset);
}
