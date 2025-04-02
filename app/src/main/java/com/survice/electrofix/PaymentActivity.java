package com.survice.electrofix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Button upiPaymentButton = findViewById(R.id.upiPaymentButton);
        upiPaymentButton.setOnClickListener(v -> startUPIPayment());
    }

    private void startUPIPayment() {
        Uri uri = Uri.parse("upi://pay?pa=your_upi_id@upi&pn=ElectroFix&am=100&cu=INR&tn=Service Payment");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}