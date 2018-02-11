package com.nisargjhaveri.netspeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public final class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView rateAppTextView = findViewById(R.id.rate_app);
        TextView sendFeedbackTextView = findViewById(R.id.send_feedback);

        rateAppTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutAppHelper.rateApp(AboutActivity.this);
            }
        });

        sendFeedbackTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutAppHelper.sendFeedback(AboutActivity.this);
            }
        });
    }

}
