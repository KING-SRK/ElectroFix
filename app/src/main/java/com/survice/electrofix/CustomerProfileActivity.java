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
    private TextView tvUserId;
    private TextView tvAddress;

    // LinearLayouts for buttons
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

        if (currentUser == null) {
            // If user is not logged in, redirect to login/main activity
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)); // or MainActivity.class
            finish();
            return;
        }

        // ⭐ Important: Ensure this path matches where user data is saved (e.g., "Users" or "Customers")
        // Based on previous discussions, it should likely be "Users"
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // UI Elements
        profileImage = findViewById(R.id.imgProfile);
        headerName = findViewById(R.id.customerHeaderName);
        // ⭐ NEW: Bind the new TextViews for User ID and Address in CustomerProfileActivity
        // Ensure these IDs exist in your activity_customer_profile.xml
        tvUserId = findViewById(R.id.tvUserId); // Assuming this is the ID for User ID
        tvAddress = findViewById(R.id.tvAddress); // Assuming this is the ID for Address

        // Find LinearLayouts
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
                        finishAffinity(); // Clears all activities from the task stack
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

        // ⭐ Removed direct profileImage.setOnClickListener for gallery.
        // The flow is now: CustomerProfileActivity -> CustomerProfileInfoActivity -> CustomerEditProfileActivity -> AvatarChooserActivity
        // If a user wants to change their profile picture, they should do it through the "Edit Profile" flow.
        // This avoids confusion and keeps the logic centralized.

        // Bottom Navigation Clicks
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerProfileActivity.this, MainActivity.class));
            finish();
        });

        profileButton.setOnClickListener(v -> {
            // Already on this page, or reload if needed.
            // No need to restart the same activity unless there's a specific reason.
            // finish(); // Remove if you want to keep the current instance.
            // If you always want a fresh instance, you can use:
            // startActivity(new Intent(CustomerProfileActivity.this, CustomerProfileActivity.class));
            // finish();
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
        // currentUser check is already done in onCreate, but good practice to double-check
        if (currentUser != null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("name").getValue(String.class); // Get full name
                        // ⭐ Get both image URL and drawable ID
                        String imageUrl = snapshot.child("profileImage").getValue(String.class);
                        Integer profileDrawableId = snapshot.child("profileImageDrawableId").getValue(Integer.class);
                        // ⭐ MODIFIED: Fetch the custom user ID using the correct field name 'userId'
                        String customUserId = snapshot.child("userId").getValue(String.class);
                        // ⭐ Get the Address from Firebase
                        String address = snapshot.child("address").getValue(String.class);

                        // ⭐ Get the User ID from Firebase Authentication (not from database data)
                        String userId = currentUser.getUid();

                        // ... existing first name parsing logic for headerName ...

                        // ⭐ Set the User ID and Address
                        if (tvUserId != null) {
                            tvUserId.setText(getValidText(customUserId, "Not set"));
                        }
                        if (tvAddress != null) { // Null check for safety
                            tvAddress.setText(getValidText(address, "Not set"));
                        }

                        // Extract first name logic (already implemented in previous response)
                        String firstName = "User";
                        if (fullName != null && !fullName.isEmpty()) {
                            String[] nameParts = fullName.split(" ");
                            if (nameParts.length > 0) {
                                firstName = nameParts[0];
                            } else {
                                firstName = fullName;
                            }
                        }
                        headerName.setText("Hi, " + firstName);

                        // ⭐ Load profile image: check drawable ID first, then URL
                        if (profileDrawableId != null && profileDrawableId != 0) {
                            // If a default avatar ID is stored, use it
                            profileImage.setImageResource(profileDrawableId);
                        } else if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Otherwise, if a URL is stored, use Glide
                            Glide.with(CustomerProfileActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile) // Use your preferred placeholder
                                    .error(R.drawable.ic_profile) // Fallback if Glide fails
                                    .into(profileImage);
                        } else {
                            // If neither is set, use a generic default
                            profileImage.setImageResource(R.drawable.ic_profile);
                        }
                    } else {
                        // Handle case where user data does not exist (e.g., brand new user)
                        headerName.setText("Hi, User");
                        profileImage.setImageResource(R.drawable.ic_profile);
                        if (tvUserId != null) tvUserId.setText("Not set");
                        if (tvAddress != null) tvAddress.setText("Not set");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(CustomerProfileActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    headerName.setText("Hi, User"); // Fallback on error
                    profileImage.setImageResource(R.drawable.ic_profile);
                    if (tvUserId != null) tvUserId.setText("Error loading...");
                    if (tvAddress != null) tvAddress.setText("Error loading...");// Fallback on error
                }
            });
        }
    }

    // ⭐ Removed onActivityResult since profile image picking is now handled by AvatarChooserActivity
    // and CustomerEditProfileActivity.
    // If you explicitly need to handle a result here for another purpose, re-add it.
    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //     super.onActivityResult(requestCode, resultCode, data);
    //     // ... your existing onActivityResult code if needed for other intents
    // }

    // Ensure you have this helper method:
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