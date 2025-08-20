package com.survice.electrofix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerProfileActivity extends BaseActivity {

    private ImageView profileImage;
    private TextView headerName;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    // New: LinearLayouts for buttons
    private LinearLayout profileInfoBtn, bookingHistoryBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full Screen Mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();

        setContentView(R.layout.activity_customer_profile);

        // Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Customers").child(currentUser.getUid());

        // UI Elements
        profileImage = findViewById(R.id.customerProfileImage);
        headerName = findViewById(R.id.customerHeaderName);

        // New: Find LinearLayouts
        profileInfoBtn = findViewById(R.id.btnProfileInfo);
        bookingHistoryBtn = findViewById(R.id.btnBookingHistory);
        logoutBtn = findViewById(R.id.btnLogout);

        // Bottom Navigation Buttons
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton profileButton = findViewById(R.id.customer_profile_button);
        ImageButton categoryButton = findViewById(R.id.category_button);
        ImageButton settingsButton = findViewById(R.id.settings_button);

        // Load User Info
        loadUserInfo();

        // Profile Info Button
        profileInfoBtn.setOnClickListener(v ->
                startActivity(new Intent(CustomerProfileActivity.this, CustomerProfileInfoActivity.class))
        );

        // Booking History Button
        bookingHistoryBtn.setOnClickListener(v ->
                startActivity(new Intent(CustomerProfileActivity.this, CustomerBookingHistoryActivity.class))
        );

        // Logout Button
        logoutBtn.setOnClickListener(v -> {
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(CustomerProfileActivity.this)
                    .setTitle("⚠ Logout")
                    .setMessage("Are you sure you want to log out from ElectroFix?")
                    .setIcon(R.drawable.ic_logout_warning)
                    .setPositiveButton("Yes, Logout", (dialogInterface, i) -> {
                        mAuth.signOut();
                        Toast.makeText(CustomerProfileActivity.this, "Logged Out Conformed", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CustomerProfileActivity.this, MainActivity.class));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dlg -> {
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(R.color.red, null));
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.green, null));
            });

            dialog.show();
        });

        // Profile Image Click (Pick New Image)
        profileImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, 100);
        });

        // Bottom Navigation Clicks
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerProfileActivity.this, MainActivity.class));
            finish();
        });

        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerProfileActivity.this, CustomerProfileActivity.class));
            finish();
        });

        categoryButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerProfileActivity.this, CategoryActivity.class));
            finish();
        });

        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerProfileActivity.this, SettingsActivity.class));
            finish();
        });
    }

    // Load User Info from Firebase
    private void loadUserInfo() {
        if (currentUser != null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String imageUrl = snapshot.child("profileImage").getValue(String.class);

                        headerName.setText("Hi, " + getValidText(name, "User"));

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(CustomerProfileActivity.this).load(imageUrl).into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.ic_profile);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(CustomerProfileActivity.this, "Failed to load data!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            profileImage.setImageURI(selectedImage);
        }
    }

    private String getValidText(String text, String defaultText) {
        return (text != null && !text.isEmpty()) ? text : defaultText;
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}