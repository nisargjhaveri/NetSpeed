package com.nisargjhaveri.netspeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null ||
                intent.getAction() == null ||
                !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (!sharedPreferences.getBoolean(SettingsFragment.KEY_START_ON_BOOT, true)
                || !sharedPreferences.getBoolean(SettingsFragment.KEY_INDICATOR_ENABLED, true)) {
            return;
        }

        Intent indicatorService = new Intent(context, NetSpeedIndicatorService.class);
        context.startService(indicatorService);
    }
}