package com.survice.electrofix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PayingAnyoneActivity extends AppCompatActivity {


    private EditText editTextAmount;
    private ImageButton btnClear;
    private TextView autoAmountBox, textBankName, textPersonName, textUpiId;
    private Button upiPaymentButton, btnCancel;

    private String upiId, personName, bankName;
    private static final int UPI_PAYMENT_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paying_anyone);

        bankName = getIntent().getStringExtra("bank_name");
        personName = getIntent().getStringExtra("person_name");
        upiId = getIntent().getStringExtra("upi_id");

        editTextAmount = findViewById(R.id.editTextAmount);
        btnClear = findViewById(R.id.btnClear);
        textPersonName = findViewById(R.id.textPersonName);
        textUpiId = findViewById(R.id.textUpiId);
        upiPaymentButton = findViewById(R.id.upiPaymentButton);
        btnCancel = findViewById(R.id.btnCancel);

        textPersonName.setText(personName != null ? personName : "Name");
        textUpiId.setText(upiId != null ? upiId : "UPI ID");
        if (textBankName != null && bankName != null) textBankName.setText(bankName);

        btnClear.setOnClickListener(v -> editTextAmount.setText(""));

        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String amount = s.toString();
                autoAmountBox.setText("Pay ₹" + (amount.isEmpty() ? "0" : amount));
                btnClear.setVisibility(amount.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        btnCancel.setOnClickListener(v -> finish());

        upiPaymentButton.setOnClickListener(v -> {
            String amount = editTextAmount.getText().toString().trim();

            if (amount.isEmpty() || amount.equals("0")) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (upiId == null || upiId.isEmpty() || upiId.equalsIgnoreCase("N/A")) {
                Toast.makeText(this, "Invalid UPI ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = new Uri.Builder()
                    .scheme("upi")
                    .authority("pay")
                    .appendQueryParameter("pa", upiId)
                    .appendQueryParameter("pn", personName)
                    .appendQueryParameter("tn", "Payment through Electrofix App")
                    .appendQueryParameter("am", amount)
                    .appendQueryParameter("cu", "INR")
                    .build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);

            Intent chooser = Intent.createChooser(intent, "Pay with");
            if (chooser.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooser, UPI_PAYMENT_REQUEST_CODE);
            } else {
                Toast.makeText(this, "No UPI app found. Please install one to continue.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            String response = data != null ? data.getStringExtra("response") : null;

            if (response != null) {
                String status = getStatusFromResponse(response);
                switch (status.toLowerCase()) {
                    case "success":
                        Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show();
                        break;
                    case "failure":
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(this, "Payment Cancelled or Unknown", Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                Toast.makeText(this, "Payment Cancelled or Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getStatusFromResponse(String response) {
        for (String part : response.split("&")) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2 && keyValue[0].equalsIgnoreCase("Status")) {
                return keyValue[1];
            }
        }
        return "UNKNOWN";
    }
}