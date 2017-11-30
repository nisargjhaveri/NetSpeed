package com.nisargjhaveri.netspeed.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.AttributeSet;

import com.nisargjhaveri.netspeed.R;

public final class SettingsFragment extends PreferenceFragment {
    private boolean mShowQuickSettings  = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mShowQuickSettings) {
            addPreferencesFromResource(R.xml.quick_preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        mShowQuickSettings = context.obtainStyledAttributes(attrs, R.styleable.SettingsFragment)
                .getBoolean(R.styleable.SettingsFragment_quick_settings, false);
    }
}