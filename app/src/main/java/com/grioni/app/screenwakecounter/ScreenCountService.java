package com.grioni.app.screenwakecounter;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

/**
 * @author - Matias Grioni
 * @created - 12/16/14
 *
 * The continually running foreground service that keeps track of the current wakes in the hour. At
 * the end of this hour, the ScreenWriteService is called to add the finished hour to the database.
 */
public class ScreenCountService extends Service {
    /**
     * @author - Matias Grioni
     * @created - 12/16/14
     *
     * A wrapper around this Service so that when it is bound to an activity the Service can be
     * retrieved.
     */
    public class ScreenCountBinder extends Binder {
        public ScreenCountService getService() {
            return ScreenCountService.this;
        }
    }

    private static final int NOTIF_ID = 1337;
    private NotificationCompat.Builder notifBuilder;
    private NotificationManagerCompat notifManager;

    // The database needed to get the counts for the notification.
    private ScreenCountDatabase countDatabase;
    private int backCount;
    private TimeInterval interval;

    private BroadcastReceiver wakeReceiver;
    private ScreenCountBinder countBinder = new ScreenCountBinder();

    private ScreenWakeListener screenWakeListener;

    private static int screenChangeCount;
    private static long lastAlarmTime;

    @Override
    public void onCreate() {
        super.onCreate();

        countDatabase = ((InstanceApplication) getApplication()).getCountDatabase();

        // When the service is first created check for any existing preferences for the notification
        // counts. If this is the first time the app is run, none will exist. However, if not and
        // the app has crashed and is being started again than some value will already exist here.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String notifDesc = preferences.getString("pref_notification_counts", "1 Hour");

        // Parse the provided into the number and the interval.
        int split = notifDesc.indexOf(" ");
        backCount = Integer.parseInt(notifDesc.substring(0, split));
        interval = TimeInterval.valueOf(notifDesc.substring(split + 1));

        // IntentFilter for the actions of turning on and off the screen.
        IntentFilter wakeFilter = new IntentFilter();
        wakeFilter.addAction(Intent.ACTION_SCREEN_ON);

        wakeReceiver = new ScreenWakeReceiver();
        registerReceiver(wakeReceiver, wakeFilter);

        notifManager = NotificationManagerCompat.from(getBaseContext());

        // Start the service in the foreground so that it is not destroyed easily, and also provides
        // a notification for the user to see their screen wakes.
        Intent notifIntent = new Intent(getBaseContext(), MainActivity.class);
        PendingIntent pendingIntent  = PendingIntent.getActivity(getBaseContext(), 0, notifIntent, 0);

        notifBuilder = new NotificationCompat.Builder(getBaseContext())
                .setContentTitle(getNotifLabel() + getNotifCount())
                .setSmallIcon(R.drawable.ic_notif)
                .setColor(0x039be5)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MIN);

        startForeground(NOTIF_ID, notifBuilder.build());

        Intent alarmIntent = new Intent(getBaseContext(), ScreenCountWriteService.class);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60 * 60 * 1000, 60 * 60 * 1000,
                PendingIntent.getService(getBaseContext(), 0, alarmIntent, 0));
        lastAlarmTime = SystemClock.elapsedRealtime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        screenChangeCount++;
        if (screenWakeListener != null)
            screenWakeListener.onScreenWake();

        notifBuilder.setContentTitle(getNotifLabel() + getNotifCount());
        notifManager.notify(NOTIF_ID, notifBuilder.build());

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return countBinder;
    }

    @Override
    public void onDestroy() {
        if(wakeReceiver != null)
            unregisterReceiver(wakeReceiver);
    }

    /**
     * If a callback should be run every time the screen is woken set the callback. Allows for the
     * Acvitity/Fragment that the Service is bound to communicate together.
     *
     * @param screenWakeListener - The listener to use.
     */
    public void setScreenWakeListener(ScreenWakeListener screenWakeListener) {
        this.screenWakeListener = screenWakeListener;
    }

    /**
     * Get the last time the database was written to. The number returned is in the format of
     * SystemClock.elapsedRealtime().
     *
     * @return - The elapsed real time figure of the last write time to the database.
     */
    public static long getLastAlarmTime() {
       return lastAlarmTime;
    }

    /**
     * Updates the notification counter with the correct count and label.
     */
    public void updateNotif() {
        notifBuilder.setContentTitle(getNotifLabel() + getNotifCount());
        notifManager.notify(NOTIF_ID, notifBuilder.build());
    }

    /**
     * Gets the amount of screen wakes in the last/current hour.
     *
     * @return - The screen wakes in the last hour.
     */
    public static int getHourCount() {
        return screenChangeCount;
    }

    /**
     * Sets the screen wakes for the last hour to 0 and sets the last write time to the database to
     * the current time.
     */
    public static void reset() {
        screenChangeCount = 0;
        lastAlarmTime = SystemClock.elapsedRealtime();
    }

    /**
     * Get the label (the part before the colon in the notification) for this Service.
     *
     * @return - The label for the Notification for this Service.
     */
    private String getNotifLabel() {
        String label = "Last ";
        if(backCount != 1)
            label += backCount + " " + interval.name() + "s: ";
        else
            label += interval.name() + ": ";

        return label;
    }

    /**
     * Get the amount of screen wakes for the interval and back count specified for this Service.
     *
     * @return - The amount of screen wakes to display for the notification.
     */
    private int getNotifCount() {
        if(backCount == 1 && interval == TimeInterval.Hour)
            return screenChangeCount;

        return countDatabase.getCount(interval, backCount);
    }

    /**
     * Set the back count for the Notification and update the notification after.
     *
     * @param backCount  - The new back count for the notification.
     */
    public void setNotifBackcount(int backCount) {
        this.backCount = backCount;
        updateNotif();
    }

    /**
     * Set the TimeInterval for the Notification and update the notification after.
     *
     * @param interval - The new TimeInterval for the notification.
     */
    public void setNotifInterval(TimeInterval interval) {
        this.interval = interval;
        updateNotif();
    }
}
