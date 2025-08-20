package com.survice.electrofix;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentConformationActivity extends Activity {

    private TextView autoAmountBox;
    private Button upiPaymentButton, btnCancel;
    private ImageButton btnBack;

    private String amountToPay = "0";  // default ₹0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_conformation);  // Ensure the layout name matches

        // View bindings
        autoAmountBox = findViewById(R.id.autoAmountBox);
        upiPaymentButton = findViewById(R.id.upiPaymentButton);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);  // ✅ Correct type and name

        // Get the amount passed from previous activity
        if (getIntent() != null && getIntent().hasExtra("amount")) {
            amountToPay = getIntent().getStringExtra("amount");
        }

        // ✅ Back Button Logic
        btnBack.setOnClickListener(v -> finish());  // Simply closes the current activity

        // Display amount dynamically
        autoAmountBox.setText("         Pay ₹" + amountToPay);

        // Cancel Button Logic
        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
            finish();  // Close this activity
        });

        // Proceed to Pay Button Logic
        upiPaymentButton.setOnClickListener(v -> {
            payUsingUPI(amountToPay, "electrofix@upi", "ElectroFix", "Payment to ElectroFix");
        });
    }

    private void payUsingUPI(String amount, String upiId, String name, String note) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)   // UPI ID
                .appendQueryParameter("pn", name)    // Payee Name
                .appendQueryParameter("tn", note)    // Transaction Note
                .appendQueryParameter("am", amount)  // Amount
                .appendQueryParameter("cu", "INR")   // Currency
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // Show only UPI apps that can handle this intent
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with UPI");

        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, 123);  // requestCode = 123
        } else {
            Toast.makeText(this, "No UPI app found! Please install one to continue.", Toast.LENGTH_SHORT).show();
        }
    }

    // Optional: Handle UPI result if needed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (data != null) {
                String response = data.getStringExtra("response");
                Toast.makeText(this, "UPI Response: " + response, Toast.LENGTH_LONG);
            }
        }
    }
}
