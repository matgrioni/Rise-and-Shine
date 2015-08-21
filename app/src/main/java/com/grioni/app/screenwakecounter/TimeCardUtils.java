package com.grioni.app.screenwakecounter;

import android.content.Context;

import java.util.List;

/**
 * @author - Matias Grioni
 * @created - 8/15/15
 *
 * A class that defines static methods to query the ScreenCountDatabase given
 * the info from a TimeCard. For instance, a TimeCard may be defined as having
 * an interval of a Week, with a backcount of 5, and a collapsed state. To get
 * the data of the count total for this time period and the data points, the
 * ScreenCountDatabase must be used. This class simplifies its access.
 */
public class TimeCardUtils {
    private static ScreenCountDatabase countDatabase;

    /**
     * Initializes the TimeCardUtils class by getting the ScreenCountDatabase
     * object.
     *
     * @param context - The Context object with which to get the
     *                ScreenCountDatabase.
     */
    public static void init(Context context) {
        countDatabase = ScreenCountDatabase.getInstance(context);
    }

    /**
     * Returns the amount of screen wakes for the defined TimeCard.
     *
     * @param card - The TimeCard to find the amount of screen wakes for.
     * @return - The amount of screen wakes for the TimeCard.
     */
    public static int getCount(TimeCard card) {
        int count = countDatabase.getCount(card.interval, card.backCount);
        return count;
    }

    /**
     * Returns a list of the points for the given TimeCard.
     *
     * @param card - The given TimeCard to find the points of.
     * @return - The list of points of screen wakes for the given TimeCard.
     */
    public static List<Integer> getPoints(TimeCard card) {
        List<Integer> points = countDatabase.getEntries(card.interval, card.backCount);
        return points;
    }
}
