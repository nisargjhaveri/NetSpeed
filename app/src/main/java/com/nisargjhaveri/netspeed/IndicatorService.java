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

    private IndicatorNotification mIndicatorNotification = new IndicatorNotification();

    private boolean mNotificationCreated = false;

    private int mNotificationPriority;
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

            mIndicatorNotification.updateNotification(mTotalHumanSpeed, mDownHumanSpeed, mUpHumanSpeed);

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
                    mIndicatorNotification.hideNotification();
                }
                mIndicatorNotification.updateNotification(mTotalHumanSpeed, mDownHumanSpeed, mUpHumanSpeed);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (mNotificationOnLockScreen || !mKeyguardManager.isKeyguardLocked()) {
                    mIndicatorNotification.showNotification();
                    restartNotifying();
                }
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                mIndicatorNotification.showNotification();
                restartNotifying();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        pauseNotifying();
        unregisterReceiver(mScreenBroadcastReceiver);

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

        mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);

        mIndicatorNotification.setup(this, mTotalHumanSpeed);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleConfigChange(intent.getExtras());

        createNotification();

        // TODO: don't restart if not needed
        restartNotifying();

        return START_REDELIVER_INTENT;
    }

    private void createNotification() {
        if (!mNotificationCreated) {
            mIndicatorNotification.start(this);
            mNotificationCreated = true;
        }
    }

    private void removeNotification() {
        if (mNotificationCreated) {
            mIndicatorNotification.stop(this);
            mNotificationCreated = false;
        }
    }

    private void pauseNotifying() {
        mHandler.removeCallbacks(mHandlerRunnable);
    }

    private void restartNotifying() {
        mHandler.removeCallbacks(mHandlerRunnable);
        mHandler.post(mHandlerRunnable);
    }

    private void handleConfigChange(Bundle config) {
        // Show/Hide on lock screen
        IntentFilter screenBroadcastIntentFilter = new IntentFilter();
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenBroadcastIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        mNotificationOnLockScreen = config.getBoolean(Settings.KEY_NOTIFICATION_ON_LOCK_SCREEN, false);

        if (!mNotificationOnLockScreen) {
            screenBroadcastIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
            screenBroadcastIntentFilter.setPriority(999);
        }

        if (mNotificationCreated) {
            unregisterReceiver(mScreenBroadcastReceiver);
        }
        registerReceiver(mScreenBroadcastReceiver, screenBroadcastIntentFilter);

        // Speed unit, bps or Bps
        boolean isSpeedUnitBits = config.getString(Settings.KEY_INDICATOR_SPEED_UNIT, "Bps").equals("bps");
        mTotalHumanSpeed.setIsSpeedUnitBits(isSpeedUnitBits);
        mDownHumanSpeed.setIsSpeedUnitBits(isSpeedUnitBits);
        mUpHumanSpeed.setIsSpeedUnitBits(isSpeedUnitBits);

        mIndicatorNotification.handleConfigChange(config);
    }
}