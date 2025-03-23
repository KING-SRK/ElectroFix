package com.survice.electrofix;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnEditProfile, btnChangePassword, btnDeleteAccount, btnPrivacyPolicy, btnTerms, btnLogout, btnClearCache, btnCheckUpdates, btnRateApp, btnShareApp, btnChangeLanguage;
    private Switch switchDarkMode, switchBiometric, switchPushNotification, switchEmailAlerts, switchSmsAlerts;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String userType;

    private String[] languages = {"English", "Hindi", "Bengali", "Tamil", "Telugu", "Marathi", "Gujarati",
            "Punjabi", "Malayalam", "Kannada", "Odia", "Urdu", "Assamese"};

    private String[] languageCodes = {"en", "hi", "bn", "ta", "te", "mr", "gu", "pa", "ml", "kn", "or", "ur", "as"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize Buttons & Switches
        btnBack = findViewById(R.id.btnBack);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnTerms = findViewById(R.id.btnTerms);
        btnLogout = findViewById(R.id.btnLogout);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnCheckUpdates = findViewById(R.id.btnCheckUpdates);
        btnRateApp = findViewById(R.id.btnRateApp);
        btnShareApp = findViewById(R.id.btnShareApp);
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage);

        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchBiometric = findViewById(R.id.switchBiometric);
        switchPushNotification = findViewById(R.id.switchPushNotification);
        switchEmailAlerts = findViewById(R.id.switchEmailAlerts);
        switchSmsAlerts = findViewById(R.id.switchSmsAlerts);

        // SharedPreferences Setup
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        userType = sharedPreferences.getString("userType", "customer"); // Default "customer"

        // Load Saved Settings
        switchDarkMode.setChecked(sharedPreferences.getBoolean("darkMode", false));
        switchBiometric.setChecked(sharedPreferences.getBoolean("biometric", false));
        switchPushNotification.setChecked(sharedPreferences.getBoolean("pushNotification", true));
        switchEmailAlerts.setChecked(sharedPreferences.getBoolean("emailAlerts", true));
        switchSmsAlerts.setChecked(sharedPreferences.getBoolean("smsAlerts", true));

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Edit Profile Click
        btnEditProfile.setOnClickListener(v -> {
            if (userType.equals("repairer")) {
                startActivity(new Intent(SettingsActivity.this, RepairerEditProfileActivity.class));
            } else {
                startActivity(new Intent(SettingsActivity.this, CustomerEditProfileActivity.class));
            }
        });

        // Dark Mode Toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("darkMode", isChecked);
            editor.apply();
            Toast.makeText(this, "Dark Mode " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            updateSwitchColor(switchDarkMode, isChecked);
        });

        // Biometric Authentication Toggle
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("biometric", isChecked);
            editor.apply();
            Toast.makeText(this, "Biometric Authentication " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            updateSwitchColor(switchBiometric, isChecked);
        });

        // Notification Toggles
        switchPushNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("pushNotification", isChecked);
            editor.apply();
            updateSwitchColor(switchPushNotification, isChecked);
        });

        switchEmailAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("emailAlerts", isChecked);
            editor.apply();
            updateSwitchColor(switchEmailAlerts, isChecked);
        });

        switchSmsAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("smsAlerts", isChecked);
            editor.apply();
            updateSwitchColor(switchSmsAlerts, isChecked);
        });

        // Change Language Button
        btnChangeLanguage.setOnClickListener(v -> showLanguageDialog());

        // Clear Cache
        btnClearCache.setOnClickListener(v -> {
            Toast.makeText(this, "Cache Cleared!", Toast.LENGTH_SHORT).show();
        });

        // Check for Updates
        btnCheckUpdates.setOnClickListener(v -> {
            Toast.makeText(this, "No Updates Available", Toast.LENGTH_SHORT).show();
        });

        // Rate App
        btnRateApp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.survice.electrofix"));
            startActivity(intent);
        });

        // Share App
        btnShareApp.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out ElectroFix: https://play.google.com/store/apps/details?id=com.survice.electrofix");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Logout Button
        btnLogout.setOnClickListener(v -> {
            editor.clear();
            editor.apply();
            Toast.makeText(SettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, ChoiceActivity.class));
            finish();
        });

        // Privacy Policy
        btnPrivacyPolicy.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://electrofix.com/privacy")));
        });

        // Terms & Conditions
        btnTerms.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://electrofix.com/terms")));
        });

        // Switch Color Set Initially
        updateSwitchColor(switchDarkMode, switchDarkMode.isChecked());
        updateSwitchColor(switchBiometric, switchBiometric.isChecked());
        updateSwitchColor(switchPushNotification, switchPushNotification.isChecked());
        updateSwitchColor(switchEmailAlerts, switchEmailAlerts.isChecked());
        updateSwitchColor(switchSmsAlerts, switchSmsAlerts.isChecked());
    }

    // Language Selection Dialog
    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Language");
        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedLanguage = languageCodes[which];
                saveLanguagePreference(selectedLanguage);
            }
        });
        builder.show();
    }

    private void saveLanguagePreference(String languageCode) {
        SharedPreferences.Editor editor = getSharedPreferences("AppSettings", MODE_PRIVATE).edit();
        editor.putString("Selected_Language", languageCode);
        editor.apply();

        // নতুন ভাষা সেট এবং অ্যাপ Restart
        LanguageUtils.setLocale(this, languageCode);
    }

    // Method to Change Switch Color
    private void updateSwitchColor(Switch switchButton, boolean isChecked) {
        int trackColor = isChecked ? R.color.switch_track_on : R.color.switch_track;
        int thumbColor = isChecked ? R.color.switch_thumb_on : R.color.switch_thumb;
        switchButton.getTrackDrawable().setColorFilter(ContextCompat.getColor(this, trackColor), PorterDuff.Mode.SRC_IN);
        switchButton.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, thumbColor), PorterDuff.Mode.SRC_IN);
    }
}