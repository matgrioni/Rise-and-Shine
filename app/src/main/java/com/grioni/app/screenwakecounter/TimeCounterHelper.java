package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Matias Grioni on 12/16/14.
 */
public class TimeCounterHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "timecounter.db";

    public static final String TABLE_HOUR_NAME = "hour";
    public static final String TABLE_DAY_NAME = "day";
    public static final String TABLE_WEEK_NAME = "week";
    public static final String TABLE_MONTH_NAME = "month";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COUNT = "count";

    private static final String CREATE_TABLE_HOUR =
            "CREATE TABLE " + TABLE_HOUR_NAME + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_COUNT + " text not null);";

    private static final String CREATE_TABLE_DAY =
            "CREATE TABLE " + TABLE_DAY_NAME + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_COUNT + " text not null);";

    private static final String CREATE_TABLE_WEEK =
            "CREATE TABLE " + TABLE_WEEK_NAME + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_COUNT + " text not null);";

    private static final String CREATE_TABLE_MONTH =
            "CREATE TABLE " + TABLE_MONTH_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_COUNT + " text not null);";

    /**
     *
     * @param context
     */
    public TimeCounterHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_HOUR);
        db.execSQL(CREATE_TABLE_DAY);
        db.execSQL(CREATE_TABLE_WEEK);
        db.execSQL(CREATE_TABLE_MONTH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUR_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAY_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEEK_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONTH_NAME);

        onCreate(db);
    }
}