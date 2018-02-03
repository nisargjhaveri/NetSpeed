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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.nisargjhaveri.netspeed.settings.Settings;

import java.util.ArrayList;

public final class IndicatorService extends Service {
    private KeyguardManager mKeyguardManager;

    private long mLastRxBytes = 0;
    private long mLastTxBytes = 0;
    private long mLastTime = 0;

    private Speed mSpeed;

    ArrayList<Messenger> mClients = new ArrayList<>();

    private IndicatorNotification mIndicatorNotification;

    private boolean mNotificationCreated = false;

    private boolean mNotificationOnLockScreen;

    private boolean mIsUpdating = false;
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

            mSpeed.calcSpeed(usedTime, usedRxBytes, usedTxBytes);

            if (mIndicatorNotification != null) {
                mIndicatorNotification.updateNotification(mSpeed);
            }

            for (int i = mClients.size() - 1; i >= 0; --i) {
                notifyClient(mClients.get(i));
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
                mIndicatorNotification.updateNotification(mSpeed);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (mNotificationOnLockScreen
                        || (mKeyguardManager != null && !mKeyguardManager.isKeyguardLocked())) {
                    mIndicatorNotification.showNotification();
                    mIndicatorNotification.updateNotification(mSpeed);
                    ensureUpdating();
                }
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                mIndicatorNotification.showNotification();
                mIndicatorNotification.updateNotification(mSpeed);
                ensureUpdating();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                boolean handled = true;
                switch (msg.what) {
                    case IndicatorServiceConnector.MSG_REGISTER_CLIENT:
                        mClients.add(msg.replyTo);
                        notifyClient(msg.replyTo);
                        ensureUpdating();
                        break;
                    case IndicatorServiceConnector.MSG_UNREGISTER_CLIENT:
                        mClients.remove(msg.replyTo);
                        break;
                    default:
                        handled = false;
                }
                return !handled;
            }
        })).getBinder();
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

        mSpeed = new Speed(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleConfigChange(intent.getExtras());

        ensureUpdating();

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
        mIndicatorNotification.updateNotification(mSpeed);
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
        mIsUpdating = false;
    }

    private void ensureUpdating() {
        if (!mIsUpdating) {
            mHandler.post(mHandlerRunnable);
            mIsUpdating = true;
        }
    }

    private void notifyClient(Messenger client) {
        try {
            client.send(
                    Message.obtain(null,
                            IndicatorServiceConnector.MSG_UPDATE_SPEED,
                            mSpeed.getBundle())
            );
        } catch (RemoteException e) {
            // The client is dead. Remove it from the list.
            mClients.remove(client);
        }
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
        mSpeed.setIsSpeedUnitBits(isSpeedUnitBits);
    }
}