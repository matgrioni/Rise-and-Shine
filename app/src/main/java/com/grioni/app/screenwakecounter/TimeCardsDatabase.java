package com.grioni.app.screenwakecounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias Grioni on 1/6/15.
 */
public class TimeCardsDatabase {
    private static TimeCardsDatabase instance;

    private SQLiteDatabase database;
    private TimeCardHelper tcHelper;
    private String[] columns = { TimeCardHelper.COLUMN_ID, TimeCardHelper.COLUMN_TYPE,
            TimeCardHelper.COLUMN_BACKCOUNT, TimeCardHelper.COLUMN_COLLAPSED };

    /**
     *
     * @param context
     * @return
     */
    public static TimeCardsDatabase getInstance(Context context) {
        if(instance == null)
            instance = new TimeCardsDatabase(context);

        return instance;
    }

    /**
     *
     * @param context
     */
    public TimeCardsDatabase(Context context) {
        tcHelper = new TimeCardHelper(context);
    }

    /**
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        database = tcHelper.getWritableDatabase();
    }

    /**
     *
     */
    public void close() {
        database.close();
    }

    /**
     *
     * @param card
     */
    public void addCard(TimeCard card) {
        ContentValues values = new ContentValues();
        values.put(TimeCardHelper.COLUMN_TYPE, card.interval.name());
        values.put(TimeCardHelper.COLUMN_BACKCOUNT, card.backCount);
        values.put(TimeCardHelper.COLUMN_COLLAPSED, card.collapsed);

        database.insert(TimeCardHelper.TABLE_CARDS_NAME, null, values);
    }

    /**
     *
     * @param position
     * @param card
     */
    public void updateCard(int position, TimeCard card) {
        // Update the position-th card in the table using the passed in card.
        ContentValues values = new ContentValues();
        values.put(TimeCardHelper.COLUMN_TYPE, card.interval.name());
        values.put(TimeCardHelper.COLUMN_BACKCOUNT, card.backCount);
        values.put(TimeCardHelper.COLUMN_COLLAPSED, card.collapsed ? 1 : 0);

        // Update the table, which starts id at 1, at the (position+1)th id, with the passed in card
        // information.
        database.update(TimeCardHelper.TABLE_CARDS_NAME, values,
                TimeCardHelper.COLUMN_ID + "=" + (position + 1), null);
    }

    /**
     *
     * @param card
     * @return
     */
    public boolean existsCard(TimeCard card) {
        String[] columns = { TimeCardHelper.COLUMN_TYPE, TimeCardHelper.COLUMN_BACKCOUNT };
        String whereClause = TimeCardHelper.COLUMN_TYPE + "=" + card.interval.name() + " AND " +
                TimeCardHelper.COLUMN_BACKCOUNT + "=" + card.backCount;
        Cursor cursor = database.query(TimeCardHelper.TABLE_CARDS_NAME, columns, whereClause,
                null, null, null, null);

        return cursor.getCount() != 0;
    }

    /**
     *
     * @return
     */
    public List<TimeCard> getCards() {
        List<TimeCard> cards = new ArrayList<TimeCard>();
        Cursor cursor = database.query(TimeCardHelper.TABLE_CARDS_NAME, columns, null, null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            TimeCard card = cursorToCard(cursor);
            cards.add(card);
            cursor.moveToNext();
        }

        return cards;
    }

    /**
     *
     * @param position
     */
    public void remove(int position) {
        database.delete(TimeCardHelper.TABLE_CARDS_NAME, TimeCardHelper.COLUMN_ID + "=" + (position + 1), null);
    }

    /**
     *
     * @param cursor
     * @return
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
