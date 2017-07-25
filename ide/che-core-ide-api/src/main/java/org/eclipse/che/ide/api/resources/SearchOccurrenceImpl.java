package org.eclipse.che.ide.api.resources;

import org.eclipse.che.api.project.shared.SearchOccurrence;

/**
 * @author Vitalii Parfonov
 */

public class SearchOccurrenceImpl implements SearchOccurrence {

    private float  score;
    private int    endOffset;
    private int    startOffset;
    private String phrase;

    public SearchOccurrenceImpl(SearchOccurrence searchOccurrence) {
        score = searchOccurrence.getScore();
        endOffset = searchOccurrence.getEndOffset();
        startOffset = searchOccurrence.getStartOffset();
        phrase = searchOccurrence.getPhrase();
    }

    @Override
    public float getScore() {
        return 0;
    }

    @Override
    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String getPhrase() {
        return phrase;
    }

    @Override
    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }
}
