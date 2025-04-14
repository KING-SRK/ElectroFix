package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.survice.electrofix.QRScannerActivity;

public class PaymentActivity extends AppCompatActivity {

    private EditText editTextAmount;
    private TextView autoAmountBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize views
        editTextAmount = findViewById(R.id.editTextAmount);
        autoAmountBox = findViewById(R.id.autoAmountBox);

        // Back Button functionality
        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());

        // Header Title
        TextView headerTitle = findViewById(R.id.PaymentPage);
        headerTitle.setText("Pay To ElectroFix");

        // Clear Button functionality
        ImageButton btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            editTextAmount.setText("");
            autoAmountBox.setText("₹0");
        });

        // Auto update live amount box
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String entered = s.toString();
                autoAmountBox.setText("₹" + (entered.isEmpty() ? "0" : entered));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Quick amount buttons
        setupAmountButton(R.id.hundredrsBalence, 100);
        setupAmountButton(R.id.twohundred, 200);
        setupAmountButton(R.id.fivehundred, 500);
        setupAmountButton(R.id.oneThousand, 1000);
        setupAmountButton(R.id.twoThousand, 2000);

        // QR Scanner Button (independent from Pay Now button)
        ImageButton btnQRScanner = findViewById(R.id.btnQRScanner);
        btnQRScanner.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        // "Pay Now" Button - go to PaymentOptionActivity
        Button upiPaymentButton = findViewById(R.id.upiPaymentButton);
        upiPaymentButton.setOnClickListener(v -> {
            String amount = editTextAmount.getText().toString().trim();
            if (amount.isEmpty() || amount.equals("0")) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(PaymentActivity.this, PaymentOptionActivity.class);
                intent.putExtra("amount", amount);
                startActivity(intent);
            }
        });
    }

    // Adds value to current amount
    private void setupAmountButton(int buttonId, int amountToAdd) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            String currentText = editTextAmount.getText().toString().trim();
            int currentAmount = 0;

            try {
                currentAmount = Integer.parseInt(currentText);
            } catch (NumberFormatException ignored) {}

            int newAmount = currentAmount + amountToAdd;
            editTextAmount.setText(String.valueOf(newAmount));
            autoAmountBox.setText("₹" + newAmount);
        });
    }
}