package com.nisargjhaveri.netspeed.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.nisargjhaveri.netspeed.R;

public final class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

}