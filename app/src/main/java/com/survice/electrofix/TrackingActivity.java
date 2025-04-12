package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TrackingActivity extends AppCompatActivity {

    private Button btnStartTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        btnStartTracking = findViewById(R.id.btnStartTracking);

        btnStartTracking.setOnClickListener(v -> {
            Intent intent = new Intent(TrackingActivity.this, TrackCustomerActivity.class);
            intent.putExtra("customerLat", 22.5726); // test value, replace with actual
            intent.putExtra("customerLon", 88.3639); // test value, replace with actual
            intent.putExtra("repairerId", "REPAIRER_USER_ID"); // replace with actual ID
            startActivity(intent);

        });
    }
}