package com.survice.electrofix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpSupportActivity extends BaseActivity {

    private ImageButton btnBack;
    private Button btnCallSupport, btnEmailSupport, btnChatSupport, btnReportIssue, btnPrivacyPolicy, btnTermsOfService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support); // তোমার XML layout

        // Back Button
        btnBack = findViewById(R.id.btnBack);

        // Contact Support Buttons
        btnCallSupport = findViewById(R.id.btnCallSupport);
        btnEmailSupport = findViewById(R.id.btnEmailSupport);
        btnChatSupport = findViewById(R.id.btnChatSupport);

        // Other Buttons
        btnReportIssue = findViewById(R.id.btnReportIssue);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnTermsOfService = findViewById(R.id.btnTermsOfService);

        // Back Button functionality
        btnBack.setOnClickListener(v -> onBackPressed());

        // Call Support functionality
        btnCallSupport.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:+918902709631")); // এখানে সাপোর্ট নম্বর দিন
            startActivity(callIntent);
        });

        // Email Support functionality
        btnEmailSupport.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "support@electrofix.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help & Support Request");
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        });

        btnChatSupport.setOnClickListener(v -> {
            String phoneNumber = "+918902709631";
            String message = "Hello ElectroFix Support, I need help!";

            try {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.putExtra("jid", phoneNumber.replace("+", "") + "@s.whatsapp.net");
                sendIntent.setPackage("com.whatsapp");

                startActivity(sendIntent);
            } catch (Exception e) {
                Toast.makeText(HelpSupportActivity.this, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
            }
        });



        // Report Issue functionality
        btnReportIssue.setOnClickListener(v -> {
            Intent reportIntent = new Intent(HelpSupportActivity.this, UploadIssueActivity.class);
            startActivity(reportIntent); // Report Issue Activity খুলবে
        });

        // Privacy Policy functionality
        btnPrivacyPolicy.setOnClickListener(v -> {
            Intent privacyIntent = new Intent(HelpSupportActivity.this, PrivacyPolicyActivity.class);
            startActivity(privacyIntent); // Privacy Policy Activity খুলবে
        });

        // Terms of Service functionality
        btnTermsOfService.setOnClickListener(v -> {
            Intent termsIntent = new Intent(HelpSupportActivity.this, TermsActivity.class);
            startActivity(termsIntent); // Terms of Service Activity খুলবে
        });
    }
}