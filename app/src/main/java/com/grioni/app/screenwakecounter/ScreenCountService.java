package com.grioni.app.screenwakecounter;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Matias Grioni on 12/16/14.
 */
public class ScreenCountService extends Service {

    public class ScreenCountBinder extends Binder {
        public ScreenCountService getService() {
            return ScreenCountService.this;
        }
    }

    private static final int NOTIF_ID = 1337;
    private NotificationCompat.Builder notifBuilder;
    private NotificationManagerCompat notifManager;

    private ScreenCountDatabase countDatabase;
    private int backCount;
    private TimeInterval interval;
    private int notifCount;

    private BroadcastReceiver wakeReceiver;
    private ScreenCountBinder countBinder = new ScreenCountBinder();

    private ScreenWakeListener screenWakeListener;

    private static int screenChangeCount;
    private static long lastAlarmTime;

    @Override
    public void onCreate() {
        super.onCreate();

        countDatabase = ScreenCountDatabase.getInstance(getBaseContext());

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

        notifCount = getNotifCount();
        notifBuilder = new NotificationCompat.Builder(getBaseContext())
                .setContentTitle(getNotifLabel() + notifCount)
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

    /**
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        screenChangeCount++;
        if (screenWakeListener != null)
            screenWakeListener.onScreenWake();

        notifCount = getNotifCount();
        notifBuilder.setContentTitle(getNotifLabel() + notifCount);
        notifManager.notify(NOTIF_ID, notifBuilder.build());

        return START_STICKY;
    }

    /**
     * @return - null since clients can not bind to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return countBinder;
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        if(wakeReceiver != null)
            unregisterReceiver(wakeReceiver);

        // Count database is separate from lifecycle of
        countDatabase.close();
    }

    /**
     *
     * @param screenWakeListener
     */
    public void setScreenWakeListener(ScreenWakeListener screenWakeListener) {
        this.screenWakeListener = screenWakeListener;
    }

    /**
     *
     * @return
     */
    public static int getHourCount() {
        return screenChangeCount;
    }

    /**
     *
     */
    public static void reset() {
        screenChangeCount = 0;
        lastAlarmTime = SystemClock.elapsedRealtime();
    }

    /**
     *
     * @return
     */
    public static long getLastAlarmTime() {
       return lastAlarmTime;
    }

    /**
     *
     */
    public void updateNotif() {
        notifCount = getNotifCount();
        notifBuilder.setContentTitle(getNotifLabel() + notifCount);
        notifManager.notify(NOTIF_ID, notifBuilder.build());
    }

    /**
     *
     * @return
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
     *
     * @return
     */
    private int getNotifCount() {
        if(backCount == 1 && interval == TimeInterval.Hour) {
            return screenChangeCount;
        }

        int sum = 0;
        List<Integer> points = countDatabase.getCounts(interval, backCount);
        for(int i = 0; i < points.size(); i++)
            sum += points.get(i);

        return sum;
    }

    /**
     *
     * @param backCount
     */
    public void setNotifBackcount(int backCount) {
        this.backCount = backCount;
        updateNotif();
    }

    /**
     *
     * @param interval
     */
    public void setNotifInterval(TimeInterval interval) {
        this.interval = interval;
        updateNotif();
    }
}
