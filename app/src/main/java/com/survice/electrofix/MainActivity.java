package com.survice.electrofix;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private ImageButton btnPayment, btnTracking, btnHelpSupport, btnUploadIssue;
    private ImageButton btnNotification;
    private ImageButton homeButton, categoryButton, settingsButton;
    private ImageButton customerProfileButton, repairerProfileButton;
    private TextView customerProfileText, repairerProfileText;
    private ProgressBar loadingProgressBar;
    private SearchView searchView;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String currentUserType;

    private SharedPreferences sharedPreferences;

    // ডার্ক মোডের জন্য UI উপাদান
    private Switch themeSwitch;
    private TextView themeSwitchLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // SharedPreferences init
        sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);

        // ডার্ক মোড প্রয়োগ
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        // ইন্টারনেট কানেকশন চেক
        if (!isConnected()) {
            startActivity(new Intent(MainActivity.this, NoNetworkActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        initializeUI();
        setupButtonClickListeners();
        setupThemeSwitch();  // ডার্ক মোড Switch সেটআপ

        checkCurrentUser();
        checkLocationPermission();
    }

    private void initializeUI() {
        searchView = findViewById(R.id.searchView);
        homeButton = findViewById(R.id.home_button);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);
        btnNotification = findViewById(R.id.btnNotification);
        btnPayment = findViewById(R.id.btnPayment);
        btnTracking = findViewById(R.id.btnTracking);
        btnHelpSupport = findViewById(R.id.btnHelpSupport);
        btnUploadIssue = findViewById(R.id.btnUpload);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        repairerProfileButton = findViewById(R.id.repairer_profile_button);
        customerProfileText = findViewById(R.id.customer_profile_text);
        repairerProfileText = findViewById(R.id.repairer_profile_text);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);

        // ডার্ক মোড Switch এবং তার TextView
        themeSwitch = findViewById(R.id.themeSwitch);
        themeSwitchLabel = findViewById(R.id.themeSwitchLabel);

        if (customerProfileButton != null) customerProfileButton.setVisibility(View.GONE);
        if (repairerProfileButton != null) repairerProfileButton.setVisibility(View.GONE);
        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);

        if (searchView != null) {
            searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                    searchView.clearFocus();
                }
            });
        }
    }

    private void setupButtonClickListeners() {
        if (btnNotification != null)
            btnNotification.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));

        if (btnPayment != null)
            btnPayment.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PaymentActivity.class)));

        if (btnUploadIssue != null)
            btnUploadIssue.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UploadIssueActivity.class)));

        if (btnTracking != null)
            btnTracking.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrackingActivity.class)));

        if (btnHelpSupport != null)
            btnHelpSupport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpSupportActivity.class)));

        if (categoryButton != null)
            categoryButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CategoryActivity.class)));

        if (settingsButton != null)
            settingsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        if (customerProfileButton != null) {
            customerProfileButton.setOnClickListener(v -> {
                if ("Customer".equals(currentUserType)) {
                    startActivity(new Intent(MainActivity.this, CustomerProfileActivity.class));
                } else {
                    Toast.makeText(this, "Access Denied! You are a Repairer.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (repairerProfileButton != null) {
            repairerProfileButton.setOnClickListener(v -> {
                if ("Repairer".equals(currentUserType)) {
                    startActivity(new Intent(MainActivity.this, RepairerProfileActivity.class));
                } else {
                    Toast.makeText(this, "Access Denied! You are a Customer.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // MainActivity এর ভিতরে যোগ করো:

    private void recreateWithoutFlicker() {
        // Animation disable
        getWindow().setWindowAnimations(0);

        // Activity recreate
        recreate();

        // Animation আবার enable করো (দ্রুত recreate হবে বলে অনেক সময় delay লাগে না দিতে)
        getWindow().setWindowAnimations(android.R.style.Animation_Activity);
    }

    private void setupThemeSwitch() {
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);

        // Switch এর স্টেট এবং Label সেট করুন
        themeSwitch.setChecked(isDarkMode);
        updateThemeSwitchLabel(isDarkMode);

        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // SharedPreferences এ সেভ করুন
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("DarkMode", isChecked);
                editor.apply();

                // Label আপডেট করুন
                updateThemeSwitchLabel(isChecked);

                // মোড পরিবর্তন
                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );

                // ফ্লিকারিং ছাড়াই Activity restart করো
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }



    private void updateThemeSwitchLabel(boolean isDarkMode) {
        if (isDarkMode) {
            themeSwitchLabel.setText("SWITCH TO LIGHT MODE");
        } else {
            themeSwitchLabel.setText("SWITCH TO DARK MODE");
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserType(currentUser.getUid());
        } else {
            if (loadingProgressBar != null)
                loadingProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserType(String userId) {
        userDatabase.child(userId).child("userType")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentUserType = snapshot.getValue(String.class);

                            if (loadingProgressBar != null)
                                loadingProgressBar.setVisibility(View.GONE);

                            if ("Customer".equals(currentUserType)) {
                                if (customerProfileButton != null)
                                    customerProfileButton.setVisibility(View.VISIBLE);
                            } else if ("Repairer".equals(currentUserType)) {
                                if (repairerProfileButton != null)
                                    repairerProfileButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (loadingProgressBar != null)
                                loadingProgressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "User type not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (loadingProgressBar != null)
                            loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();

        // শুধুমাত্র প্রয়োজনে recreate করুন
        if ((isDarkMode && currentNightMode != AppCompatDelegate.MODE_NIGHT_YES) ||
                (!isDarkMode && currentNightMode != AppCompatDelegate.MODE_NIGHT_NO)) {
            AppCompatDelegate.setDefaultNightMode(
                    isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        // তোমার লোকেশন ফিচার এখানে লিখো
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
