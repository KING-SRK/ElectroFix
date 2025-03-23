package com.survice.electrofix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HelpSupportActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnCallSupport, btnEmailSupport, btnChatSupport, btnReportIssue, btnPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        // UI Elements Initialize
        btnBack = findViewById(R.id.btnBack);
        btnCallSupport = findViewById(R.id.btnCallSupport);
        btnEmailSupport = findViewById(R.id.btnEmailSupport);
        btnChatSupport = findViewById(R.id.btnChatSupport);
        btnReportIssue = findViewById(R.id.btnReportIssue);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);


        // 🔹 Back Button (Go Back)
        btnBack.setOnClickListener(v -> onBackPressed());

        // 🔹 Call Support
        btnCallSupport.setOnClickListener(v -> makeCall("+8801234567890"));

        // 🔹 Email Support
        btnEmailSupport.setOnClickListener(v -> sendEmail("support@electrofix.com", "Support Request", "Hello Support Team,\n\n"));

        // 🔹 Chat Support
        btnChatSupport.setOnClickListener(v -> openUrl("https://electrofix.com/chat"));

        // 🔹 Report an Issue
        btnReportIssue.setOnClickListener(v -> {
            Intent intent = new Intent(HelpSupportActivity.this, HelpReportIssueActivity.class);
            startActivity(intent);
        });

        // 🔹 Privacy Policy
        btnPrivacyPolicy.setOnClickListener(v -> openUrl("https://electrofix.com/privacy"));



    }

    // 🔸 Function: Call a Number
    private void makeCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    // 🔸 Function: Send Email
    private void sendEmail(String email, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No email app found!", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔸 Function: Open URL in Browser
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open the link!", Toast.LENGTH_SHORT).show();
        }
    }
}