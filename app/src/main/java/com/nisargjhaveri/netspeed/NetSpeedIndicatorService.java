package com.nisargjhaveri.netspeed;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.IBinder;

public final class NetSpeedIndicatorService extends Service {
    private static final int NOTIFICATION_ID = 1;

    private Paint mIconSpeedPaint, mIconUnitPaint;
    private Bitmap mIconBitmap;
    private Canvas mIconCanvas;

    NotificationManager mNotificationManager;
    Notification.Builder mNotificationBuilder;

    private long mLastUsage = 0;
    private long mLastTime = 0;

    private Thread mThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        try {
            mThread.interrupt();
            mThread.join();
        } catch (InterruptedException e) {}

        stopForeground(true);
    }

    public void onCreate() {
        super.onCreate();

        setupIndicatorIconGenerator();
        setupNotifications();

        mThread = setupTimerThread();

        mLastUsage = TrafficStats.getTotalRxBytes() +TrafficStats.getTotalTxBytes();
        mLastTime = System.currentTimeMillis();

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        mThread.start();
    }

    private void updateNotification() {
        long currentUsage = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        long currentTime = System.currentTimeMillis();

        mNotificationBuilder
                .setSmallIcon(
                        getIndicatorIcon(
                                (currentUsage - mLastUsage) * 1000 / (currentTime - mLastTime)
                        )
                )
                .setPriority(Notification.PRIORITY_MAX);

        mNotificationManager
                .notify(NOTIFICATION_ID, mNotificationBuilder.build());

        mLastUsage = currentUsage;
        mLastTime = currentTime;
    }

    private void setupNotifications() {
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new Notification.Builder(this)
                .setSmallIcon(getIndicatorIcon(0))
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setLocalOnly(true);
    }

    private void setupIndicatorIconGenerator() {
        mIconSpeedPaint = new Paint();
        mIconSpeedPaint.setAntiAlias(true);
        mIconSpeedPaint.setTextSize(65);
        mIconSpeedPaint.setTextAlign(Paint.Align.CENTER);
        mIconSpeedPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));

        mIconUnitPaint = new Paint();
        mIconUnitPaint.setAntiAlias(true);
        mIconUnitPaint.setTextSize(40);
        mIconUnitPaint.setTextAlign(Paint.Align.CENTER);
        mIconUnitPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mIconBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ALPHA_8);

        mIconCanvas = new Canvas(mIconBitmap);
    }

    private Thread setupTimerThread() {
        // Setup background thread
        return new Thread() {
            long currentUsage, currentTime;

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        updateNotification();
                    }
                } catch (InterruptedException e) {}
            }
        };
    }

    private Icon getIndicatorIcon(long speed) {
        String speedValue;
        String speedUnit;

        if (speed < 1000000) {
            speedUnit = getString(R.string.kbps);
            speedValue = String.valueOf(speed / 1000);
        } else if (speed >= 1000000) {
            speedUnit = getString(R.string.mbps);

            if (speed < 10000000) {
                speedValue = String.format("%.1f", speed / 1000000.0);
            } else if (speed < 100000000) {
                speedValue = String.valueOf(speed / 1000000);
            } else {
                speedValue = getString(R.string.plus99);
            }
        } else {
            speedValue = getString(R.string.dash);
            speedUnit = getString(R.string.dash);
        }

        mIconCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mIconCanvas.drawText(speedValue, 48, 50, mIconSpeedPaint);
        mIconCanvas.drawText(speedUnit, 48, 95, mIconUnitPaint);

        return Icon.createWithBitmap(mIconBitmap);
    }
}