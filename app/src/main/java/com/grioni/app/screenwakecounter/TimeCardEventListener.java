package com.grioni.app.screenwakecounter;

/**
 * @author - Matias Grioni
 * @created - 8/12/15
 *
 * Event listener for the TimeCardHolder. When the TimeCardHolder expands or
 * collapses the card or clicks it, this requires orchestration with the
 * TimeCardsManager and GraphDetailFragment. This allows for the TimeCardHolder
 * to remain focused on the implementation of the view, and for the controller
 * logic to stay within Fragments/Activities.
 */
public interface TimeCardEventListener {
    /**
     * Callback for when the TimeCardHolder item is clicked.
     *
     * @param position - The position of the card in the TimeCards list.
     */
    public void onCardClicked(int position);

    /**
     * Callback for when the arrow to change the collapsed state of the card is
     * clicked.
     *
     * @param position - The position of the card in the TimeCards list.
     */
    public void onCardStateChanged(int position);
}
