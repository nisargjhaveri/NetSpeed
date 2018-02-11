package com.nisargjhaveri.netspeed;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public final class AboutAppHelper {

    public static void rateApp(Context context) {
        String packageName = context.getPackageName();

        try {
            Intent goToMarket = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName));
            goToMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            );
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(
                    new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + packageName))
            );
        }
    }

    public static void sendFeedback(Context context) {
        String debugInfo = "\n\n\n---";
        debugInfo += "\nOS Version: " + System.getProperty("os.version") + " (" + Build.VERSION.INCREMENTAL + ")";
        debugInfo += "\nAndroid API: " + Build.VERSION.SDK_INT;
        debugInfo += "\nModel (Device): " + Build.MODEL + " ("+ Build.DEVICE + ")";
        debugInfo += "\nManufacturer: " + Build.MANUFACTURER;
        debugInfo += "\n---";

        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", context.getString(R.string.feedback_email), null));

        intent.putExtra(Intent.EXTRA_EMAIL, context.getString(R.string.feedback_email));
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_subject));
        intent.putExtra(Intent.EXTRA_TEXT, debugInfo);

        context.startActivity(Intent.createChooser(intent, "Send feedback using..."));
    }
}
