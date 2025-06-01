package com.survice.electrofix;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private EditText editTextAmount;
    private TextView autoAmountBox;
    private ImageButton btnClear;

    private static final String UPI_ID = "yourupiid@bank";     // Replace with your actual UPI ID
    private static final String PAYEE_NAME = "ElectroFix";     // Display name
    private static final int UPI_PAYMENT_REQUEST = 123;
    private boolean isUpdatingText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        editTextAmount = findViewById(R.id.editTextAmount);
        autoAmountBox = findViewById(R.id.autoAmountBox);
        btnClear = findViewById(R.id.btnClear);

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());

        TextView headerTitle = findViewById(R.id.PaymentPage);
        headerTitle.setText("Pay To ElectroFix");

        btnClear.setOnClickListener(v -> {
            editTextAmount.setText("");
            autoAmountBox.setText("Pay ₹0");
        });

        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingText) return;

                isUpdatingText = true;
                String digits = s.toString().replaceAll("[^0-9]", "");

                if (!digits.isEmpty()) {
                    String formatted = "₹" + digits;
                    editTextAmount.setText(formatted);
                    editTextAmount.setSelection(formatted.length());
                    autoAmountBox.setText("Pay ₹" + digits);
                    btnClear.setVisibility(ImageButton.VISIBLE);
                } else {
                    editTextAmount.setText("");
                    autoAmountBox.setText("Pay ₹0");
                    btnClear.setVisibility(ImageButton.GONE);
                }

                isUpdatingText = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        setupAmountButton(R.id.hundredrsBalence, 100);
        setupAmountButton(R.id.twohundred, 200);
        setupAmountButton(R.id.fivehundred, 500);
        setupAmountButton(R.id.oneThousand, 1000);
        setupAmountButton(R.id.twoThousand, 2000);

        ImageButton btnQRScanner = findViewById(R.id.btnQRScanner);
        btnQRScanner.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        Button upiPaymentButton = findViewById(R.id.upiPaymentButton);
        upiPaymentButton.setOnClickListener(v -> {
            String amountText = editTextAmount.getText().toString().replaceAll("[^0-9]", "");
            if (amountText.isEmpty() || amountText.equals("0")) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(PaymentActivity.this, PaymentConformationActivity.class);
                intent.putExtra("amount", amountText);
                startActivity(intent);
            }
        });
    }

    private void setupAmountButton(int buttonId, int amountToAdd) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            String currentText = editTextAmount.getText().toString().replaceAll("[^0-9]", "");
            int currentAmount = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
            int newAmount = currentAmount + amountToAdd;

            isUpdatingText = true;
            editTextAmount.setText("₹" + newAmount);
            editTextAmount.setSelection(("₹" + newAmount).length());
            autoAmountBox.setText("Pay ₹" + newAmount);
            btnClear.setVisibility(ImageButton.VISIBLE);
            isUpdatingText = false;
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT_REQUEST) {
            if (data != null && data.getStringExtra("response") != null) {
                String response = data.getStringExtra("response").toLowerCase();
                if (response.contains("success")) {
                    Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
                } else if (response.contains("failure") || response.contains("failed")) {
                    Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Payment Cancelled by User", Toast.LENGTH_SHORT).show();
            }
        }
    }
}