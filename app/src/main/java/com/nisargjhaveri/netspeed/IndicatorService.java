package com.nisargjhaveri.netspeed;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.nisargjhaveri.netspeed.settings.Settings;

public final class IndicatorService extends Service {
    private KeyguardManager mKeyguardManager;

    private long mLastRxBytes = 0;
    private long mLastTxBytes = 0;
    private long mLastTime = 0;

    private HumanSpeed mTotalHumanSpeed;
    private HumanSpeed mDownHumanSpeed;
    private HumanSpeed mUpHumanSpeed;

    private IndicatorNotification mIndicatorNotification;

    private boolean mNotificationCreated = false;

    private boolean mNotificationOnLockScreen;

    final private Handler mHandler = new Handler();

    private final Runnable mHandlerRunnable = new Runnable() {
        @Override
        public void run() {
            long currentRxBytes = TrafficStats.getTotalRxBytes();
            long currentTxBytes = TrafficStats.getTotalTxBytes();
            long usedRxBytes = currentRxBytes - mLastRxBytes;
            long usedTxBytes = currentTxBytes - mLastTxBytes;
            long currentTime = System.currentTimeMillis();
            long usedTime = currentTime - mLastTime;

            mLastRxBytes = currentRxBytes;
            mLastTxBytes = currentTxBytes;
            mLastTime = currentTime;

            mTotalHumanSpeed.calcSpeed(usedRxBytes + usedTxBytes, usedTime);
            mDownHumanSpeed.calcSpeed(usedRxBytes, usedTime);
            mUpHumanSpeed.calcSpeed(usedTxBytes, usedTime);

            if (mIndicatorNotification != null) {
                mIndicatorNotification.updateNotification(mTotalHumanSpeed, mDownHumanSpeed, mUpHumanSpeed);
            }

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
                pauseUpdating();
                if (!mNotificationOnLockScreen) {
                    mIndicatorNotification.hideNotification();
                }
                mIndicatorNotification.updateNotification(mTotalHumanSpeed, mDownHumanSpeed, mUpHumanSpeed);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (mNotificationOnLockScreen
                        || (mKeyguardManager != null && !mKeyguardManager.isKeyguardLocked())) {
                    mIndicatorNotification.showNotification();
                    restartUpdating();
                }
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                mIndicatorNotification.showNotification();
                restartUpdating();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        pauseUpdating();

        removeNotification();
    }

    public void onCreate() {
        super.onCreate();

        mLastRxBytes = TrafficStats.getTotalRxBytes();
        mLastTxBytes = TrafficStats.getTotalTxBytes();
        mLastTime = System.currentTimeMillis();

        mTotalHumanSpeed = new HumanSpeed(this);
        mDownHumanSpeed = new HumanSpeed(this);
        mUpHumanSpeed = new HumanSpeed(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleConfigChange(intent.getExtras());

        // TODO: don't restart if not needed
        restartUpdating();

        return START_REDELIVER_INTENT;
    }

    private void createNotification(Bundle config) {
        if (mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        }
        if (mIndicatorNotification == null) {
            mIndicatorNotification = new IndicatorNotification(this);
        }
        mIndicatorNotification.handleConfigChange(config);
        if (!mNotificationCreated) {
            mIndicatorNotification.start(this);
            mNotificationCreated = true;
        }
    }

    private void removeNotification() {
        if (mNotificationCreated) {
            unregisterReceiver(mScreenBroadcastReceiver);
            mIndicatorNotification.stop(this);
            mIndicatorNotification = null;
            mNotificationCreated = false;
        }
    }

    private void pauseUpdating() {
        mHandler.removeCallbacks(mHandlerRunnable);
    }

    private void restartUpdating() {
        mHandler.removeCallbacks(mHandlerRunnable);
        mHandler.post(mHandlerRunnable);
    }

    private void handleConfigChange(Bundle config) {
        boolean isNotificationEnabled = config.getBoolean(Settings.KEY_INDICATOR_ENABLED, true);
        if (!isNotificationEnabled) {
            removeNotification();
            return;
        }

        // Pass it to notification
        createNotification(config);

        // Show/Hide on lock screen
        IntentFilter screenBroadcastIntentFilter = new IntentFilter();
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        mNotificationOnLockScreen = config.getBoolean(Settings.KEY_NOTIFICATION_ON_LOCK_SCREEN, false);

        if (!mNotificationOnLockScreen) {
            screenBroadcastIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
            screenBroadcastIntentFilter.setPriority(999);
        }

        try {
            unregisterReceiver(mScreenBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            // Not registered. Do nothing.
        }
        registerReceiver(mScreenBroadcastReceiver, screenBroadcastIntentFilter);

        // Speed unit, bps or Bps
        boolean isSpeedUnitBits = config.getString(Settings.KEY_INDICATOR_SPEED_UNIT, "Bps").equals("bps");
        mTotalHumanSpeed.setIsSpeedUnitBits(isSpeedUnitBits);
        mDownHumanSpeed.setIsSpeedUnitBits(isSpeedUnitBits);
        mUpHumanSpeed.setIsSpeedUnitBits(isSpeedUnitBits);
    }
}