package com.survice.electrofix;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends BaseActivity { // Assuming BaseActivity is properly defined

    private static final String TAG = "SettingsActivity"; // For logging

    private ImageButton homeButton, customerProfileButton, categoryButton, settingsButton;
    private String currentUserType; // Stores the user type after fetching from DB
    private TextView customerProfileText;
    private Button btnChangePassword, btnDeleteAccount, btnPrivacyPolicy, btnTerms, btnLogout,
            btnClearCache, btnCheckUpdates, btnRateApp, btnShareApp, btnChangeLanguage;
    private Switch switchBiometric, switchPushNotification, switchEmailAlerts, switchSmsAlerts, themeSwitch;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;

    public static class UserModel {
        public String userId, email, phone, address, userType, profileImage;
        public UserModel() {}
        public UserModel(String userId, String email, String phone, String address, String userType) {
            this.userId = userId;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.userType = userType;
            this.profileImage = "";
        }
    }

    private final String[] languages = {
            "English", "Hindi", "Bengali", "Tamil", "Telugu", "Marathi", "Gujarati",
            "Punjabi", "Malayalam", "Kannada", "Odia", "Urdu", "Assamese"
    };

    private final String[] languageCodes = {
            "en", "hi", "bn", "ta", "te", "mr", "gu", "pa", "ml", "kn", "or", "ur", "as"
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize SharedPreferences FIRST, before super.onCreate if theme depends on it
        sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        // Apply theme preference before calling super.onCreate for immediate effect
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editor = sharedPreferences.edit();

        // --- Firebase Initialization (CRUCIAL: Do this early in onCreate) ---
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // --- Initialize UI Views ---
        initViews(); // Call this to bind all UI elements first

        // --- Setup Navigation and other UI listeners ---
        initBottomNavigation();
        initSwitchStates(); // Load states from SharedPreferences
        initSettingsActions(); // Setup listeners for all settings buttons and switches
        setupButtonClickListeners();
        // --- Check current user and load their profile/permissions ---
        checkCurrentUser(); // This handles visibility of profile button and loads user type
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
        themeSwitch = findViewById(R.id.themeSwitch);

        homeButton = findViewById(R.id.home_button);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        customerProfileText = findViewById(R.id.customer_profile_text);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);
    }

    private void initSwitchStates() {
        switchBiometric.setChecked(sharedPreferences.getBoolean("biometric", false));
        switchPushNotification.setChecked(sharedPreferences.getBoolean("pushNotification", true));
        switchEmailAlerts.setChecked(sharedPreferences.getBoolean("emailAlerts", true));
        switchSmsAlerts.setChecked(sharedPreferences.getBoolean("smsAlerts", true));
        themeSwitch.setChecked(sharedPreferences.getBoolean("DarkMode", false));

        updateSwitchColor(switchBiometric, switchBiometric.isChecked());
        updateSwitchColor(switchPushNotification, switchPushNotification.isChecked());
        updateSwitchColor(switchEmailAlerts, switchEmailAlerts.isChecked());
        updateSwitchColor(switchSmsAlerts, switchSmsAlerts.isChecked());
        updateSwitchColor(themeSwitch, themeSwitch.isChecked());
    }

    private void initBottomNavigation() {
        homeButton.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, MainActivity.class)));
        categoryButton.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, CategoryActivity.class)));
        settingsButton.setOnClickListener(v ->
                Toast.makeText(SettingsActivity.this, "You are already in Settings", Toast.LENGTH_SHORT).show());
    }

    private void setupButtonClickListeners() {
        if (customerProfileButton != null) {
            customerProfileButton.setOnClickListener(v -> {
                if (isLoggedIn()) {
                    startActivity(new Intent(SettingsActivity.this, CustomerProfileActivity.class));
                } else {
                    showLoginPrompt("Please login to access your profile.");
                }
            });
        }
    }

    private boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private void showLoginPrompt(String message) {
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Login Required")
                .setMessage(message)
                .setPositiveButton("Login / Signup", (dialog, which) -> {
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Checks current Firebase user status and fetches userType from Realtime Database.
     * Adjusts UI visibility based on login status and user type.
     */
    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User is not logged in
            Log.d(TAG, "No user is logged in.");
            if (customerProfileButton != null) {
                customerProfileButton.setVisibility(View.VISIBLE);
                customerProfileText.setText("Login/Signup");
                customerProfileText.setVisibility(View.VISIBLE);
            }
        } else {
            // User is logged in, now fetch their userType from the Realtime Database
            String uid = currentUser.getUid();
            Log.d(TAG, "User logged in. UID: " + uid);

            userDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null && "Customer".equals(userModel.userType)) {
                            currentUserType = userModel.userType;
                            Log.d(TAG, "User type: " + currentUserType);

                            if (customerProfileButton != null) {
                                customerProfileButton.setVisibility(View.VISIBLE);
                                customerProfileText.setText("Profile");
                                customerProfileText.setVisibility(View.VISIBLE);
                                loadUserProfileLogo(uid);
                            }
                        } else {
                            String message = "This app is only for Customers. Please use Technician app or signup with a customer account.";
                            Log.w(TAG, message);
                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        String message = "User profile data not found in database for UID: " + uid + "! Please complete registration or contact support.";
                        Log.e(TAG, message);
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    String message = "Database error checking user type: " + error.getMessage();
                    Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
                    Log.e(TAG, message, error.toException());
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                }
            });
        }
    }


    private void initSettingsActions() {
        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        switchBiometric.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("biometric", isChecked).apply();
            updateSwitchColor(switchBiometric, isChecked);
            // Consider saving biometric preference to Firebase as well if you want it synced across devices
        });

        switchPushNotification.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("pushNotification", isChecked).apply();
            updateSwitchColor(switchPushNotification, isChecked);
            saveSwitchStateToFirebase("pushNotification", isChecked); // Save to Firebase
        });

        switchEmailAlerts.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("emailAlerts", isChecked).apply();
            updateSwitchColor(switchEmailAlerts, isChecked);
            saveSwitchStateToFirebase("emailAlerts", isChecked); // Save to Firebase
        });

        switchSmsAlerts.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean("smsAlerts", isChecked).apply();
            updateSwitchColor(switchSmsAlerts, isChecked);
            saveSwitchStateToFirebase("smsAlerts", isChecked); // Save to Firebase
        });

       

            themeSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                editor.putBoolean("DarkMode", isChecked).apply();
                updateSwitchColor(themeSwitch, isChecked);

                // Apply the theme change globally
                AppCompatDelegate.setDefaultNightMode(isChecked ?
                        AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

                // ✅ This is the correct way to apply the theme change
                recreate();
                // Optional: Add this line to remove the screen flashing
                overridePendingTransition(0, 0);
            });


        btnChangeLanguage.setOnClickListener(v -> showLanguageDialog());
        btnClearCache.setOnClickListener(v -> Toast.makeText(this, "Cache Cleared!", Toast.LENGTH_SHORT).show());
        btnCheckUpdates.setOnClickListener(v -> Toast.makeText(this, "No Updates Available", Toast.LENGTH_SHORT).show());
        btnRateApp.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + getPackageName())))); // Use getPackageName() for current app

        btnShareApp.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check out ElectroFix: https://play.google.com/store/apps/details?id=" + getPackageName()); // Use getPackageName()
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnDeleteAccount.setOnClickListener(v -> {
            // Implement actual delete account logic here, possibly with a confirmation dialog
            // and re-authentication for security
            Toast.makeText(this, "Delete Account functionality coming soon!", Toast.LENGTH_SHORT).show();
            // Example: showDeleteAccountConfirmation();
        });

        btnLogout.setOnClickListener(v -> {
            editor.clear().apply(); // Clear all shared preferences
            mAuth.signOut(); // Use initialized mAuth
            Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ChoiceActivity.class)); // Assuming ChoiceActivity is your landing page for unauthenticated users
            finishAffinity(); // Clears all activities from this task
        });

        btnPrivacyPolicy.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://electrofix.com/privacy"))));

        btnTerms.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://electrofix.com/terms"))));
    }

    private void updateSwitchColor(Switch switchBtn, boolean isChecked) {
        int colorResId = isChecked ? R.color.switch_thumb_on : R.color.switch_thumb;
        // Ensure switch_thumb_on and switch_thumb colors are defined in your colors.xml
        switchBtn.getThumbDrawable().setColorFilter(
                ContextCompat.getColor(this, colorResId), PorterDuff.Mode.SRC_IN);
        switchBtn.getTrackDrawable().setColorFilter( // Also update track color for better visual
                ContextCompat.getColor(this, isChecked ? R.color.switch_track_on : R.color.switch_track), PorterDuff.Mode.SRC_IN);
    }

    // ===== Profile Image Loading Logic =====
    private void loadUserProfileLogo(String uid) {
        DatabaseReference profileImageRef = userDatabase.child(uid).child("profileImage"); // Use userDatabase

        profileImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && customerProfileButton != null) {
                    String url = snapshot.getValue(String.class);
                    if (url != null && !url.isEmpty()) {
                        Glide.with(SettingsActivity.this)
                                .load(url)
                                .placeholder(R.drawable.ic_profile) // Placeholder while loading
                                .error(R.drawable.ic_profile) // Error image if loading fails
                                .into(customerProfileButton);
                    } else {
                        customerProfileButton.setImageResource(R.drawable.ic_profile); // Default if URL is empty
                    }
                } else if (customerProfileButton != null) {
                    customerProfileButton.setImageResource(R.drawable.ic_profile); // Default if no image exists
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile image: " + error.getMessage());
                if (customerProfileButton != null) {
                    customerProfileButton.setImageResource(R.drawable.ic_profile); // Default on error
                }
            }
        });
    }

    // ===== Firebase Switch Methods (already well-implemented) =====

    private void saveSwitchStateToFirebase(String key, boolean value) {
        FirebaseUser user = mAuth.getCurrentUser(); // Use initialized mAuth
        if (user != null) {
            DatabaseReference ref = userDatabase.child(user.getUid()).child("Settings"); // Use userDatabase
            ref.child(key).setValue(value)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Setting " + key + " saved successfully to Firebase.");
                        } else {
                            Log.e(TAG, "Failed to save " + key + " to Firebase: " + task.getException().getMessage());
                        }
                    });
        } else {
            Log.d(TAG, "User not logged in, cannot save settings to Firebase.");
        }
    }

    private void loadSwitchStatesFromFirebase() {
        FirebaseUser user = mAuth.getCurrentUser(); // Use initialized mAuth
        if (user == null) {
            Log.d(TAG, "No user logged in, cannot load settings from Firebase.");
            return;
        }

        DatabaseReference ref = userDatabase.child(user.getUid()).child("Settings"); // Use userDatabase

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Firebase settings snapshot exists.");
                    if (snapshot.hasChild("pushNotification")) {
                        boolean value = Boolean.TRUE.equals(snapshot.child("pushNotification").getValue(Boolean.class)); // Safe null check
                        switchPushNotification.setChecked(value);
                        updateSwitchColor(switchPushNotification, value);
                    }
                    if (snapshot.hasChild("emailAlerts")) {
                        boolean value = Boolean.TRUE.equals(snapshot.child("emailAlerts").getValue(Boolean.class));
                        switchEmailAlerts.setChecked(value);
                        updateSwitchColor(switchEmailAlerts, value);
                    }
                    if (snapshot.hasChild("smsAlerts")) {
                        boolean value = Boolean.TRUE.equals(snapshot.child("smsAlerts").getValue(Boolean.class));
                        switchSmsAlerts.setChecked(value);
                        updateSwitchColor(switchSmsAlerts, value);
                    }
                } else {
                    Log.d(TAG, "No Firebase settings found for user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Error loading settings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading switch states from Firebase: " + error.getMessage(), error.toException());
            }
        });
    }

    private void showLanguageDialog() {
        int checkedItem = -1;
        String currentLangCode = sharedPreferences.getString("Selected_Language", "en");
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLangCode)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Choose Language")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLanguageCode = languageCodes[which];
                    String selectedLanguageName = languages[which];
                    editor.putString("Selected_Language", selectedLanguageCode).apply();
                    Toast.makeText(this, "Language set to " + selectedLanguageName, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Optional: recreate activity to apply language changes if needed (requires more complex localization setup)
                    // recreate();
                }).show();
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