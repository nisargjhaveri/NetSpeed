package com.nisargjhaveri.netspeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null ||
                intent.getAction() == null ||
                !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) return;

        Intent indicatorService = new Intent(context, NetSpeedIndicatorService.class);
        context.startService(indicatorService);
    }
}