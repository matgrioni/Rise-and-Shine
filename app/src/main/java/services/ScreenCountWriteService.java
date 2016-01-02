package services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.grioni.app.screenwakecounter.InstanceApplication;
import com.grioni.app.screenwakecounter.ScreenCountDatabase;
import com.grioni.app.screenwakecounter.ScreenCountNotificationManager;

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
    private ServiceUpdateListener updateListener;
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

        if(updateListener != null)
            updateListener.onUpdate();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent service) {
        return binder;
    }

    /**
     *
     * @param updateListener
     */
    public void setUpdateListener(ServiceUpdateListener updateListener) {
        this.updateListener = updateListener;
    }
}
