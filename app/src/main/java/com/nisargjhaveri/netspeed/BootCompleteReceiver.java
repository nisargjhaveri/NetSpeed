package com.nisargjhaveri.netspeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nisargjhaveri.netspeed.settings.Settings;

public final class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null ||
                intent.getAction() == null ||
                !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPreferences.edit()
                .putBoolean(Settings.KEY_INDICATOR_STARTED, false)
                .apply();

        if (!sharedPreferences.getBoolean(Settings.KEY_START_ON_BOOT, true)
                || !sharedPreferences.getBoolean(Settings.KEY_INDICATOR_ENABLED, true)) {
            return;
        }

        IndicatorServiceHelper.startService(context);
    }
}
