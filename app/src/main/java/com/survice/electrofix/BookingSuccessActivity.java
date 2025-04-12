package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;

public class BookingSuccessActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_success);

        MaterialButton btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            Intent intent = new Intent(BookingSuccessActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}