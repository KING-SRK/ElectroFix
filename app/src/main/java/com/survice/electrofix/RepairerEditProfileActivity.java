package com.survice.electrofix;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RepairerEditProfileActivity extends BaseActivity {

    private ImageView profileImage;
    private EditText etName, etEmail, etPhone, etSkills, etLocation, etPincode, etCharge, etExperience;
    private Switch switchAvailability;
    private Button btnSave;
    private FirebaseFirestore db;
    private DocumentReference profileRef;
    private String repairerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_edit_profile);

        repairerId = getIntent().getStringExtra("repairer_id");
        if (repairerId == null || repairerId.isEmpty()) {
            Log.e("EditProfile", "No Repairer ID found!");
            finish();
            return;
        }

        profileImage = findViewById(R.id.profileImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail); // ✅ Email EditText যুক্ত করা হলো
        etPhone = findViewById(R.id.etPhone);
        etSkills = findViewById(R.id.etSkills);
        etLocation = findViewById(R.id.etLocation);
        etPincode = findViewById(R.id.etPincode);
        etCharge = findViewById(R.id.etCharge);
        etExperience = findViewById(R.id.etExperience);
        switchAvailability = findViewById(R.id.switchAvailability);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        profileRef = db.collection("Repairers").document(repairerId);

        loadRepairerProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadRepairerProfile() {
        profileRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    etName.setText(snapshot.getString("name"));
                    etEmail.setText(snapshot.getString("email")); // ✅ Firestore থেকে Email লোড করা হচ্ছে
                    etPhone.setText(snapshot.getString("phone"));
                    etSkills.setText(snapshot.getString("skills"));
                    etLocation.setText(snapshot.getString("location"));
                    etPincode.setText(snapshot.getString("pinCode"));
                    etCharge.setText(snapshot.getString("charges"));
                    etExperience.setText(snapshot.getString("experience"));
                    switchAvailability.setChecked(Boolean.TRUE.equals(snapshot.getBoolean("availability")));

                    String profileImageUrl = snapshot.getString("profileImageUrl");
                    if (!TextUtils.isEmpty(profileImageUrl)) {
                        Glide.with(this).load(profileImageUrl)
                                .placeholder(R.drawable.repairer_profile_icon)
                                .into(profileImage);
                    }
                }
            } else {
                Log.e("Firestore Error", "Failed to load profile", task.getException());
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim(); // ✅ Email সেভ করার জন্য যুক্ত করা হলো
        String phone = etPhone.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String charge = etCharge.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        boolean availability = switchAvailability.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Name and Phone are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("email", email); // ✅ Firestore-এ Email আপডেট করা হচ্ছে
        updateData.put("phone", phone);
        updateData.put("skills", skills);
        updateData.put("location", location);
        updateData.put("pinCode", pincode);
        updateData.put("charges", charge);
        updateData.put("experience", experience);
        updateData.put("availability", availability);

        profileRef.update(updateData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                Log.e("Firestore Error", "Update failed", task.getException());
            }
        });
    }
}