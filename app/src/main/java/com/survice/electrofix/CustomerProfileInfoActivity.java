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
        userRef = FirebaseDatabase.getInstance().getReference("Customers").child(currentUser.getUid());

        // UI Elements
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack);
        tvFullName = findViewById(R.id.Fullname); // XML এ @id/Fullname
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
        });

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                int filledFields = 0;
                int totalFields = 7; // name, email, phone, address, pincode, dob, gender

                if (setTextIfAvailable(tvFullName, snapshot.child("name").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvEmail, snapshot.child("email").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvPhone, snapshot.child("phone").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvAddress, snapshot.child("address").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvPinCode, snapshot.child("pinCode").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvDOB, snapshot.child("dob").getValue(String.class))) filledFields++;
                if (setTextIfAvailable(tvGender, snapshot.child("gender").getValue(String.class))) filledFields++;

                // Profile Image Load
                String imageUrl = snapshot.child("profileImage").getValue(String.class);
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    Glide.with(CustomerProfileInfoActivity.this).load(imageUrl).into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.ic_default_avatar);
                }

                // Calculate percentage
                int percent = (filledFields == 0) ? 0 : (int) ((filledFields / (float) totalFields) * 100);
                animateProgress(percent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerProfileInfoActivity.this, "Failed to load profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean setTextIfAvailable(TextView textView, String value) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(value);
            return true;
        } else {
            textView.setText("Not set");
            return false;
        }
    }

    private void animateProgress(int target) {
        profileProgressBar.setProgress(0);
        tvProgressText.setText("0%");
        Handler handler = new Handler();
        new Thread(() -> {
            int progress = 0;
            while (progress <= target) {
                int finalProgress = progress;
                handler.post(() -> {
                    profileProgressBar.setProgress(finalProgress);
                    tvProgressText.setText(finalProgress + "%");
                });
                try {
                    Thread.sleep(20); // smooth animation
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progress++;
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
