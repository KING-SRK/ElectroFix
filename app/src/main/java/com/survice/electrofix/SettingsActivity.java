package com.survice.electrofix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends BaseActivity {

    private ImageButton homeButton, customerProfileButton, repairerProfileButton, categoryButton, settingsButton;
    private String currentUserType;

    private Button btnChangePassword, btnDeleteAccount, btnPrivacyPolicy,
            btnTerms, btnLogout, btnClearCache, btnCheckUpdates, btnRateApp,
            btnShareApp, btnChangeLanguage;

    private Switch switchBiometric, switchPushNotification,
            switchEmailAlerts, switchSmsAlerts;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final String[] languages = {
            "English", "Hindi", "Bengali", "Tamil", "Telugu", "Marathi", "Gujarati",
            "Punjabi", "Malayalam", "Kannada", "Odia", "Urdu", "Assamese"
    };

    private final String[] languageCodes = {
            "en", "hi", "bn", "ta", "te", "mr", "gu", "pa", "ml", "kn", "or", "ur", "as"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);

        setContentView(R.layout.activity_setting);

        editor = sharedPreferences.edit();

        initViews();
        initBottomNavigation();
        initSwitchStates();
        loadSwitchStatesFromFirebase();
        initSettingsActions();
        checkCurrentUserType(); // এখানে কল করলে currentUserType ঠিকভাবে কাজ করবে
    }

    private void initViews() {
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

        switchBiometric = findViewById(R.id.switchBiometric);
        switchPushNotification = findViewById(R.id.switchPushNotification);
        switchEmailAlerts = findViewById(R.id.switchEmailAlerts);
        switchSmsAlerts = findViewById(R.id.switchSmsAlerts);

        homeButton = findViewById(R.id.home_button);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        repairerProfileButton = findViewById(R.id.repairer_profile_button);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);

        customerProfileButton.setVisibility(View.GONE);
        repairerProfileButton.setVisibility(View.GONE);
    }

    private void initBottomNavigation() {
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        });

        customerProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, CustomerProfileActivity.class));
            finish();
        });

        repairerProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, RepairerProfileActivity.class));
            finish();
        });

        categoryButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, CategoryActivity.class));
            finish();
        });

        settingsButton.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "You are already in Settings", Toast.LENGTH_SHORT).show();
        });
    }

    private void initSwitchStates() {
        switchBiometric.setChecked(sharedPreferences.getBoolean("biometric", false));
        switchPushNotification.setChecked(sharedPreferences.getBoolean("pushNotification", true));
        switchEmailAlerts.setChecked(sharedPreferences.getBoolean("emailAlerts", true));
        switchSmsAlerts.setChecked(sharedPreferences.getBoolean("smsAlerts", true));

        updateSwitchColor(switchBiometric, switchBiometric.isChecked());
        updateSwitchColor(switchPushNotification, switchPushNotification.isChecked());
        updateSwitchColor(switchEmailAlerts, switchEmailAlerts.isChecked());
        updateSwitchColor(switchSmsAlerts, switchSmsAlerts.isChecked());
    }

    private void initSettingsActions() {
        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        switchBiometric.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("biometric", isChecked).apply();
            updateSwitchColor(switchBiometric, isChecked);
        });

        switchPushNotification.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("pushNotification", isChecked).apply();
            updateSwitchColor(switchPushNotification, isChecked);
            saveSwitchStateToFirebase("pushNotification", isChecked);
        });

        switchEmailAlerts.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("emailAlerts", isChecked).apply();
            updateSwitchColor(switchEmailAlerts, isChecked);
            saveSwitchStateToFirebase("emailAlerts", isChecked);
        });

        switchSmsAlerts.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("smsAlerts", isChecked).apply();
            updateSwitchColor(switchSmsAlerts, isChecked);
            saveSwitchStateToFirebase("smsAlerts", isChecked);
        });

        btnChangeLanguage.setOnClickListener(v -> showLanguageDialog());
        btnClearCache.setOnClickListener(v -> Toast.makeText(this, "Cache Cleared!", Toast.LENGTH_SHORT).show());
        btnCheckUpdates.setOnClickListener(v -> Toast.makeText(this, "No Updates Available", Toast.LENGTH_SHORT).show());

        btnRateApp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.survice.electrofix"));
            startActivity(intent);
        });

        btnShareApp.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check out ElectroFix: https://play.google.com/store/apps/details?id=com.survice.electrofix");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnLogout.setOnClickListener(v -> {
            editor.clear().apply();
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ChoiceActivity.class));
            finish();
        });

        btnPrivacyPolicy.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://electrofix.com/privacy"))));

        btnTerms.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://electrofix.com/terms"))));
    }

    private void updateSwitchColor(Switch switchBtn, boolean isChecked) {
        int color = isChecked ? R.color.switch_thumb_on : R.color.switch_thumb;
        switchBtn.getThumbDrawable().setColorFilter(
                ContextCompat.getColor(this, color), PorterDuff.Mode.SRC_IN);
    }

    private void checkCurrentUserType() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("userType");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserType = snapshot.getValue(String.class);
                    updateProfileButtonVisibility();
                    Log.d("SettingsActivity", "User type: " + currentUserType);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileButtonVisibility() {
        if ("Customer".equalsIgnoreCase(currentUserType)) {
            customerProfileButton.setVisibility(View.VISIBLE);
            repairerProfileButton.setVisibility(View.GONE);
        } else if ("Repairer".equalsIgnoreCase(currentUserType)) {
            repairerProfileButton.setVisibility(View.VISIBLE);
            customerProfileButton.setVisibility(View.GONE);
        } else {
            customerProfileButton.setVisibility(View.GONE);
            repairerProfileButton.setVisibility(View.GONE);
        }
    }

    private void showLanguageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choose Language")
                .setItems(languages, (dialog, which) -> {
                    editor.putString("Selected_Language", languageCodes[which]).apply();
                    Toast.makeText(this, "Language set to " + languages[which], Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void saveSwitchStateToFirebase(String key, boolean value) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(user.getUid())
                    .child("Settings");

            ref.child(key).setValue(value);
        }
    }

    private void loadSwitchStatesFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(user.getUid())
                .child("Settings");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("pushNotification")) {
                        boolean value = snapshot.child("pushNotification").getValue(Boolean.class);
                        switchPushNotification.setChecked(value);
                        updateSwitchColor(switchPushNotification, value);
                    }
                    if (snapshot.hasChild("emailAlerts")) {
                        boolean value = snapshot.child("emailAlerts").getValue(Boolean.class);
                        switchEmailAlerts.setChecked(value);
                        updateSwitchColor(switchEmailAlerts, value);
                    }
                    if (snapshot.hasChild("smsAlerts")) {
                        boolean value = snapshot.child("smsAlerts").getValue(Boolean.class);
                        switchSmsAlerts.setChecked(value);
                        updateSwitchColor(switchSmsAlerts, value);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}
