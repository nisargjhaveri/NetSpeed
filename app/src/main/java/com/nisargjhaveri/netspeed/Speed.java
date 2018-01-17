package com.nisargjhaveri.netspeed;

import android.content.Context;
import android.os.Bundle;

import java.util.Locale;

final class Speed {
    private static final String KEY_TOTAL_SPEED = "totalSpeed";
    private static final String KEY_DOWN_SPEED = "downSpeed";
    private static final String KEY_UP_SPEED = "upSpeed";
    private static final String KEY_IS_SPEED_UNIT_BITS = "isSpeedUnitBits";

    private long mTotalSpeed = 0;
    private long mDownSpeed = 0;
    private long mUpSpeed = 0;

    HumanSpeed total = new HumanSpeed();
    HumanSpeed down = new HumanSpeed();
    HumanSpeed up = new HumanSpeed();

    private boolean mIsSpeedUnitBits = false;

    private Context mContext;

    Speed(Context context) {
        mContext = context;

        updateHumanSpeeds();
    }

    Speed(Context context, Bundle speedBundle) {
        mContext = context;

        mTotalSpeed = speedBundle.getLong(KEY_TOTAL_SPEED);
        mDownSpeed = speedBundle.getLong(KEY_DOWN_SPEED);
        mUpSpeed = speedBundle.getLong(KEY_UP_SPEED);
        mIsSpeedUnitBits = speedBundle.getBoolean(KEY_IS_SPEED_UNIT_BITS);

        updateHumanSpeeds();
    }

    private void updateHumanSpeeds() {
        total.setSpeed(mTotalSpeed);
        down.setSpeed(mDownSpeed);
        up.setSpeed(mUpSpeed);
    }

    void calcSpeed(long timeTaken, long downBytes, long upBytes) {
        long totalSpeed = 0;
        long downSpeed = 0;
        long upSpeed = 0;

        long totalBytes = downBytes + upBytes;

        if (timeTaken > 0) {
            totalSpeed = totalBytes * 1000 / timeTaken;
            downSpeed = downBytes * 1000 / timeTaken;
            upSpeed = upBytes * 1000 / timeTaken;
        }

        mTotalSpeed = totalSpeed;
        mDownSpeed = downSpeed;
        mUpSpeed = upSpeed;

        updateHumanSpeeds();
    }

    void setIsSpeedUnitBits(boolean isSpeedUnitBits) {
        mIsSpeedUnitBits = isSpeedUnitBits;
    }

    Bundle getBundle() {
        Bundle speedBundle = new Bundle();
        speedBundle.putLong(KEY_TOTAL_SPEED, mTotalSpeed);
        speedBundle.putLong(KEY_DOWN_SPEED, mDownSpeed);
        speedBundle.putLong(KEY_UP_SPEED, mUpSpeed);
        speedBundle.putBoolean(KEY_IS_SPEED_UNIT_BITS, mIsSpeedUnitBits);

        return speedBundle;
    }

    class HumanSpeed {
        String speedValue;
        String speedUnit;

        private void setSpeed(long speed) {
            if (mContext == null) return;

            if (mIsSpeedUnitBits) {
                speed *= 8;
            }

            if (speed < 1000000) {
                this.speedUnit = mContext.getString(mIsSpeedUnitBits ? R.string.kbps : R.string.kBps);
                this.speedValue = String.valueOf(speed / 1000);
            } else if (speed >= 1000000) {
                this.speedUnit = mContext.getString(mIsSpeedUnitBits ? R.string.Mbps : R.string.MBps);

                if (speed < 10000000) {
                    this.speedValue = String.format(Locale.ENGLISH, "%.1f", speed / 1000000.0);
                } else if (speed < 100000000) {
                    this.speedValue = String.valueOf(speed / 1000000);
                } else {
                    this.speedValue = mContext.getString(R.string.plus99);
                }
            } else {
                this.speedValue = mContext.getString(R.string.dash);
                this.speedUnit = mContext.getString(R.string.dash);
            }
        }
    }
}