package com.grioni.app.screenwakecounter;

import android.app.AlarmManager;
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
import android.support.v4.app.NotificationManagerCompat;

import models.TimeInterval;

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

    private static final int SECONDS_TO_ALARM = 60;
    private static final int NOTIF_ID = 1337;

    private NotificationManagerCompat notifManager;

    private BroadcastReceiver wakeReceiver;
    private ScreenCountBinder countBinder = new ScreenCountBinder();

    private ScreenWakeListener screenWakeListener;

    private static int screenChangeCount;
    private static long lastAlarmTime;

    @Override
    public void onCreate() {
        super.onCreate();

        // IntentFilter for the actions of turning the screen on.
        IntentFilter wakeFilter = new IntentFilter();
        wakeFilter.addAction(Intent.ACTION_SCREEN_ON);

        wakeReceiver = new ScreenWakeReceiver();
        registerReceiver(wakeReceiver, wakeFilter);

        notifManager = NotificationManagerCompat.from(getBaseContext());

        startNotification();
        setScreenCountWriteAlarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        screenChangeCount++;
        if (screenWakeListener != null)
            screenWakeListener.onScreenWake();

        ScreenCountNotificationManager.refreshCount();

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
     * SystemClock#elapsedRealtime().
     *
     * @return - The elapsed real time figure of the last write time to the database.
     */
    public static long getLastAlarmTime() {
       return lastAlarmTime;
    }

    /**
     * Gets the amount of screen wakes in the last/current hour.
     *
     * @return - The screen wakes in the last hour.
     */
    public static int getHourCount() {
        return screenChangeCount;
    }

    public void updateNotif() {

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
     *
     */
    private void startNotification() {
        // When the service is first created check for any existing preferences for the notification
        // counts. If this is the first time the app is run, none will exist. However, if not and
        // the app has crashed and is being started again than some value will already exist here.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int backCount = Integer.parseInt(preferences.getString("pref_notification_backcount", "1"));
        String intervalStr = preferences.getString("pref_notification_interval", "Hour");
        TimeInterval interval = TimeInterval.valueOf(intervalStr);

        ScreenCountNotificationManager.setup(getBaseContext(), NOTIF_ID);
        startForeground(NOTIF_ID, ScreenCountNotificationManager.build());

        ScreenCountNotificationManager.updateInfo(interval, backCount);
    }

    /**
     *
     */
    private void setScreenCountWriteAlarm() {
        Intent alarmIntent = new Intent(getBaseContext(), ScreenCountWriteService.class);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + SECONDS_TO_ALARM * 1000, SECONDS_TO_ALARM * 1000,
                PendingIntent.getService(getBaseContext(), 0, alarmIntent, 0));
        lastAlarmTime = SystemClock.elapsedRealtime();
    }
}
