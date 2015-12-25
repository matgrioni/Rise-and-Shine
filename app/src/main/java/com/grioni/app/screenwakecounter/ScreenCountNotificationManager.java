package com.grioni.app.screenwakecounter;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import models.TimeInterval;
import utils.LabelUtils;

/**
 * @author - Matias Grioni
 * @created - 12/23/15
 *
 * A manager for the persistent application notification that displays the current screen wakes
 * for a specific amount of time defined in the settings.
 */
public class ScreenCountNotificationManager {
    private static Context context;

    private static NotificationCompat.Builder builder;
    private static NotificationManagerCompat manager;
    private static ScreenCountDatabase countDatabase;

    private static int NOTIF_ID;

    private static int icon;
    private static int bgcolor;

    private static String label;
    private static int count;

    private static TimeInterval interval;
    private static int backCount;

    /**
     * Setup up the current notification, using the given {@code context} and {@code id}. The id is
     * the unique notification that will be used as the id for this notification. The setup allows
     * for future methods manipulating the notification to be called.
     *
     * @param _context - The context to use for notification creation and management.
     * @param id
     */
    public static void setup(Context _context, int id) {
        context = _context;

        builder = new NotificationCompat.Builder(context);
        manager = NotificationManagerCompat.from(context);
        countDatabase = ((InstanceApplication) context.getApplicationContext()).getCountDatabase();

        NOTIF_ID = id;

        interval = TimeInterval.Hour;
        backCount = 1;
        updateLabelAndCount();

        initResources();
        setupBuilder();
    }

    /**
     * Updates the notification with the current notification builder values.
     */
    public static void update() {
        manager.notify(NOTIF_ID, builder.build());
    }

    /**
     * Requeries the count database and updates the notification text.
     */
    public static void refreshCount() {
        count = countDatabase.getCount(interval, backCount);
        updateLabelAndCount();
        update();
    }

    /**
     * Updates both the {@code interval} and the {@code backCount}, rather than calling each
     * individual methods and using two database calls.
     *
     * @param _interval - The new interval for the notification.
     * @param _backCount - The new backcount for the notification.
     */
    public static void updateInfo(TimeInterval _interval, int _backCount) {
        interval = _interval;
        backCount = _backCount;
        updateLabelAndCount();
        update();
    }

    /**
     * Updates the {@code interval} for the notification and updates the notification itself too.
     *
     * @param _interval - The new interval for the notification.
     */
    public static void updateInterval(TimeInterval _interval) {
        interval = _interval;
        updateLabelAndCount();
        update();
    }

    /**
     * Updates {@code backCount} for the notification and updates the notification itself too.
     *
     * @param _backCount - The new backCount for the notification.
     */
    public static void updateBackcount(int _backCount) {
        backCount = _backCount;
        updateLabelAndCount();
        update();
    }

    /**
     * A wrapper around the build call to NotificationCompat.Builder#build.
     *
     * @return The created notification.
     */
    public static Notification build() {
        return builder.build();
    }

    /**
     * Updates the label and count fields for the notification and updates the corresponding text
     * in the notification.
     */
    private static void updateLabelAndCount() {
        label = LabelUtils.last(interval, backCount);
        count = countDatabase.getCount(interval, backCount);

        builder.setContentTitle(label + count);
    }

    /**
     * Assigns values to the resources for the notifications.
     */
    private static void initResources() {
        bgcolor = context.getResources().getColor(R.color.color_primary);
        icon = R.drawable.ic_notif;
    }

    /**
     * Setup all the fields of the notification using the notification builder. The only field
     * that is not set through here is the content title. This is set when the label and count are
     * updated through ScreenCountNotificationManager#updateLabelAndCount.
     */
    private static void setupBuilder() {
        Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent  = PendingIntent.getActivity(context, 0, notifIntent, 0);

        builder.setSmallIcon(icon)
               .setColor(bgcolor)
               .setContentIntent(pendingIntent)
               .setPriority(Notification.PRIORITY_MIN);
    }
}