package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TrackingActivity extends BaseActivity {

    private TextView customerLocation, repairerLocation;
    private Button btnStartTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // UI Elements Initialization
        customerLocation = findViewById(R.id.customer_location);
        repairerLocation = findViewById(R.id.repairer_location);
        btnStartTracking = findViewById(R.id.btnStartTracking);

        // Start Tracking Button Click Listener
        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Tracking Map Page
                Intent intent = new Intent(TrackingActivity.this, TrackingMapActivity.class);
                startActivity(intent);
            }
        });

        // TODO: এখানে Firestore থেকে Customer & Repairer-এর লোকেশন ডেটা লোড করা হবে
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, RepairerLocationService.class);
        startService(serviceIntent); // 🔥 Repairer-এর লাইভ লোকেশন Firebase-এ আপলোড করা শুরু করবে
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent serviceIntent = new Intent(this, RepairerLocationService.class);
        stopService(serviceIntent); // 🚀 Repairer-এর লোকেশন আপলোড বন্ধ হবে
    }
}