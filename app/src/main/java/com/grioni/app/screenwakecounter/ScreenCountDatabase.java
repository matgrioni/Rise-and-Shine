package com.grioni.app.screenwakecounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias Grioni on 12/16/14.
 */
public class ScreenCountDatabase {
    public static final int DAY_TO_HOUR = 24;
    public static final int WEEK_TO_DAY = 7;
    public static final int MONTH_TO_DAY = 30;

    private static ScreenCountDatabase instance;

    private SQLiteDatabase database;
    private TimeCounterHelper tcHelper;
    private String[] columns = { TimeCounterHelper.COLUMN_ID, TimeCounterHelper.COLUMN_COUNT };

    // The row size / length of the respective databases. The hour table is an
    // irregularity because once the first day passes the hour rows are reused
    // so the size is usually 24. So hourSize is really the current hour of the
    // day we are currently on.
    private static int hourSize = 0;
    private static int daySize = 0;
    private static int weekSize = 0;
    private static int monthSize = 0;

    /**
     *
     * @param context
     * @return
     */
    public static ScreenCountDatabase getInstance(Context context) {
        if(instance == null)
            instance = new ScreenCountDatabase(context);

        return instance;
    }

    /**
     *
     * @param context
     */
    public ScreenCountDatabase(Context context) {
        tcHelper = new TimeCounterHelper(context);
    }

    /**
     *
     */
    public void open() throws SQLException {
        database = tcHelper.getWritableDatabase();

        daySize = getTableSize(TimeCounterHelper.TABLE_DAY_NAME);
        weekSize = getTableSize(TimeCounterHelper.TABLE_WEEK_NAME);
        monthSize = getTableSize(TimeCounterHelper.TABLE_MONTH_NAME);
    }

    /**
     *
     */
    public void close() {
        database.close();
    }

    /**
     *
     * @param hourCount
     */
    public void addHour(int hourCount) {
        ContentValues values = new ContentValues();

        values.put("count", hourCount);
        updateTable(TimeInterval.Hour, values);
        hourSize++;

        if(hourSize > DAY_TO_HOUR - 1) {
            values.put("count", sumCount(TimeInterval.Hour));
            updateTable(TimeInterval.Day, values);

            hourSize = 0;
            daySize++;
        }

        if(daySize != 0 && hourSize == 0) {
            if(daySize % WEEK_TO_DAY == 0) {
                values.put("count", sumCount(TimeInterval.Day, WEEK_TO_DAY));
                updateTable(TimeInterval.Week, values);

                weekSize++;
            }

            if(daySize % MONTH_TO_DAY == 0) {
                values.put("count", sumCount(TimeInterval.Day, MONTH_TO_DAY));
                updateTable(TimeInterval.Month, values);

                monthSize++;
            }
        }
    }

    /**
     *
     * @param interval
     * @param backCount
     * @return
     */
    private int sumCount(TimeInterval interval, int backCount) {
        int sum = 0;
        String tableName = getTableName(interval);
        int lastIndex = getTableSize(interval);
        int start = (lastIndex - backCount < 0) ? 1 : lastIndex - backCount + 1;

        Cursor cursor = database.query(tableName, new String[] {TimeCounterHelper.COLUMN_COUNT},
                TimeCounterHelper.COLUMN_ID + " >= " + Integer.toString(start)
                + " and " + TimeCounterHelper.COLUMN_ID + " <= " + Integer.toString(lastIndex)
                , null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            sum += cursor.getInt(0);

            cursor.moveToNext();
        }

        cursor.close();
        return sum;
    }

    /**
     *
     * @param interval
     * @return
     */
    private int sumCount(TimeInterval interval) {
        return sumCount(interval, getTableSize(interval));
    }

    /**
     *
     * @param backCount
     * @param interval
     * @return
     */
    public List<Integer> getData(TimeInterval interval, int backCount) {
        int count = backCount;
        if(backCount == 1) {
            count = convertSingleton(interval);

            if(interval == TimeInterval.Day)
                interval = TimeInterval.Hour;
            else if(interval != TimeInterval.Hour)
                interval = TimeInterval.Day;
        }

        String table = getTableName(interval);
        List<Integer> data = new ArrayList<Integer>();
        int lastIndex = getTableSize(interval);
        int start = (lastIndex - (count - 1)) < 0 ? 1 : lastIndex - (count - 1) + 1;

        Cursor cursor = database.query(table, new String[] {TimeCounterHelper.COLUMN_COUNT},
                TimeCounterHelper.COLUMN_ID + " >= " + Integer.toString(start)
                + " and " + TimeCounterHelper.COLUMN_ID + " <= " + Integer.toString(lastIndex),
                null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            data.add(cursor.getInt(0));
            cursor.moveToNext();
        }

        int extra = ScreenCountService.getHourCount();
        if(interval != TimeInterval.Hour) {
            if(interval != TimeInterval.Day) {
                int intervalCount = daySize / convertSingleton(interval);
                extra += sumCount(TimeInterval.Day,
                        getTableSize(TimeInterval.Day) - intervalCount * convertSingleton(interval));
            }

            extra += sumCount(TimeInterval.Hour);
        }

        data.add(extra);
        cursor.close();

        return data;
    }

    /**
     *
     * @param interval
     * @param backCount
     * @return
     */
    public List<Integer> getCounts(TimeInterval interval, int backCount) {
        return getData(interval, backCount);
    }

    /**
     *
     * @param interval
     * @param values
     */
    private void updateTable(TimeInterval interval, ContentValues values) {
        int index = getTableSize(interval);
        String table = getTableName(interval);

        // Update the database with the screen wakes in the current hour. If the amount of rows,
        // updated is 0 that means it's the first day and therefore there is no id to update, it
        // must be inserted first to get the id.
        int rows = database.update(table, values,
                TimeCounterHelper.COLUMN_ID + "=" + Integer.toString(index + 1), null);
        if(rows == 0)
            database.insert(table, null, values);
    }

    /**
     *
     * @param interval
     * @return
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
     *
     * @param interval
     * @return
     */
    private int getTableSize(TimeInterval interval) {
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
     *
     * @param tableName
     * @return
     */
    private int getTableSize(String tableName) {
        Cursor cursor = database.query(tableName, columns, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    /**
     *
     * @param interval
     * @return
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
