package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    private TextView txtTerms;
    private Button btnAccept;
    private ImageButton btnBack; // 🔹 Back Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        // 🔹 UI Elements
        txtTerms = findViewById(R.id.txtTerms);
        btnAccept = findViewById(R.id.btnAccept);
        btnBack = findViewById(R.id.btnBack); // 🔹 Back Button

        // 🔹 Load Terms & Conditions
        txtTerms.setText(getTermsAndConditions());

        // 🔹 Accept & Continue Button Click
        btnAccept.setOnClickListener(v -> {
            Intent intent = new Intent(TermsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 🔹 Back Button Click - Go Back to MainActivity
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    // 🔹 Method to Load Terms & Conditions
    private String getTermsAndConditions() {
        return "📜 Terms & Conditions\n\n" +
                "1️⃣ By using this app, you agree to follow all terms.\n" +
                "2️⃣ We collect your data to improve our services.\n" +
                "3️⃣ Payments are secure and non-refundable.\n" +
                "4️⃣ Unauthorized activities are strictly prohibited.\n" +
                "5️⃣ The company holds all rights to modify these terms.\n\n" +
                "For more details, contact: support@electrofix.com";
    }
}