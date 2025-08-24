package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CategoryActivity extends BaseActivity {

    private ImageButton homeButton, customerProfileButton, categoryButton, settingsButton, btnBack;

    private ImageButton btnAcRepair, btnComputerRepair, btnWashingMachine,
            btnLaptop, btnTv, btnMobilePhone, btnFridge, btnFan, btnWaterPurifier;
    private TextView customerProfileText;
    private FirebaseAuth mAuth;
    private ProgressBar loadingProgressBar;

    private DatabaseReference userDatabase;
    private String currentUserType = "";

    @SuppressLint("MissingInflatedId")
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
        setupButtonClickListeners();
        new Handler().postDelayed(this::checkCurrentUser, 200);

        customerProfileButton = findViewById(R.id.customer_profile_button);  // ✅ must match XML id
        checkCurrentUser();
        customerProfileText = findViewById(R.id.customer_profile_text
        );
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        homeButton = findViewById(R.id.home_button);
        customerProfileButton = findViewById(R.id.customer_profile_button);// নতুন single profile button
        categoryButton = findViewById(R.id.category_button);
        customerProfileText = findViewById(R.id.customer_profile_text);
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
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        if (customerProfileButton != null) customerProfileButton.setVisibility(View.GONE);
        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);
    }
    private void initBottomNavigation() {
        homeButton.setOnClickListener(v -> startNewActivity(MainActivity.class));

        categoryButton.setOnClickListener(v ->
                Toast.makeText(CategoryActivity.this, "You are already in Category", Toast.LENGTH_SHORT).show());

        settingsButton.setOnClickListener(v -> startNewActivity(SettingsActivity.class));
    }

    private void setupButtonClickListeners() {
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(CategoryActivity.this, MainActivity.class));
            // You might want to finish this activity if you don't want it in the back stack
            // or just let it reload when returning. For now, we'll let it reload.
        });
        // ✅ Profile (restricted) - Now correctly inside the method
        if (customerProfileButton != null) {
            customerProfileButton.setOnClickListener(v -> {
                if (isLoggedIn()) {
                    startActivity(new Intent(CategoryActivity.this, CustomerProfileActivity.class));
                } else {
                    showLoginPrompt("Please login to access your profile.");
                }
            });
        }
        // Add other button listeners here if this method is meant to manage them
        // For example:
        // if (homeButton != null) {
        //    homeButton.setOnClickListener(v -> startNewActivity(MainActivity.class));
        // }
    }
    private boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
    private void showLoginPrompt(String message) {
        new android.app.AlertDialog.Builder(CategoryActivity.this)
                .setTitle("Login Required")
                .setMessage(message)
                .setPositiveButton("Login / Signup", (dialog, which) -> {
                    startActivity(new Intent(CategoryActivity.this, SignupActivity.class));
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE); // Show loader while checking
        }

        if (currentUser == null) {
            // Not logged in → show button with login popup
            if (customerProfileButton != null) customerProfileButton.setVisibility(View.VISIBLE);
            if (customerProfileText != null) customerProfileText.setVisibility(View.VISIBLE);

            if (customerProfileButton != null) {
                customerProfileButton.setOnClickListener(v ->
                        showLoginPrompt("Please login to access your profile.")
                );
            }
            // Hide the loader as authentication status is known (not logged in)
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);

        } else {
            // User is logged in, now fetch their userType from the Realtime Database
            String uid = currentUser.getUid(); // Get the unique Firebase Auth User ID

            userDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Hide loader here, as database read is complete
                    if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);

                    if (snapshot.exists()) {
                        // Assuming UserModel has public fields or appropriate getters
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        if (userModel != null && "Customer".equals(userModel.userType)) {
                            // User is a customer, proceed normally
                            currentUserType = userModel.userType; // Update currentUserType variable
                            if (customerProfileButton != null) {
                                customerProfileButton.setVisibility(View.VISIBLE);
                                customerProfileText.setVisibility(View.VISIBLE);
                                customerProfileButton.setOnClickListener(v -> {
                                    Intent intent = new Intent(CategoryActivity.this, CustomerProfileActivity.class);
                                    startActivity(intent);
                                });
                            }
                            // Any other customer-specific UI adjustments or data loads can go here
                        } else {
                            // User type is not "Customer" or UserModel conversion failed
                            Toast.makeText(CategoryActivity.this,
                                    "This app is only for Customers. Please use Technician app or signup with a customer account.",
                                    Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut(); // Force sign out
                            startActivity(new Intent(CategoryActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        // User data (including userType) not found in Realtime Database for this UID
                        Toast.makeText(CategoryActivity.this, "User profile data not found! Please complete registration or contact support.", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut(); // Force sign out
                        startActivity(new Intent(CategoryActivity.this, LoginActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Hide loader on error
                    if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(CategoryActivity.this, "Database error checking user type: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    // Log.e("CategoryActivity", "Firebase Database Error: " + error.getMessage(), error.toException()); // Add proper TAG
                    FirebaseAuth.getInstance().signOut(); // Force sign out on error
                    startActivity(new Intent(CategoryActivity.this, LoginActivity.class));
                    finish();
                }
            });
        }
    }

    private void initCategoryButtons() {
        // ⭐ MODIFIED: This button will now open the new AcServicingActivity
        btnAcRepair.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, AcServicingActivity.class);
            startActivity(intent);
        });
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


    private void checkUserType(String userId) {
        userDatabase.child(userId).child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserType = snapshot.getValue(String.class);
                } else {
                    currentUserType = "";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
