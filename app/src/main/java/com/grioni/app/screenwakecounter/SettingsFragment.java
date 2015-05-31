package com.grioni.app.screenwakecounter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Matias Grioni on 2/18/15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) activity);
    }

    @Override
    public void onPause() {
        super.onPause();

        Activity activity = getActivity();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) activity);
    }
}
