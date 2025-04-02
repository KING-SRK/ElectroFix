package com.survice.electrofix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private Button orderHistoryButton, editProfileButton, logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 Full Screen Mode (Hide Status & Navigation Bar)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();

        setContentView(R.layout.activity_customer_profile);

        // 🔹 Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Customers").child(currentUser.getUid());

        // 🔹 UI Elements
        profileImage = findViewById(R.id.customerProfileImage);
        headerName = findViewById(R.id.customerHeaderName); // 🔹 "Hi, Name!" Header

        orderHistoryButton = findViewById(R.id.customerBookingHistory);
        editProfileButton = findViewById(R.id.customerEditProfile);
        logoutButton = findViewById(R.id.customerLogout);
        ImageButton homeButton = findViewById(R.id.home_button);

        // 🔹 Load User Info with Realtime Update
        loadUserInfo();

        // 🔹 Edit Profile Button Click -> CustomerEditProfileActivity-তে যাবে
        editProfileButton.setOnClickListener(v -> startActivity(new Intent(CustomerProfileActivity.this, CustomerProfileInfoActivity.class)));

        // 🔹 Logout Button Click -> ChoiceActivity-তে যাবে
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(CustomerProfileActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CustomerProfileActivity.this, ChoiceActivity.class));
            finish();
        });

        // 🔹 Home Button Click -> MainActivity-তে যাবে
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerProfileActivity.this, MainActivity.class));
            finish();
        });

        // 🔹 Profile Image Change Option
        profileImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, 100);
        });
    }

    // 🔹 Load User Info from Firebase with Real-time Update
    private void loadUserInfo() {
        if (currentUser != null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String imageUrl = snapshot.child("profileImage").getValue(String.class);

                        // 🔹 Update Header Text
                        headerName.setText("Hi, " + getValidText(name, "User") );

                        // 🔹 Load Profile Image
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(CustomerProfileActivity.this).load(imageUrl).into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.profile_image); // Default Image
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

    // 🔹 Handle Profile Image Selection
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

    // 🔹 Hide System UI for Full Screen Experience
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

    // 🔹 Restore UI Visibility when exiting full screen mode
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}