package com.survice.electrofix;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;

public class OffersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        // 🔹 Back Button Functionality
        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 🔹 বর্তমান অ্যাক্টিভিটি বন্ধ করবে এবং আগের স্ক্রিনে ফিরে যাবে
            }
        });
    }
}