package com.grioni.app.screenwakecounter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import models.TimeInterval;

/**
 * Created by Matias Grioni on 2/18/15.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.setBackgroundColor(getResources().getColor(android.R.color.white));
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.settings).setVisible(false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        // If the settings that is changed is the notification count, then update the service notif
        // appropriately
        if (key.equals("pref_notification_backcount")) {
            int backcount = Integer.parseInt(preferences.getString(key, "1"));

            ScreenCountNotificationManager.updateBackcount(backcount);
        } else if (key.equals("pref_notification_interval")) {
            String preference = preferences.getString(key, "Hour");
            TimeInterval interval = TimeInterval.valueOf(preference);

            ScreenCountNotificationManager.updateInterval(interval);
        }
    }
}
