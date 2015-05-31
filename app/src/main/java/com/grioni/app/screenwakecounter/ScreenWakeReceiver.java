package com.grioni.app.screenwakecounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Matias Grioni on 12/15/14.
 */
public class ScreenWakeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // The action is either for screen off and screen on as these
        // are the only actions for which this receiver should be
        // registered for.

        // Also when the device is turned on, this onReceive method will be called, so the service
        // will be started once the device is turned on.
        Intent screenStateIntent = new Intent(context, ScreenCountService.class);
        context.startService(screenStateIntent);
    }
}
