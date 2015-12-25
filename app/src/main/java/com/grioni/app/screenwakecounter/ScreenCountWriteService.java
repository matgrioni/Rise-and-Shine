package com.grioni.app.screenwakecounter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by Matias Grioni on 12/21/14.
 */
public class ScreenCountWriteService extends Service {

    public class ScreenCountWriteBinder extends Binder {
        public ScreenCountWriteService getService() {
            return ScreenCountWriteService.this;
        }
    }

    private ScreenCountWriteBinder binder = new ScreenCountWriteBinder();
    private WriteListener writeListener;
    private ScreenCountDatabase countDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        countDatabase = ((InstanceApplication) getApplication()).getCountDatabase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int hourCount = ScreenCountService.getHourCount();
        countDatabase.addHour(hourCount);

        ScreenCountService.reset();
        ScreenCountNotificationManager.refreshCount();

        if(writeListener != null)
            writeListener.onWrite();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent service) {
        return binder;
    }

    /**
     *
     * @param writeListener
     */
    public void setWriteListener(WriteListener writeListener) {
        this.writeListener = writeListener;
    }
}
