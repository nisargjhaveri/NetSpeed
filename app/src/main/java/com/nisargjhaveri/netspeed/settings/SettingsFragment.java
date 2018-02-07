package com.nisargjhaveri.netspeed.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.nisargjhaveri.netspeed.IndicatorServiceHelper;
import com.nisargjhaveri.netspeed.R;

public final class SettingsFragment extends PreferenceFragment {
    private SharedPreferences mSharedPref;
    private Context mContext;

    private final SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Settings.KEY_INDICATOR_ENABLED)) {
                if (mSharedPref.getBoolean(Settings.KEY_INDICATOR_ENABLED, true)) {
                    startIndicatorService();
                } else {
                    stopIndicatorService();
                }
            } else if (!key.equals(Settings.KEY_START_ON_BOOT)
                    && !key.equals(Settings.KEY_INDICATOR_STARTED)) {
                startIndicatorService();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        addPreferencesFromResource(R.xml.preferences);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (mSharedPref.getBoolean(Settings.KEY_INDICATOR_ENABLED, true)) {
            startIndicatorService();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPref.registerOnSharedPreferenceChangeListener(mSettingsListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSharedPref.unregisterOnSharedPreferenceChangeListener(mSettingsListener);
    }

    private void startIndicatorService() {
        IndicatorServiceHelper.startService(mContext);
    }

    private void stopIndicatorService() {
        IndicatorServiceHelper.stopService(mContext);
    }

}
