package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
// No need for LinearLayout, AnimationDrawable, Drawable imports if the problematic code is removed
// import android.graphics.drawable.AnimationDrawable;
// import android.graphics.drawable.Drawable;
// import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class TrackingActivity extends AppCompatActivity {

    private Button btnStartTracking;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // REMOVE THE FOLLOWING BLOCK OF CODE FROM HERE!
        // LinearLayout startJourneyLayout = findViewById(R.id.start_journey_layout);
        // if (startJourneyLayout != null) {
        //     Drawable background = startJourneyLayout.getBackground();
        //     if (background instanceof AnimationDrawable) {
        //         AnimationDrawable animationDrawable = (AnimationDrawable) background;
        //         animationDrawable.start();
        //     }
        // }
        // END OF BLOCK TO BE REMOVED

        btnStartTracking = findViewById(R.id.btnStartTracking);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnStartTracking.setOnClickListener(v -> {
            Intent intent = new Intent(TrackingActivity.this, TrackCustomerActivity.class);
            intent.putExtra("customerLat", 22.5726); // test value, replace with actual
            intent.putExtra("customerLon", 88.3639); // test value, replace with actual
            intent.putExtra("repairerId", "REPAIRER_USER_ID"); // replace with actual ID
            startActivity(intent);
        });
    }
}