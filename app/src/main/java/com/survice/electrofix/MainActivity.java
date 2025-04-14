package com.survice.electrofix;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
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

    private ImageButton  btnNotification;

    private ImageButton homeButton, categoryButton, settingsButton;
    private LinearLayout customerProfileLayout, repairerProfileLayout;
    private ImageButton customerProfileButton, repairerProfileButton;
    private TextView customerProfileText, repairerProfileText;
    private ProgressBar loadingProgressBar;
    private SearchView searchView;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String currentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isConnected()) {
            startActivity(new Intent(MainActivity.this, NoNetworkActivity.class));
            finish();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        initializeUI();
        setupButtonClickListeners();
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
        customerProfileLayout = findViewById(R.id.customer_profile_layout);
        repairerProfileLayout = findViewById(R.id.repairer_profile_layout);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        repairerProfileButton = findViewById(R.id.repairer_profile_button);
        customerProfileText = findViewById(R.id.customer_profile_text);
        repairerProfileText = findViewById(R.id.repairer_profile_text);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);

        customerProfileLayout.setVisibility(View.GONE);
        repairerProfileLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void setupButtonClickListeners() {
        

        btnNotification.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));
        btnPayment.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PaymentActivity.class)));
        btnUploadIssue.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UploadIssueActivity.class)));
        btnTracking.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrackingActivity.class)));
        btnHelpSupport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpSupportActivity.class)));
        categoryButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CategoryActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        customerProfileButton.setOnClickListener(v -> {
            if ("Customer".equals(currentUserType)) {
                startActivity(new Intent(MainActivity.this, CustomerProfileActivity.class));
            } else {
                Toast.makeText(this, "Access Denied! You are a Repairer.", Toast.LENGTH_LONG).show();
            }
        });

        repairerProfileButton.setOnClickListener(v -> {
            if ("Repairer".equals(currentUserType)) {
                startActivity(new Intent(MainActivity.this, RepairerProfileActivity.class));
            } else {
                Toast.makeText(this, "Access Denied! You are a Customer.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserType(currentUser.getUid());
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserType(String userId) {
        userDatabase.child(userId).child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserType = snapshot.getValue(String.class);
                    loadingProgressBar.setVisibility(View.GONE);

                    if ("Customer".equals(currentUserType)) {
                        customerProfileLayout.setVisibility(View.VISIBLE);
                    } else if ("Repairer".equals(currentUserType)) {
                        repairerProfileLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "User type not found!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                //Toast.makeText(this, "Location Permission প্রয়োজন! দয়া করে অনুমতি দিন।", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getUserLocation() {
        //Toast.makeText(this, "আপনার বর্তমান অবস্থান নেওয়া হচ্ছে...", Toast.LENGTH_SHORT).show();
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

}