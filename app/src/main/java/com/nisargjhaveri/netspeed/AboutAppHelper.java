package com.nisargjhaveri.netspeed;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, context.getString(R.string.feedback_email));
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_subject));

        context.startActivity(Intent.createChooser(intent, "Send feedback using..."));
    }
}
