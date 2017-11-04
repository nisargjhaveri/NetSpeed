package com.nisargjhaveri.netspeed

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, NetSpeedIndicatorService::class.java)
        startService(intent)

        finish()
    }

}
