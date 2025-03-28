package com.survice.electrofix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {

    // 🔹 UI Variables
    private ImageButton btnPayment, btnTracking, btnOffer, btnHelpSupport;
    private ImageButton btnUploadIssue, btnBilling, btnTerms, btnRequest;
    private ImageButton homeButton, categoryButton, settingsButton;
    private LinearLayout customerProfileLayout, repairerProfileLayout;
    private ImageButton customerProfileButton, repairerProfileButton;
    private TextView customerProfileText, repairerProfileText;
    private ProgressBar loadingProgressBar;
    private SearchView searchView;

    // 🔹 Firebase & Auth
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String currentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🔹 Dark Mode চেক করা
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 Status Bar & Navigation Bar Hide করা
        hideSystemBars();

        // 🔹 Firebase ইনিশিয়ালাইজ
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // 🔹 UI ইনিশিয়ালাইজ
        searchView = findViewById(R.id.searchView);
        homeButton = findViewById(R.id.home_button);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);
        btnPayment = findViewById(R.id.btnPayment);
        btnTracking = findViewById(R.id.btnTracking);
        btnOffer = findViewById(R.id.btnOffer);
        btnHelpSupport = findViewById(R.id.btnHelpSupport);
        btnUploadIssue = findViewById(R.id.btnUpload);
        btnBilling = findViewById(R.id.btnBilling);
        btnTerms = findViewById(R.id.btnTerms);
        btnRequest = findViewById(R.id.btnRequest);
        customerProfileLayout = findViewById(R.id.customer_profile_layout);
        repairerProfileLayout = findViewById(R.id.repairer_profile_layout);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        repairerProfileButton = findViewById(R.id.repairer_profile_button);
        customerProfileText = findViewById(R.id.customer_profile_text);
        repairerProfileText = findViewById(R.id.repairer_profile_text);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);

        // 🔹 "Upload Issue" Button Click
        btnUploadIssue.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UploadIssueActivity.class)));

        // 🔹 Tracking Button Click
        btnTracking.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrackingActivity.class)));

        // 🔹 প্রথমে প্রোফাইল লুকিয়ে রাখো
        customerProfileLayout.setVisibility(View.GONE);
        repairerProfileLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);

        // 🔹 বর্তমান ইউজার চেক করা
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserType(currentUser.getUid());
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
        }

        // 🔹 Search View Functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "Searching: " + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // 🔹 অন্যান্য Button Click Listeners
        btnPayment.setOnClickListener(v -> Toast.makeText(this, "Payment Clicked", Toast.LENGTH_SHORT).show());
        btnOffer.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OffersActivity.class)));
        btnHelpSupport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpSupportActivity.class)));
        btnBilling.setOnClickListener(v -> Toast.makeText(this, "Billing Clicked", Toast.LENGTH_SHORT).show());
        btnTerms.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TermsActivity.class)));
        btnRequest.setOnClickListener(v -> Toast.makeText(this, "Requests Clicked", Toast.LENGTH_SHORT).show());

        // 🔹 Bottom Navigation Click Events
        homeButton.setOnClickListener(v -> Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show());
        categoryButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CategoryActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        // 🔹 Customer & Repairer Profile Buttons
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

    // 🔹 ইউজারের টাইপ চেক করে UI আপডেট করবে
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

    // 🔹 Status Bar & Navigation Bar Hide করার জন্য Updated Function
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false); // ✅ এটা নতুন যোগ করা হয়েছে

            WindowInsetsControllerCompat windowInsetsController =
                    ViewCompat.getWindowInsetsController(getWindow().getDecorView());

            if (windowInsetsController != null) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
                windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
    }
}