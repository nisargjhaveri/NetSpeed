package com.nisargjhaveri.netspeed

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView


class IndicatorIconView : RelativeLayout {
    private val mSpeedValue by lazy {
        findViewById<TextView>(R.id.speed_value)
    }

    private val mSpeedUnit by lazy {
        findViewById<TextView>(R.id.speed_unit)
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val inflate = inflate(context, R.layout.indicator_icon_layout, this)

        mSpeedValue.paint.isAntiAlias = true
        mSpeedUnit.paint.isAntiAlias = true
    }

    fun setSpeed(speed: Long) {
        val speedValue: String
        val speedUnit: String

        when {
            speed < 1000000 -> {
                speedValue = (speed / 1000).toString()
                speedUnit = context.getString(R.string.kbps)
            }
            speed >= 1000000 -> {
                speedUnit = context.getString(R.string.mbps)

                when {
                    speed < 10000000 -> {
                        speedValue = "%.1f".format(speed / 1000000.0)
                    }
                    speed < 100000000 -> {
                        speedValue = (speed / 1000000).toString()
                    }
                    else -> {
                        speedValue = context.getString(R.string.plus99)
                    }
                }
            }
            else -> {
                speedValue = context.getString(R.string.dash)
                speedUnit = context.getString(R.string.dash)
            }
        }

        mSpeedValue.setText(speedValue)
        mSpeedUnit.setText(speedUnit)
    }

}
