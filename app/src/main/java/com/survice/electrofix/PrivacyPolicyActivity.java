package com.survice.electrofix;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends BaseActivity {

    private TextView tvPrivacyPolicy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Privacy Policy TextView
        tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);

        // Set Privacy Policy content
        String privacyPolicyText = "Privacy Policy\n\n" +
                "At ElectroFix, we value your privacy. This Privacy Policy outlines how we collect, use, and protect your personal data.\n\n" +
                "1. *Data Collection*\n" +
                "We collect the following personal information: name, email, phone number, and location data. This information is used to provide our services and improve user experience.\n\n" +
                "2. *Data Usage*\n" +
                "We use the collected data to process service bookings, send service updates, and offer customer support. Your information is also used for billing and communication purposes.\n\n" +
                "3. *Data Sharing*\n" +
                "We do not share your personal data with third parties except as necessary for providing our services. We may share your data with repairers for booking purposes.\n\n" +
                "4. *Security*\n" +
                "We implement strong security measures to protect your personal data. However, no method of transmission over the internet is 100% secure, and we cannot guarantee absolute security.\n\n" +
                "5. *Cookies*\n" +
                "We may use cookies to enhance your experience and for analytics purposes. You can choose to disable cookies in your browser settings.\n\n" +
                "6. *Changes to the Privacy Policy*\n" +
                "We may update this Privacy Policy from time to time. Any changes will be posted on this page with the updated date.\n\n" +
                "If you have any questions about our privacy policy, feel free to contact us.\n\n" +
                "Last updated: [Date]";

        // Set the text to TextView
        tvPrivacyPolicy.setText(privacyPolicyText);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}