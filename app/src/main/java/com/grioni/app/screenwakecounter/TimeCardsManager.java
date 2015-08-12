package com.grioni.app.screenwakecounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author - Matias Grioni
 * @created - 8/10/15
 *
 * A wrapper around a list and database object to manage TimeCards for this application. Addition,
 * updating, and removal of cards affects the list and the underlying database so that both are in
 * sync. Uses the singleton pattern so that all Activities, Fragments, and classes can have access
 * to the cards, and do not need to keep a local copy in sync.
 */
public class TimeCardsManager {
    /**
     * @author - Matias Grioni
     * @created - 8/11/15
     *
     * A listener for any changes to the TimeCards list. This allows for Views
     * or Fragments, etc to implement it and to be updated when any changes
     * happen to the TimeCards list.
     */
    public interface TimeCardsListener {
        public void onCardsLoaded(List<TimeCard> cards);
        public void onCardAdded(TimeCard card);
        public void onCardDeleted(int position);
        public void onCardUpdated(int position, TimeCard card);
        public void onCardsUpdated(List<TimeCard> cards);
    }

    private static TimeCardsManager instance;
    private static TimeCardsListener cardsListener;
    private static List<TimeCard> cards;

    private SQLiteDatabase database;
    private TimeCardHelper tcHelper;
    private String[] columns = { TimeCardHelper.COLUMN_ID, TimeCardHelper.COLUMN_TYPE,
            TimeCardHelper.COLUMN_BACKCOUNT, TimeCardHelper.COLUMN_COLLAPSED };

    /**
     * Creates a TimeCardsManager instance if none exist or returns the currently existing one.
     *
     * @param context - An application context to create the instance.
     * @return - The current instance or a new one if none has been created yet.
     */
    public static TimeCardsManager getInstance(Context context) {
        if(instance == null) {
            instance = new TimeCardsManager(context);
            cards = new ArrayList<TimeCard>();
        }

        return instance;
    }

    /**
     * Set the listener object for this class.
     *
     * @param listener - The listener for the TimeCardsManager.
     */
    public static void setTimeCardsListener(TimeCardsListener listener) {
        cardsListener = listener;
    }

    /**
     * Sets the TimeCardsListener object to null so that the previous listener callbacks are no
     * longer called.
     */
    public static void removeTimeCardsListener() {
        cardsListener = null;
    }

    /**
     * Constructor.
     * @param context - An application context to create the TimeCardsManager object.
     */
    public TimeCardsManager(Context context) {
        tcHelper = new TimeCardHelper(context);
    }

    /**
     * Opens the underlying database and loads the cards saved in it.
     * @throws SQLException
     */
    public void open() throws SQLException {
        database = tcHelper.getWritableDatabase();
        loadCards();
    }

    /**
     * Every call to open must have a corresponding close to clean up resources.
     */
    public void close() {
        database.close();
    }

    /**
     * Loads the list of TimeCards in the database to the member variable list.
     * Calls the corresponding TimeCardsListener callback if the listener is
     * set.
     */
    public void loadCards() {
        // Clear the list of cards then query the database for all cards
        cards.clear();
        Cursor cursor = database.query(TimeCardHelper.TABLE_CARDS_NAME, columns,
                null, null, null, null, null);

        // Add all found cards to the list.
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            TimeCard card = cursorToCard(cursor);
            cards.add(card);
            cursor.moveToNext();
        }

        if (cardsListener != null)
            cardsListener.onCardsLoaded(cards);
    }

    /**
     * Adds a card to the list of cards and saves it to the database. Calls the
     * corresponding TimeCardsListener callback if the listener is set.
     *
     * @param card - The TimeCard to save.
     */
    public void addCard(TimeCard card) {
        cards.add(card);

        ContentValues values = new ContentValues();
        values.put(TimeCardHelper.COLUMN_TYPE, card.interval.name());
        values.put(TimeCardHelper.COLUMN_BACKCOUNT, card.backCount);
        values.put(TimeCardHelper.COLUMN_COLLAPSED, card.collapsed);

        database.insert(TimeCardHelper.TABLE_CARDS_NAME, null, values);

        if (cardsListener != null)
            cardsListener.onCardAdded(card);
    }

    /**
     * Removes the card at the given position from the list and the database.
     * Calls the corresponding TimeCardsListener callback if the listener is
     * set.
     *
     * @param position - The position of the TimeCard to remove.
     */
    public void remove(int position) {
        cards.remove(position);
        database.delete(TimeCardHelper.TABLE_CARDS_NAME,
                TimeCardHelper.COLUMN_ID + "=" + (position + 1), null);

        if (cardsListener != null)
            cardsListener.onCardDeleted(position);
    }

    /**
     * Change the card collapsed state of the card at the provided position and
     * save it in the database. Calls onCardUpdate if the TimeCardsListener is
     * set for the manager.
     *
     * @param position - The position of the card to update in the card list. position + 1 is the id
     *                 in the database.
     */
    public void changeCardState(int position) {
        TimeCard card = cards.get(position);
        card.collapsed = !card.collapsed;

        this.updateCard(position, card);
    }

    /**
     * Update the TimeCard in the list at the provided position and in the database corresponding to
     * the position. Calls onCardUpdate if the TimeCardsListener is set for the manager.
     *
     * @param position - The position of the TimeCard to replace in the TimeCard list and
     *                 (position + 1) is the id of the row for the TimeCard in the database.
     * @param card - The TimeCard to replace the old TimeCard with.
     */
    public void updateCard(int position, TimeCard card) {
        cards.set(position, card);

        // Update the position-th card in the table using the passed in card.
        ContentValues values = new ContentValues();
        values.put(TimeCardHelper.COLUMN_TYPE, card.interval.name());
        values.put(TimeCardHelper.COLUMN_BACKCOUNT, card.backCount);
        values.put(TimeCardHelper.COLUMN_COLLAPSED, card.collapsed ? 1 : 0);

        // Update the table, which starts id at 1, at the (position+1)th id, with the passed in card
        // information.
        database.update(TimeCardHelper.TABLE_CARDS_NAME, values,
                TimeCardHelper.COLUMN_ID + "=" + (position + 1), null);

        if (cardsListener != null)
            cardsListener.onCardUpdated(position, card);
    }

    /**
     * Update the cards for the manager to the passed in list. Calls onCardsUpdate of the
     * TimeCardsListener if it is set.
     *
     * @param cards - Sets the cards of the TimeCardsManager. Aliasing does not occur.
     */
    public void updateCards(List<TimeCard> cards) {
        this.cards = new ArrayList<TimeCard>(cards);

        if (cardsListener != null)
            cardsListener.onCardsUpdated(this.cards);
    }

    /**
     * Increments the card counters on this
     */
    public void incrementCards() {
        for(int i = 0; i < cards.size(); i++) {
            TimeCard card = cards.get(i);

            // Increment the total count for the card and also increment the last point of the card.
            card.count++;

            List<Integer> points = card.points;
            int lastPoint = points.get(points.size() - 1);
            points.set(points.size() - 1, ++lastPoint);
            card.points = points;

            this.updateCard(i, card);
        }
    }

    /**
     * The list of TimeCards.
     *
     * @return - The list of TimeCards.
     */
    public List<TimeCard> getCards() {
        return this.cards;
    }

    /**
     * The TimeCard at the provided position.
     *
     * @param position - The position at which the desired card is at.
     * @return - The TimeCard at the desired position.
     */
    public TimeCard getCard(int position) {
        return cards.get(position);
    }

    /**
     * Converts the given cursor and its information into a TimeCard.
     *
     * @param cursor - The current cursor to convert to a TimeCard.
     * @return - The TimeCard that was retrieved from the cursor.
     */
    private TimeCard cursorToCard(Cursor cursor) {
        TimeCard card = new TimeCard();
        card.interval = TimeInterval.valueOf(cursor.getString(1));
        card.backCount = cursor.getInt(2);

        int collapsed = cursor.getInt(3);
        card.collapsed = (collapsed != 0);

        return card;
    }
}
