package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerProfileInfoActivity extends BaseActivity {

    private ImageView imgProfile, btnBack;
    private TextView tvFullName, tvEmail, tvPhone, tvAddress, tvPinCode, tvDOB, tvGender, tvProgressText;
    private Button btnEditProfile;
    private DatabaseReference userRef;
    private ProgressBar profileProgressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profileinfo);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Ensure this path is correct, it was "Users" in CustomerEditProfileActivity
        // If it's "Users" in CustomerEditProfileActivity, it should be "Users" here too.
        // Assuming "Users" as per previous discussion. If it's "Customers", keep it.
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // UI Elements
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);
        tvFullName = findViewById(R.id.Fullname);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvPinCode = findViewById(R.id.tvPinCode);
        tvDOB = findViewById(R.id.tvDOB);
        tvGender = findViewById(R.id.tvGender);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        profileProgressBar = findViewById(R.id.profileProgressBar);
        tvProgressText = findViewById(R.id.tvProgressText);

        loadUserProfile();

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(CustomerProfileInfoActivity.this, CustomerEditProfileActivity.class));
            // You might want to finish this activity if you don't want it in the back stack
            // or just let it reload when returning. For now, we'll let it reload.
        });

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Optionally, set default empty states if profile doesn't exist
                    tvFullName.setText("N/A");
                    tvEmail.setText("N/A");
                    tvPhone.setText("N/A");
                    tvAddress.setText("N/A");
                    tvPinCode.setText("N/A");
                    tvDOB.setText("N/A");
                    tvGender.setText("N/A");
                    imgProfile.setImageResource(R.drawable.ic_default_avatar);
                    animateProgress(0);
                    return;
                }

                int filledFields = 0;
                // Count fields from your CustomerEditProfileActivity that are saved
                int totalFields = 7; // name, email, phone, address, pinCode, dob, gender

                // Use the helper to set text and count filled fields
                if (setTextIfAvailable(tvFullName, snapshot.child("name").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvEmail, snapshot.child("email").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvPhone, snapshot.child("phone").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvAddress, snapshot.child("address").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvPinCode, snapshot.child("pinCode").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvDOB, snapshot.child("dob").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvGender, snapshot.child("gender").getValue(String.class))) filledFields++;


                // ⭐ Profile Image Load - Check for drawable ID first, then URL
                Integer profileDrawableId = snapshot.child("profileImageDrawableId").getValue(Integer.class);
                String imageUrl = snapshot.child("profileImage").getValue(String.class);

                if (profileDrawableId != null && profileDrawableId != 0) {
                    // Load default avatar if ID is stored
                    imgProfile.setImageResource(profileDrawableId);
                } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    // Otherwise, load from URL using Glide
                    Glide.with(CustomerProfileInfoActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar) // Fallback if Glide fails
                            .into(imgProfile);
                } else {
                    // Default if neither is found or invalid
                    imgProfile.setImageResource(R.drawable.ic_default_avatar);
                }

                // Calculate percentage
                // Add 1 to totalFields if profile image counts towards completion
                if (profileDrawableId != null && profileDrawableId != 0 || (imageUrl != null && !imageUrl.trim().isEmpty())) {
                    // Consider the profile image as a field if it's set
                    // You might adjust this logic based on how strictly you define "profile completion"
                    // If you want to count it, increment filledFields and totalFields.
                    // For now, let's stick to the 7 main text fields + 1 for image if filled.
                    // totalFields = 8 if image also counts.
                    // If you want profile image to be part of the 7 (e.g., instead of gender), adjust totalFields.
                    // For simplicity, let's assume it's separate for now in terms of progress.
                }

                int percent = (filledFields == 0) ? 0 : (int) ((filledFields / (float) totalFields) * 100);
                animateProgress(percent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerProfileInfoActivity.this, "Failed to load profile! " + error.getMessage(), Toast.LENGTH_SHORT).show();
                // Consider setting default UI state on error too
                imgProfile.setImageResource(R.drawable.ic_default_avatar);
                tvFullName.setText("Error loading...");
                // ... set other fields to error state
                animateProgress(0);
            }
        });
    }

    private boolean setTextIfAvailable(TextView textView, String value) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(value);
            return true;
        } else {
            textView.setText("Not set"); // Or "N/A", adjust as desired
            return false;
        }
    }

    private void animateProgress(int target) {
        // Only reset to 0 if the new target is less than current or initial setup
        if (profileProgressBar.getProgress() > target || profileProgressBar.getProgress() == 0) {
            profileProgressBar.setProgress(0);
            tvProgressText.setText("0%");
        }

        Handler handler = new Handler(getMainLooper()); // Use getMainLooper() for UI updates
        new Thread(() -> {
            int currentProgress = profileProgressBar.getProgress();
            while (currentProgress <= target) {
                int finalProgress = currentProgress;
                handler.post(() -> {
                    profileProgressBar.setProgress(finalProgress);
                    tvProgressText.setText(finalProgress + "%");
                });
                try {
                    Thread.sleep(20); // smooth animation
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Best practice for InterruptedException
                    e.printStackTrace();
                }
                currentProgress++;
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}