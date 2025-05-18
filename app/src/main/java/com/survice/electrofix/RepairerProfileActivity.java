package com.survice.electrofix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RepairerProfileActivity extends BaseActivity {

    private ImageView repairerProfileImage;
    private TextView repairerName;
    private Button btnProfileInfo, btnViewBookings, btnViewRatings, btnViewWorkHistory, btnLogout;
    private ImageButton homeButton, repairerProfileButton, categoryButton, settingsButton;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the activity to fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_repairer_profile);

        // Initialize UI components
        repairerProfileImage = findViewById(R.id.repairerProfileImage);
        repairerName = findViewById(R.id.repairerName);
        btnProfileInfo = findViewById(R.id.btnProfileInfo);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnViewRatings = findViewById(R.id.btnViewRatings);
        btnViewWorkHistory = findViewById(R.id.btnViewWorkHistory);
        btnLogout = findViewById(R.id.btnLogout);
        homeButton = findViewById(R.id.home_button);

        // Bottom navigation buttons
        repairerProfileButton = findViewById(R.id.repairer_profile_button);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            logoutUser();
        } else {
            repairerName.setText(currentUser.getDisplayName()); // Display name of the repairer
            // You can also set the profile image if you want
        }

        // Profile Info
        btnProfileInfo.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerProfileInfoActivity.class);
            intent.putExtra("repairer_id", currentUser.getUid());
            startActivity(intent);
        });

        // View Booking List
        btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerBookingActivity.class);
            startActivity(intent);
        });

        // View Ratings
        btnViewRatings.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerSetBudgetActivity.class);
            startActivity(intent);
        });

        // View Work History
        btnViewWorkHistory.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerWorkHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(RepairerProfileActivity.this)
                    .setTitle("⚠ Logout")
                    .setMessage("Are you sure you want to log out from ElectroFix?")
                    .setIcon(R.drawable.ic_logout_warning) // ✅ তোমার drawable-এ একটি custom icon রাখো (যদি না থাকে, বাদ দিতে পারো)
                    .setPositiveButton("Yes, Logout", (dialogInterface, i) -> logoutUser())
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dlg -> {
                // Style the buttons
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(R.color.red, null));
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.green, null));
            });

            dialog.show();
        });



        // Home button
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Bottom Navigation buttons
        repairerProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerProfileActivity.class);
            startActivity(intent);
        });

        categoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, CategoryActivity.class); // Replace with actual activity
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, SettingsActivity.class); // Replace with actual activity
            startActivity(intent);
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(RepairerProfileActivity.this, ChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}