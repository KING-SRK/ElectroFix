package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerProfileInfoActivity extends AppCompatActivity {

    private ImageView imgProfile, btnBack;
    private TextView tvName, tvEmail, tvPhone, tvAddress, tvPinCode, tvDOB, tvGender;
    private Button btnEditProfile;
    private DatabaseReference userRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profileinfo);

        // 🔹 Firebase Reference
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Customers").child(currentUser.getUid());

        // 🔹 UI Elements
        imgProfile = findViewById(R.id.imgProfile);
        btnBack = findViewById(R.id.btnBack); // 🔙 Back Button
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvPinCode = findViewById(R.id.tvPinCode);
        tvDOB = findViewById(R.id.tvDOB);
        tvGender = findViewById(R.id.tvGender);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // 🔹 Load User Profile (Live Data Update)
        loadUserProfile();

        // 🔹 Edit Profile Button Click
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerProfileInfoActivity.this, CustomerEditProfileActivity.class);
            startActivity(intent);
        });

        // 🔙 Back Button Click
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // 🔹 Firebase থেকে লাইভ প্রোফাইল ডাটা লোড করার মেথড
    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvName.setText(snapshot.child("name").getValue(String.class));
                    tvEmail.setText(snapshot.child("email").getValue(String.class));
                    tvPhone.setText(snapshot.child("phone").getValue(String.class));
                    tvAddress.setText(snapshot.child("address").getValue(String.class));
                    tvDOB.setText(snapshot.child("dob").getValue(String.class));
                    tvGender.setText(snapshot.child("gender").getValue(String.class));

                    // 🔹 Profile Image Load
                    String imageUrl = snapshot.child("profileImage").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(CustomerProfileInfoActivity.this).load(imageUrl).into(imgProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerProfileInfoActivity.this, "Failed to load profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔙 ফোনের Back Button প্রেস করলে Activity বন্ধ হবে
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}