package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class CategoryActivity extends BaseActivity {

    private ImageButton homeButton, customerProfileButton, repairerProfileButton, categoryButton, settingsButton;
    private ImageButton btnAcRepair, btnComputerRepair, btnWashingMachine;
    private LinearLayout customerLayout, repairerLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen Mode (Title bar + Status + Nav bar hidden)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_category);

        // Bottom Navigation Buttons
        homeButton = findViewById(R.id.home_button);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        repairerProfileButton = findViewById(R.id.repairer_profile_button);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);

        // Category Buttons
        btnAcRepair = findViewById(R.id.btn_ac_repair);
        btnComputerRepair = findViewById(R.id.btn_computer_repair);
        btnWashingMachine = findViewById(R.id.btn_washing_machine);

        // Profile Layouts
        customerLayout = findViewById(R.id.customer_profile_layout);
        repairerLayout = findViewById(R.id.repairer_profile_layout);

        // Delay to update layout visibility
        new Handler().postDelayed(this::updateProfileVisibility, 200);

        // Bottom Navigation Click Listeners
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        customerProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CustomerProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        repairerProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, RepairerProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        categoryButton.setOnClickListener(v -> {
            // Do nothing - already in this activity
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Category Button Clicks
        btnAcRepair.setOnClickListener(v -> openServiceList("AC Repair"));
        btnComputerRepair.setOnClickListener(v -> openServiceList("Computer Repair"));
        btnWashingMachine.setOnClickListener(v -> openServiceList("Washing Machine Repair"));
    }

    // Navigate to service list based on category
    private void openServiceList(String categoryName) {
        Intent intent = new Intent(CategoryActivity.this, ServiceListActivity.class);
        intent.putExtra("category", categoryName);
        startActivity(intent);
    }

    // Show/hide profile layout based on user type
    private void updateProfileVisibility() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userType = preferences.getString("user_type", "");

        Log.d("CategoryActivity", "User type: " + userType);

        if (userType.equals("customer")) {
            customerLayout.setVisibility(View.VISIBLE);
            repairerLayout.setVisibility(View.GONE);
        } else if (userType.equals("repairer")) {
            customerLayout.setVisibility(View.GONE);
            repairerLayout.setVisibility(View.VISIBLE);
        } else {
            customerLayout.setVisibility(View.GONE);
            repairerLayout.setVisibility(View.GONE);
        }
    }

    // Hide system UI for fullscreen mode
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}