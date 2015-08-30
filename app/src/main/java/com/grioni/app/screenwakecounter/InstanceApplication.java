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
    protected TimeCardsManager cardsManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // No need to close, Android kernel will handle the closing at the end of the
        // application life.
        countDatabase = ScreenCountDatabase.getInstance(getApplicationContext());
        countDatabase.open();

        // Same as above. Android kernel will handle the closing at the end of the application life.
        // Doesn't really matter if it's open, because there is only one instance per application.
        // So it wouldn't hog resources to not close it when appropriate.
        cardsManager = TimeCardsManager.getInstance(getApplicationContext());
        cardsManager.open();
    }

    /**
     * Provides the ScreenCountDatabase instance for the application.
     *
     * @return - The ScreenCountDatabase instance for the application.
     */
    public ScreenCountDatabase getCountDatabase() {
        return this.countDatabase;
    }

    /**
     * Provides the TimeCardsManager instance for this application.
     *
     * @return - The TimeCardsManager instance for this application.
     */
    public TimeCardsManager getCardsManager() {
        return this.cardsManager;
    }
}
