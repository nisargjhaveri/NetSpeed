package com.nisargjhaveri.netspeed;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public final class SettingsFragment extends PreferenceFragment {

    public static final String KEY_INDICATOR_ENABLED = "indicatorEnabled";
    public static final String KEY_START_ON_BOOT = "startOnBoot";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

}