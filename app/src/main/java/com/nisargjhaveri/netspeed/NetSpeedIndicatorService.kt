package com.nisargjhaveri.netspeed

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import android.net.TrafficStats
import android.view.View

class NetSpeedIndicatorService : Service() {
    private val NOTIFICATION_ID = 1;

    private val mIconView by lazy {
        IndicatorIconView(this)
    }
    private val mIconBitmap by lazy {
        Bitmap.createBitmap(
                mIconView.measuredWidth,
                mIconView.measuredHeight,
                Bitmap.Config.ALPHA_8
        )
    }
    private val mIconCanvas: Canvas by lazy {
        Canvas(mIconBitmap)
    }

    val mNotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    val mNotificationBuilder by lazy {
        Notification.Builder(this)
                .setSmallIcon(getIndicatorIcon(0))
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setLocalOnly(true)
    }

    private var mLastUsage: Long = 0;
    private var mLastTime: Long = 0;

    private val mThread by lazy {
        object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(1000)

                        val currentUsage = (TrafficStats::getTotalRxBytes)() + (TrafficStats::getTotalTxBytes)()
                        val currentTime = System.currentTimeMillis()

                        updateNotification((currentUsage  - mLastUsage) * 1000 / (currentTime - mLastTime))

                        mLastUsage = currentUsage
                        mLastTime = currentTime
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
    }

    override fun onBind(p0: Intent?) = null

    override fun onDestroy() {
        super.onDestroy()

        mThread.interrupt()
        mThread.join()

        stopForeground(true)
    }

    override fun onCreate() {
        super.onCreate()

        setupIndicatorIconGenerator()

        mLastUsage = (TrafficStats::getTotalRxBytes)() +(TrafficStats::getTotalTxBytes)()
        mLastTime = System.currentTimeMillis()

        mThread.start()
        showNotification()
    }

    private fun showNotification() {
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build())
    }

    private fun updateNotification(speed: Long) {
        mNotificationBuilder
                .setSmallIcon(getIndicatorIcon(speed))
                .setPriority(Notification.PRIORITY_MAX)
        mNotificationManager .notify(NOTIFICATION_ID, mNotificationBuilder.build())
    }

    private fun setupIndicatorIconGenerator() {
        //Cause the view to re-layout
        mIconView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        mIconView.layout(0, 0, mIconView.measuredWidth, mIconView.measuredHeight)
    }

    private fun getIndicatorIcon(speed: Long): Icon {
        println(speed)

        mIconView.setSpeed(speed)

        mIconCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        mIconView.draw(mIconCanvas)

        return Icon.createWithBitmap(mIconBitmap)
    }
}
