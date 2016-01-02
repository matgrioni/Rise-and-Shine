package com.grioni.app.screenwakecounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import models.TimeInterval;
import services.ScreenCountService;

/**
 * @author - Matias Grioni
 * @created - 12/16/14
 *
 * Access to the database that keeps track of the screen wakes per hour, day, week, and month. Given
 * a TimeInterval and how far back to go, the ScreenCountDatabase instance can return the list of
 * points for that time period. This class uses a singleton instance.
 */
public class ScreenCountDatabase {
    public static final int DAY_TO_HOUR = 4;
    public static final int WEEK_TO_DAY = 3;
    public static final int MONTH_TO_DAY = 4;

    private static ScreenCountDatabase instance;

    private SQLiteDatabase database;
    private TimeCounterHelper tcHelper;
    private String[] columns = { TimeCounterHelper.COLUMN_ID, TimeCounterHelper.COLUMN_COUNT };

    // The row size / length of the respective tables. The hour table is an
    // irregularity because once the first day passes the hour rows are reused
    // so the size is usually 24. So hourSize is really the current hour of the
    // day we are currently on.
    private static int hourSize = 0;
    private static int daySize = 0;
    private static int weekSize = 0;
    private static int monthSize = 0;

    /**
     * Create a new instance or get the already created instance for ScreenCountDatabase.
     *
     * @param context - The Context to create the ScreenCountDatabase instance with.
     * @return - The ScreenCountDatabase instance.
     */
    public static ScreenCountDatabase getInstance(Context context) {
        if(instance == null)
            instance = new ScreenCountDatabase(context);

        return instance;
    }

    /**
     * Creates a new ScreenCountDatabase object. To access the global instance use the
     * getInstance method.
     *
     * @param context - The Context to create the database object with.
     */
    private ScreenCountDatabase(Context context) {
        tcHelper = new TimeCounterHelper(context);
    }

    /**
     * Should be called before any data retrieval or modifications to the database. Opens the
     * database.
     *
     * @throws SQLException - If the database can not be opened for writing.
     */
    public void open() throws SQLException {
        database = tcHelper.getWritableDatabase();

        // Get the current table size. Important if the app crashed or was closed and it needs to be
        // restarted from where it was left off.
        hourSize = queryTableSize(TimeCounterHelper.TABLE_HOUR_NAME);
        daySize = queryTableSize(TimeCounterHelper.TABLE_DAY_NAME);
        weekSize = queryTableSize(TimeCounterHelper.TABLE_WEEK_NAME);
        monthSize = queryTableSize(TimeCounterHelper.TABLE_MONTH_NAME);
    }

    /**
     * Closes the database once it's not needed anymore. If the database is to be used again a call
     * to open must follow.
     */
    public void close() {
        database.close();
    }

    /**
     * Puts the number in the correct location in the hour table. If it is currently the 7th hour
     * then adding an hour will put the number in the 7th position of the hour table. If the hour is
     * the 24th hour then the day counter will be incremented. If the day counter is on the 7th day
     * then the week counter will be incremented and so on.
     *
     * Handles all the logic behind moving up in the table based on one more hour being added.
     *
     * @param hourCount - The number of screen wakes in the last hour to put in the table.
     */
    public void addHour(int hourCount) {
        ContentValues values = new ContentValues();

        values.put("count", hourCount);
        values.put("_id", hourSize + 1);
        updateTable(TimeInterval.Hour, values);
        hourSize++;

        // Once we reach the DAY_TO_HOUR number or more sum the last hours starting from now and
        // this sum of all the hours in the last day is put into the day table.
        if(hourSize > DAY_TO_HOUR - 1) {
            values.put("count", sumIntervalEntries(TimeInterval.Hour));
            values.put("_id", daySize + 1);
            updateTable(TimeInterval.Day, values);

            database.delete(TimeCounterHelper.TABLE_HOUR_NAME, null, null);
            hourSize = 0;
            daySize++;
        }

        // If this is the first day then day % WEEK_TO_DAY will return 0 so check against that.
        // Second we have to make sure this is the start of the day with hourSize == 0. If
        // day % WEEK_TO_DAY == 0, it will for all hours of that day, so we have to make sure we
        // only add it to the week table once. The same goes for the month check.
        if (daySize != 0 && hourSize == 0) {
            if(daySize % WEEK_TO_DAY == 0) {
                values.put("count", sumIntervalEntries(TimeInterval.Day, WEEK_TO_DAY));
                values.put("_id", weekSize + 1);
                updateTable(TimeInterval.Week, values);

                weekSize++;
            }

            if(daySize % MONTH_TO_DAY == 0) {
                values.put("count", sumIntervalEntries(TimeInterval.Day, MONTH_TO_DAY));
                values.put("_id", monthSize + 1);
                updateTable(TimeInterval.Month, values);

                monthSize++;
            }
        }
    }

    /**
     * Queries the corresponding TimeInterval and sums the last {@code backCount} entries in that table.
     * Result is equivalent to summing the items in the list from getEntries.
     *
     * @param interval - The TimeInterval whose corresponding table to query for the entries.
     * @param backCount - How far back to go in the table including the current entry.
     * @return - The sum of all the selected entries in the table for the TimeInterval.
     */
    public int getCount(TimeInterval interval, int backCount) {
        if(backCount == 1) {
            backCount = convertSingleton(interval);

            if(interval == TimeInterval.Day)
                interval = TimeInterval.Hour;
            else if(interval != TimeInterval.Hour)
                interval = TimeInterval.Day;
        }

        // Add together the TimeInterval entries that have already occurred and are written to the
        // database. Would not include current TimeInterval.
        int entryCount = getEntryCount(interval);
        int start = (entryCount - (backCount - 1)) < 0 ? 1 : entryCount - (backCount - 1) + 1;
        Cursor cursor = getEntriesCursor(interval, start);
        cursor.moveToFirst();

        int sum = 0;
        while(!cursor.isAfterLast()) {
            sum += cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();

        sum += currentIntervalCount(interval);

        return sum;
    }

    /**
     * Queries the tables for the last desired entries, including the current entry, for the
     * TimeInterval. Returns a list of all the entries for that TimeInterval, with the item order
     * being chronological. The last point is the current entry.
     *
     * @param interval - The TimeInterval whose corresponding table to query.
     * @param backCount - How far back to go in the table including the current entry.
     * @return - A list of the entries with a length of backCount corresponding to the table for
     *         TimeInterval.
     */
    public List<Integer> getEntries(TimeInterval interval, int backCount) {
        if(backCount == 1) {
            backCount = convertSingleton(interval);

            if(interval == TimeInterval.Day)
                interval = TimeInterval.Hour;
            else if(interval != TimeInterval.Hour)
                interval = TimeInterval.Day;
        }

        // Add together the TimeInterval entries that have already occurred and are written to the
        // database. Would not include current TimeInterval.
        int entryCount = getEntryCount(interval);
        int start = (entryCount - (backCount - 1)) < 0 ? 1 : entryCount - (backCount - 1) + 1;
        Cursor cursor = getEntriesCursor(interval, start);
        cursor.moveToFirst();

        List<Integer> data = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            data.add(cursor.getInt(0));
            cursor.moveToNext();
        }
        cursor.close();

        data.add(currentIntervalCount(interval));

        return data;
    }

    /**
     * Gets the current screen wake count for the desired interval.
     *
     * @param interval - The TimeInterval to find the current screen wake count for.
     * @return - The current screen wake count for a given TimeInterval.
     */
    private int currentIntervalCount(TimeInterval interval) {
        // Sum up the current TimeInterval. If we want the hour points, then the last hour is only
        // the ScreenCountService count. If the interval is a day then we have to include all the
        // current hours of this day in the count. If it's a week or month, we have to include the
        // the days leading up to the current day.
        int current = ScreenCountService.getHourCount();
        if(interval != TimeInterval.Hour) {
            if(interval != TimeInterval.Day) {
                int intervalCount = daySize / convertSingleton(interval);
                current += sumIntervalEntries(TimeInterval.Day,
                        getEntryCount(TimeInterval.Day) - intervalCount * convertSingleton(interval));
            }

            current += sumIntervalEntries(TimeInterval.Hour);
        }

        return current;
    }

    /**
     * Gives a cursor that queries the table for the given TimeInterval starting from the row with
     * an id of start until the end of the table.
     *
     * @param interval - The TimeInterval whose corresponding table to query.
     * @param start - The id of the first row in the cursor. 1 is the first possible value.
     * @return - The cursor object that will transverse rows in the table for the TimeInterval from
     *         the row with an id of start to the last entry.
     */
    private Cursor getEntriesCursor(TimeInterval interval, int start) {
        int end = getEntryCount(interval);
        String table = getTableName(interval);

        return database.query(table, new String[] {TimeCounterHelper.COLUMN_COUNT},
                TimeCounterHelper.COLUMN_ID + " >= " + Integer.toString(start)
                + " and " + TimeCounterHelper.COLUMN_ID + " <= " + Integer.toString(end),
                null, null, null, null);
    }

    /**
     * Sums the desired last entries for a provided table type in the database. This sum does not
     * include the current hour/day/month figure, only what has already been written to the database
     * and is not currently happening. This is more a convenience method
     *
     * @param interval - The TimeInterval whose corresponding table to query.
     * @param backCount - How far back to go in the table not including the current entry.
     * @return - The sum of the desired entries.
     */
    private int sumIntervalEntries(TimeInterval interval, int backCount) {
        int entryCount = getEntryCount(interval);
        int start = (entryCount - backCount < 0) ? 1 : entryCount - backCount + 1;

        Cursor cursor = getEntriesCursor(interval, start);
        cursor.moveToFirst();

        int sum = 0;
        while(!cursor.isAfterLast()) {
            sum += cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        return sum;
    }

    /**
     * Sums all the entries for the provided TimeInterval.
     *
     * @param interval - The TimeInterval for which to sum all the entries in the corresponding
     *                 table.
     * @return - The sum of all the entries in the corresponding table for the TimeInterval.
     */
    private int sumIntervalEntries(TimeInterval interval) {
        return sumIntervalEntries(interval, getEntryCount(interval));
    }

    /**
     * Adds the provided ContentValues to the table for the TimeInterval. If the table is cyclical
     * then the row will be overwritten when needed, for example with the hour table.
     *
     * @param interval - The table to update.
     * @param values - The ContentValues object which has the amount of wakes to add to the table.
     */
    private void updateTable(TimeInterval interval, ContentValues values) {
        String table = getTableName(interval);

        // Update the database with the screen wakes in the current hour. If the amount of rows,
        // updated is 0 that means it's the first day and therefore there is no id to update, it
        // must be inserted first to get the id.
        database.insert(table, null, values);
    }

    /**
     * Given a TimeInterval, returns the table name corresponding to it.
     *
     * @param interval - The TimeInterval to get the table name for.
     * @return - A string of the table name in the database.
     */
    private String getTableName(TimeInterval interval) {
        if(interval == TimeInterval.Hour)
            return TimeCounterHelper.TABLE_HOUR_NAME;
        else if(interval == TimeInterval.Day)
            return TimeCounterHelper.TABLE_DAY_NAME;
        else if(interval == TimeInterval.Week)
            return TimeCounterHelper.TABLE_WEEK_NAME;
        else if(interval == TimeInterval.Month)
            return  TimeCounterHelper.TABLE_MONTH_NAME;

        return "";
    }

    /**
     * Gets the amount of relevant entries for the TimeInterval. For example if there are 7 weeks
     * written so far to the database then 7 is returned when TimeInterval.Week is given. However,
     * for a table like the hour table, it does not return 24, the usual size, but the current hours
     * passed in the current day. Just a wrapper around getting hourSize, weekSize, etc, based on
     * the TimeInterval.
     *
     * @param interval - The TimeInterval to get the entry counts for.
     * @return - The amount of relevant entries for that TimeInterval.
     */
    private int getEntryCount(TimeInterval interval) {
        if(interval == TimeInterval.Hour)
            return hourSize;
        else if(interval == TimeInterval.Day)
            return daySize;
        else if(interval == TimeInterval.Week)
            return weekSize;
        else if(interval == TimeInterval.Month)
            return monthSize;

        return -1;
    }

    /**
     * Gets the table size for the provided table name by querying all rows and counting how many
     * matched.
     *
     * @param tableName - The table to get the size of.
     * @return - The size of the table.
     */
    private int queryTableSize(String tableName) {
        Cursor cursor = database.query(tableName, columns, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     * Returns the conversion factor between a TimeInterval and it's next smallest unit.
     *
     * @param interval - The TimeInterval to convert.
     * @return - The conversion answer between the TimeInterval and the next smallest TimeInterval.
     */
    private int convertSingleton(TimeInterval interval) {
        if(interval == TimeInterval.Day)
            return DAY_TO_HOUR;
        else if(interval == TimeInterval.Week)
            return WEEK_TO_DAY;
        else if(interval == TimeInterval.Month)
            return MONTH_TO_DAY;

        return 1;
    }
}
