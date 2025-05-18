package com.survice.electrofix;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class PaymentOptionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_options);

        // Back Button functionality
        ImageButton backButton = findViewById(R.id.btnBackOption);
        backButton.setOnClickListener(v -> finish());

        // Optional: Set header text dynamically
        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText("Payment Options");  // You can set it here if needed
    }
}