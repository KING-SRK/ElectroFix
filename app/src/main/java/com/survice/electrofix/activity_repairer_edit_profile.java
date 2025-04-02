package com.survice.electrofix;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class activity_repairer_edit_profile extends AppCompatActivity {

    private ImageView profileImage;
    private EditText etName, etEmail, etPhone, etSkills, etLocation, etPincode, etCharge, etExperience;
    private Switch switchAvailability;
    private Button btnSave;

    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_edit_profile);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize UI Components
        profileImage = findViewById(R.id.profileImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSkills = findViewById(R.id.etSkills);
        etLocation = findViewById(R.id.etLocation);
        etPincode = findViewById(R.id.etPincode);
        etCharge = findViewById(R.id.etCharge);
        etExperience = findViewById(R.id.etExperience);
        switchAvailability = findViewById(R.id.switchAvailability);
        btnSave = findViewById(R.id.btnSave);

        // Load Profile Data
        loadProfileData();

        // Save Profile Data to Firestore
        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void loadProfileData() {
        firestore.collection("repairers").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetching data from Firestore
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String skills = documentSnapshot.getString("skills");
                        String location = documentSnapshot.getString("location");
                        String pincode = documentSnapshot.getString("pincode");
                        String charge = documentSnapshot.getString("charge");
                        String experience = documentSnapshot.getString("experience");
                        boolean isAvailable = documentSnapshot.getBoolean("available");
                        String profilePicUrl = documentSnapshot.getString("profilePicUrl");

                        // Setting data to UI
                        etName.setText(name);
                        etEmail.setText(email);
                        etPhone.setText(phone);
                        etSkills.setText(skills);
                        etLocation.setText(location);
                        etPincode.setText(pincode);
                        etCharge.setText(charge);
                        etExperience.setText(experience);
                        switchAvailability.setChecked(isAvailable);

                        // Load Profile Picture with Glide
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(activity_repairer_edit_profile.this)
                                    .load(profilePicUrl)
                                    .into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(activity_repairer_edit_profile.this, "Failed to load profile!", Toast.LENGTH_SHORT).show());
    }

    private void updateProfile() {
        // Get updated data from input fields
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String charge = etCharge.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        boolean isAvailable = switchAvailability.isChecked();

        // Validate required fields
        if (name.isEmpty() || phone.isEmpty() || skills.isEmpty() || location.isEmpty() || charge.isEmpty() || experience.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create HashMap to update data
        Map<String, Object> profileUpdates = new HashMap<>();
        profileUpdates.put("name", name);
        profileUpdates.put("phone", phone);
        profileUpdates.put("skills", skills);
        profileUpdates.put("location", location);
        profileUpdates.put("pincode", pincode);
        profileUpdates.put("charge", charge);
        profileUpdates.put("experience", experience);
        profileUpdates.put("available", isAvailable);

        // Update Firestore document
        firestore.collection("repairers").document(userId)
                .update(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(activity_repairer_edit_profile.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Redirect to Profile Page after update
                })
                .addOnFailureListener(e -> Toast.makeText(activity_repairer_edit_profile.this, "Update Failed!", Toast.LENGTH_SHORT).show());
    }
}