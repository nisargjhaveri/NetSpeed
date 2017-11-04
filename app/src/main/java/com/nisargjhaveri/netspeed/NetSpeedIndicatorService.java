package com.nisargjhaveri.netspeed;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.IBinder;
import android.view.View;

public final class NetSpeedIndicatorService extends Service {
    private static final int NOTIFICATION_ID = 1;

    private IndicatorIconView mIconView;
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

        setupIndicatorIconView();
        setupNotifications();

        mThread = setupTimerThread();

        mLastUsage = TrafficStats.getTotalRxBytes() +TrafficStats.getTotalTxBytes();
        mLastTime = System.currentTimeMillis();

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        mThread.start();
    }

    private void updateNotification(long speed) {
        mNotificationBuilder
                .setSmallIcon(getIndicatorIcon(speed))
                .setPriority(Notification.PRIORITY_MAX);

        mNotificationManager
                .notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void setupNotifications() {
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new Notification.Builder(this)
                .setSmallIcon(getIndicatorIcon(0))
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setLocalOnly(true);
    }

    private void setupIndicatorIconView() {
        mIconView = new IndicatorIconView(this);

        mIconView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        mIconView.layout(0, 0, mIconView.getMeasuredWidth(), mIconView.getMeasuredHeight());

        mIconBitmap = Bitmap.createBitmap(
                mIconView.getMeasuredWidth(),
                mIconView.getMeasuredHeight(),
                Bitmap.Config.ALPHA_8
        );
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

                        currentUsage = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
                        currentTime = System.currentTimeMillis();

                        updateNotification((currentUsage - mLastUsage) * 1000 / (currentTime - mLastTime));

                        mLastUsage = currentUsage;
                        mLastTime = currentTime;
                    }
                } catch (InterruptedException e) {}
            }
        };
    }

    private Icon getIndicatorIcon(long speed) {
        System.out.println(speed);

        mIconView.setSpeed(speed);

        mIconCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mIconView.draw(mIconCanvas);

        return Icon.createWithBitmap(mIconBitmap);
    }
}