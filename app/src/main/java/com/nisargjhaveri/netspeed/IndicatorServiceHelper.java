package com.nisargjhaveri.netspeed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class IndicatorServiceHelper {
    public static Intent getServiceIntent(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        Intent serviceIntent = new Intent(context, NetSpeedIndicatorService.class);

        serviceIntent.putExtra(
                Settings.KEY_SHOW_SETTINGS_BUTTON,
                sharedPref.getBoolean(Settings.KEY_SHOW_SETTINGS_BUTTON, false)
        );
        serviceIntent.putExtra(
                Settings.KEY_NOTIFICATION_PRIORITY,
                sharedPref.getString(Settings.KEY_NOTIFICATION_PRIORITY, "max")
        );

        return serviceIntent;
    }
}