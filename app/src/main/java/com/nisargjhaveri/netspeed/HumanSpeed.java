package com.nisargjhaveri.netspeed;

import android.content.Context;

import java.util.Locale;

final class HumanSpeed {
    String speedValue;
    String speedUnit;

    private boolean mIsSpeedUnitBits = false;

    private Context mContext;

    HumanSpeed(Context context) {
        mContext = context;

        setSpeed(0);
    }

    private void setSpeed(long speed) {
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

    void calcSpeed(long bytesUsed, long timeTaken) {
        long speed = 0;
        if (timeTaken > 0) {
            speed = bytesUsed * 1000 / timeTaken;
        }

        setSpeed(speed);
    }

    void setIsSpeedUnitBits(boolean isSpeedUnitBits) {
        mIsSpeedUnitBits = isSpeedUnitBits;
    }
}