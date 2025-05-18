package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CategoryActivity extends BaseActivity {

    private ImageButton homeButton, customerProfileButton, repairerProfileButton, categoryButton, settingsButton;

    private ImageButton btnAcRepair, btnComputerRepair, btnWashingMachine,
            btnLaptop, btnTv, btnMobilePhone, btnFridge, btnFan, btnWaterPurifier;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String currentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen Mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_category);

        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        initViews();
        initBottomNavigation();
        initCategoryButtons();

        new Handler().postDelayed(this::checkCurrentUser, 200);
    }

    private void initViews() {
        homeButton = findViewById(R.id.home_button);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        repairerProfileButton = findViewById(R.id.repairer_profile_button);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);

        btnAcRepair = findViewById(R.id.btn_ac_repair);
        btnComputerRepair = findViewById(R.id.btn_computer_repair);
        btnWashingMachine = findViewById(R.id.btn_washing_machine);
        btnLaptop = findViewById(R.id.btn_laptop);
        btnTv = findViewById(R.id.btn_tv);
        btnMobilePhone = findViewById(R.id.btn_mobile_phone);
        btnFridge = findViewById(R.id.btn_fridge);
        btnFan = findViewById(R.id.btn_fan);
        btnWaterPurifier = findViewById(R.id.btn_water_purifier);

        // Hide both profile buttons initially
        customerProfileButton.setVisibility(View.GONE);
        repairerProfileButton.setVisibility(View.GONE);
    }

    private void initBottomNavigation() {
        homeButton.setOnClickListener(v -> startNewActivity(MainActivity.class));

        customerProfileButton.setOnClickListener(v -> {
            if ("Customer".equals(currentUserType)) {
                startNewActivity(CustomerProfileActivity.class);
            } else {
                Toast.makeText(this, "Access Denied! You are a Repairer.", Toast.LENGTH_SHORT).show();
            }
        });

        repairerProfileButton.setOnClickListener(v -> {
            if ("Repairer".equals(currentUserType)) {
                startNewActivity(RepairerProfileActivity.class);
            } else {
                Toast.makeText(this, "Access Denied! You are a Customer.", Toast.LENGTH_SHORT).show();
            }
        });

        categoryButton.setOnClickListener(v -> {
            // Already in Category
        });

        settingsButton.setOnClickListener(v -> startNewActivity(SettingsActivity.class));
    }

    private void initCategoryButtons() {
        btnAcRepair.setOnClickListener(v -> openServiceList("AC Repair"));
        btnComputerRepair.setOnClickListener(v -> openServiceList("Computer Repair"));
        btnWashingMachine.setOnClickListener(v -> openServiceList("Washing Machine Repair"));
        btnLaptop.setOnClickListener(v -> openServiceList("Laptop Repair"));
        btnTv.setOnClickListener(v -> openServiceList("TV Repair"));
        btnMobilePhone.setOnClickListener(v -> openServiceList("Mobile Phone Repair"));
        btnFridge.setOnClickListener(v -> openServiceList("Fridge Repair"));
        btnFan.setOnClickListener(v -> openServiceList("Fan Repair"));
        btnWaterPurifier.setOnClickListener(v -> openServiceList("Water Purifier Repair"));
    }

    private void openServiceList(String categoryName) {
        Intent intent = new Intent(CategoryActivity.this, ServiceListActivity.class);
        intent.putExtra("category", categoryName);
        startActivity(intent);
    }

    private void startNewActivity(Class<?> activityClass) {
        Intent intent = new Intent(CategoryActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserType(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserType(String userId) {
        userDatabase.child(userId).child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserType = snapshot.getValue(String.class);
                    updateProfileVisibility();
                } else {
                    Toast.makeText(CategoryActivity.this, "User type not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileVisibility() {
        if ("Customer".equalsIgnoreCase(currentUserType)) {
            customerProfileButton.setVisibility(View.VISIBLE);
            repairerProfileButton.setVisibility(View.GONE);
        } else if ("Repairer".equalsIgnoreCase(currentUserType)) {
            customerProfileButton.setVisibility(View.GONE);
            repairerProfileButton.setVisibility(View.VISIBLE);
        } else {
            customerProfileButton.setVisibility(View.GONE);
            repairerProfileButton.setVisibility(View.GONE);
        }
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
