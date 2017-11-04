package com.nisargjhaveri.netspeed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public final class IndicatorIconView extends RelativeLayout {
    private TextView mSpeedValue = null;
    private TextView mSpeedUnit = null;

    public IndicatorIconView(Context context) {
        super(context);
        init(null, 0);
    }

    public IndicatorIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public IndicatorIconView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        RelativeLayout.inflate(this.getContext(), R.layout.indicator_icon_layout, this);
        mSpeedValue = this.findViewById(R.id.speed_value);
        mSpeedUnit = this.findViewById(R.id.speed_unit);

        mSpeedValue.getPaint().setAntiAlias(true);
        mSpeedUnit.getPaint().setAntiAlias(true);
    }

    public final void setSpeed(long speed) {
        String speedValue;
        String speedUnit;

        Context context = this.getContext();

        if (speed < 1000000) {
            speedUnit = context.getString(R.string.kbps);
            speedValue = String.valueOf(speed / 1000);
        } else if (speed >= 1000000) {
            speedUnit = context.getString(R.string.mbps);

            if (speed < 10000000) {
                speedValue = String.format("%.1f", speed / 1000000.0);
            } else if (speed < 100000000) {
                speedValue = String.valueOf(speed / 1000000);
            } else {
                speedValue = context.getString(R.string.plus99);
            }
        } else {
            speedValue = context.getString(R.string.dash);
            speedUnit = context.getString(R.string.dash);
        }

        mSpeedValue.setText(speedValue);
        mSpeedUnit.setText(speedUnit);
    }
}