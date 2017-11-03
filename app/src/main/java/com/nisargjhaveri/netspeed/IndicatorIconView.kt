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

    public fun setSpeed(speed: Long) {
        findViewById<TextView>(R.id.speed_value).setText((speed / 1000).toString())
    }

}
