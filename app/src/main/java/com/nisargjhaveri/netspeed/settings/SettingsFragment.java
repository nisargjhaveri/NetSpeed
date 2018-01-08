package com.nisargjhaveri.netspeed.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.nisargjhaveri.netspeed.IndicatorServiceHelper;
import com.nisargjhaveri.netspeed.R;

public final class SettingsFragment extends PreferenceFragment {
    private boolean mShowQuickSettings = false;

    private Context mContext;

    private SharedPreferences mSharedPref;

    private final SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (!key.equals(Settings.KEY_START_ON_BOOT)
                    && !key.equals(Settings.KEY_INDICATOR_STARTED)) {
                startIndicatorService();
            }
            if (key.equals(Settings.KEY_INDICATOR_ENABLED)
                    && !mSharedPref.getBoolean(Settings.KEY_INDICATOR_ENABLED, true)) {
                stopIndicatorService();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        if (mShowQuickSettings) {
            addPreferencesFromResource(R.xml.quick_preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        mShowQuickSettings = context.obtainStyledAttributes(attrs, R.styleable.SettingsFragment)
                .getBoolean(R.styleable.SettingsFragment_quick_settings, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPref.registerOnSharedPreferenceChangeListener(mSettingsListener);

        if (mSharedPref.getBoolean(Settings.KEY_INDICATOR_ENABLED, true)) {
            startIndicatorService();
        }
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