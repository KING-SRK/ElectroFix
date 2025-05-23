package com.survice.electrofix;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class NotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

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