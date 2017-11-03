package com.nisargjhaveri.netspeed

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView


class IndicatorIconView : RelativeLayout {

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
        inflate(context, R.layout.indicator_icon_layout, this)
    }

    fun setSpeed(speed: Long) {
        val speedValue: String
        val speedUnit: String

        when {
//            speed < 1000 -> {
//                speedValue = speed.toString()
//                speedUnit = "B/s"
//            }
            speed < 1000000 -> {
                speedValue = (speed / 1000).toString()
                speedUnit = "KB/s"
            }
            speed < 10000000 -> {
                speedValue = (speed / 1000000.0).toString()
                speedUnit = "MB/s"
            }
            speed < 1000000000 -> {
                speedValue = (speed / 1000000).toString()
                speedUnit = "MB/s"
            }
            else -> {
                speedValue = "-"
                speedUnit = "-"
            }
        }

        findViewById<TextView>(R.id.speed_value).setText(speedValue)
        findViewById<TextView>(R.id.speed_unit).setText(speedUnit)
    }

}
