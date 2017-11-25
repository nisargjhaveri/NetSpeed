package com.nisargjhaveri.netspeed;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Locale;

public final class NetSpeedIndicatorService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "indicator_channel";

    private Paint mIconSpeedPaint, mIconUnitPaint;
    private Bitmap mIconBitmap;
    private Canvas mIconCanvas;

    private RemoteViews mNotificationContentView;

    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder;

    private KeyguardManager mKeyguardManager;

    private long mLastRxBytes = 0;
    private long mLastTxBytes = 0;
    private long mLastTime = 0;

    private HumanSpeed mTotalHumanSpeed;
    private HumanSpeed mDownHumanSpeed;
    private HumanSpeed mUpHumanSpeed;

    private boolean mIsSpeedUnitBits = false;

    private boolean mNotificationCreated = false;

    private int mNotificationPriority;
    private boolean mNotificationOnLockScreen;

    final private Handler mHandler = new Handler();

    private final Runnable mHandlerRunnable = new Runnable() {
        @Override
        public void run() {
            updateNotification();
            mHandler.postDelayed(this, 1000);
        }
    };

    private final BroadcastReceiver mScreenBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                pauseNotifying();
                if (!mNotificationOnLockScreen) {
                    hideNotification();
                }
                updateNotification();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (mNotificationOnLockScreen || !mKeyguardManager.isKeyguardLocked()) {
                    showNotification();
                    restartNotifying();
                }
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                showNotification();
                restartNotifying();
            }
        }
    };

    private class HumanSpeed {
        String speedValue;
        String speedUnit;

        HumanSpeed() {
            setSpeed(0);
        }

        HumanSpeed setSpeed(long speed) {
            if (mIsSpeedUnitBits) {
                speed *= 8;
            }

            if (speed < 1000000) {
                this.speedUnit = getString(mIsSpeedUnitBits ? R.string.kbps : R.string.kBps);
                this.speedValue = String.valueOf(speed / 1000);
            } else if (speed >= 1000000) {
                this.speedUnit = getString(mIsSpeedUnitBits ? R.string.Mbps : R.string.MBps);

                if (speed < 10000000) {
                    this.speedValue = String.format(Locale.ENGLISH, "%.1f", speed / 1000000.0);
                } else if (speed < 100000000) {
                    this.speedValue = String.valueOf(speed / 1000000);
                } else {
                    this.speedValue = getString(R.string.plus99);
                }
            } else {
                this.speedValue = getString(R.string.dash);
                this.speedUnit = getString(R.string.dash);
            }

            return this;
        }

        HumanSpeed calcSpeed(long bytesUsed, long timeTaken) {
            long speed = 0;
            if (timeTaken > 0) {
                speed = bytesUsed * 1000 / timeTaken;
            }

            return setSpeed(speed);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        pauseNotifying();
        unregisterReceiver(mScreenBroadcastReceiver);

        stopForeground(true);
    }

    public void onCreate() {
        super.onCreate();

        mLastRxBytes = TrafficStats.getTotalRxBytes();
        mLastTxBytes = TrafficStats.getTotalTxBytes();
        mLastTime = System.currentTimeMillis();

        mTotalHumanSpeed = new HumanSpeed();
        mDownHumanSpeed = new HumanSpeed();
        mUpHumanSpeed = new HumanSpeed();

        setupIndicatorIconGenerator();
        setupNotifications();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleConfigChange(intent.getExtras());

        if (!mNotificationCreated) {
            startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
            mNotificationCreated = true;
        }

        restartNotifying();

        return START_REDELIVER_INTENT;
    }

    private void pauseNotifying() {
        mHandler.removeCallbacks(mHandlerRunnable);
    }

    private void restartNotifying() {
        mHandler.removeCallbacks(mHandlerRunnable);
        mHandler.post(mHandlerRunnable);
    }

    private void hideNotification() {
        mNotificationBuilder.setPriority(Notification.PRIORITY_MIN);
    }

    private void showNotification() {
        mNotificationBuilder.setPriority(mNotificationPriority);
    }

    private void updateNotification() {
        long currentRxBytes = TrafficStats.getTotalRxBytes();
        long currentTxBytes = TrafficStats.getTotalTxBytes();
        long usedRxBytes = currentRxBytes - mLastRxBytes;
        long usedTxBytes = currentTxBytes - mLastTxBytes;
        long currentTime = System.currentTimeMillis();
        long usedTime = currentTime - mLastTime;

        mTotalHumanSpeed.calcSpeed(usedRxBytes + usedTxBytes, usedTime);
        mDownHumanSpeed.calcSpeed(usedRxBytes, usedTime);
        mUpHumanSpeed.calcSpeed(usedTxBytes, usedTime);

        mNotificationBuilder.setSmallIcon(
                getIndicatorIcon(mTotalHumanSpeed)
        );

        mNotificationContentView.setTextViewText(
                R.id.notificationText,
                String.format(
                        Locale.ENGLISH,"Down: %s %s     Up: %s %s",
                        mDownHumanSpeed.speedValue, mDownHumanSpeed.speedUnit,
                        mUpHumanSpeed.speedValue, mUpHumanSpeed.speedUnit
                )
        );

        mNotificationManager
                .notify(NOTIFICATION_ID, mNotificationBuilder.build());

        mLastRxBytes = currentRxBytes;
        mLastTxBytes = currentTxBytes;
        mLastTime = currentTime;
    }

    private void handleConfigChange(Bundle extras) {
        // Show/Hide settings button
        if (extras.getBoolean(Settings.KEY_SHOW_SETTINGS_BUTTON, false)) {
            mNotificationContentView.setViewVisibility(R.id.notificationSettings, View.VISIBLE);
        } else {
            mNotificationContentView.setViewVisibility(R.id.notificationSettings, View.GONE);
        }

        // Notification priority
        switch (extras.getString(Settings.KEY_NOTIFICATION_PRIORITY, "max")) {
            case "low":
                mNotificationPriority = Notification.PRIORITY_LOW;
                break;
            case "default":
                mNotificationPriority = Notification.PRIORITY_DEFAULT;
                break;
            case "high":
                mNotificationPriority = Notification.PRIORITY_HIGH;
                break;
            case "max":
                mNotificationPriority = Notification.PRIORITY_MAX;
                break;
        }
        mNotificationBuilder.setPriority(mNotificationPriority);

        // Show/Hide on lock screen
        IntentFilter screenBroadcastIntentFilter = new IntentFilter();
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        mNotificationOnLockScreen = extras.getBoolean(Settings.KEY_NOTIFICATION_ON_LOCK_SCREEN, false);

        if (mNotificationOnLockScreen) {
            mNotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        } else {
            mNotificationBuilder.setVisibility(Notification.VISIBILITY_SECRET);

            screenBroadcastIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
            screenBroadcastIntentFilter.setPriority(999);
        }

        if (mNotificationCreated) {
            unregisterReceiver(mScreenBroadcastReceiver);
        }
        registerReceiver(mScreenBroadcastReceiver, screenBroadcastIntentFilter);

        // Speed unit, bps or Bps
        mIsSpeedUnitBits = extras.getString(Settings.KEY_INDICATOR_SPEED_UNIT, "Bps").equals("bps");
    }

    private void setupNotifications() {
        mNotificationContentView = new RemoteViews(getPackageName(), R.layout.indicator_notification_view);
        mNotificationContentView.setImageViewBitmap(R.id.notificationIcon, mIconBitmap);

        PendingIntent openSettingsIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        mNotificationContentView.setOnClickPendingIntent(R.id.notificationSettings, openSettingsIntent);

        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            mNotificationManager.createNotificationChannel(notificationChannel);

            mNotificationBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            mNotificationBuilder = new Notification.Builder(this);
        }

        mNotificationBuilder
                .setSmallIcon(getIndicatorIcon(mTotalHumanSpeed))
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setContent(mNotificationContentView)
                .setOngoing(true)
                .setLocalOnly(true);

        mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
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

    private Icon getIndicatorIcon(HumanSpeed speed) {
        mIconCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mIconCanvas.drawText(speed.speedValue, 48, 52, mIconSpeedPaint);
        mIconCanvas.drawText(speed.speedUnit, 48, 95, mIconUnitPaint);

        return Icon.createWithBitmap(mIconBitmap);
    }
}