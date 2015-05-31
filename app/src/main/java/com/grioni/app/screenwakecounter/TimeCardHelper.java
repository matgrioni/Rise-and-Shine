package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Matias Grioni on 1/6/15.
 */
public class TimeCardHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "timecards.db";
    public static final String TABLE_CARDS_NAME = "timecards";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_BACKCOUNT = "backcount";
    public static final String COLUMN_COLLAPSED = "collapsed";

    // TODO: text or integer for backcount
    public static final String CREATE_TABLE_CARDS =
            "CREATE TABLE " + TABLE_CARDS_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TYPE + " text not null ," + COLUMN_BACKCOUNT
            + " text not null, " + COLUMN_COLLAPSED + " integer);";

    /**
     *
     * @param context
     */
    public TimeCardHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CARDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS_NAME);
        onCreate(db);
    }
}
