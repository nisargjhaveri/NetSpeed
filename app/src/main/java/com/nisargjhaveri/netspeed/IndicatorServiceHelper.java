package com.nisargjhaveri.netspeed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

final class IndicatorServiceHelper {
    private static Intent getServiceIntent(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        Intent serviceIntent = new Intent(context, NetSpeedIndicatorService.class);

        // Add all preferences to intent
        for (Map.Entry<String, ?> entry : sharedPref.getAll().entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                serviceIntent.putExtra(entry.getKey(), (boolean)(Object)entry.getValue());
            } else if (entry.getValue() instanceof String) {
                serviceIntent.putExtra(entry.getKey(), (String)entry.getValue());
            }
        }

        return serviceIntent;
    }

    static void startService(Context context) {
        context.startService(getServiceIntent(context));

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Settings.KEY_INDICATOR_STARTED, true)
                .apply();
    }

    static void stopService(Context context) {
        context.stopService(new Intent(context, NetSpeedIndicatorService.class));

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(Settings.KEY_INDICATOR_STARTED, false)
                .apply();
    }
}