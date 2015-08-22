package com.grioni.app.screenwakecounter;

import android.app.Application;

/**
 * @author - Matias Grioni
 * @created - 8/22/15
 *
 * Custom Application class to handle the singleton instances in the application. This makes sure
 * the resource management is handled from the beginning of the application.
 */
public class InstanceApplication extends Application {
    protected ScreenCountDatabase countDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        // No need to close, Android kernel will handle the closing at the end of the
        // application life.
        countDatabase = ScreenCountDatabase.getInstance(getApplicationContext());
        countDatabase.open();
    }

    /**
     * Provides the ScreenCountDatabase instance for the application.
     *
     * @return - The ScreenCountDatabase instance for the application.
     */
    public ScreenCountDatabase getCountDatabase() {
        return this.countDatabase;
    }
}
