package com.nisargjhaveri.netspeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public final class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, NetSpeedIndicatorService.class);
        startService(intent);

        finish();
    }

}
