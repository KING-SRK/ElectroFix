package com;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.survice.electrofix.R;

public class paymentBillHistactivity extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_billhist);

        // Back Button functionality
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton backButton = findViewById(R.id.btnBackOption);
        backButton.setOnClickListener(v -> finish());

        // Optional: Set header text dynamically
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText("Payment Options");  // You can set it here if needed
    }
}