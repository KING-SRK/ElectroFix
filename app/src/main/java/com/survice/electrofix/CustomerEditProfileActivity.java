package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CustomerEditProfileActivity extends BaseActivity {

    private ImageView imgProfile;
    private EditText etName, etEmail, etPhone, etAddress, etPinCode, etDOB;
    private RadioGroup radioGender;
    private RadioButton radioMale, radioFemale, radioOther;
    private Button btnChangeProfilePic, btnSaveProfile, btnChangePassword;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private Uri imageUri;
    private String gender = "Male";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_edit_profile);

        // 🔹 Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Customers").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages").child(currentUser.getUid());

        // 🔹 UI Elements
        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPinCode = findViewById(R.id.etPinCode);
        etDOB = findViewById(R.id.etDOB);
        radioGender = findViewById(R.id.radioGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioOther = findViewById(R.id.radioOther);
        btnChangeProfilePic = findViewById(R.id.btnChangeProfilePic);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // 🔹 Back Button
        btnBack.setOnClickListener(v -> finish());

        // 🔹 Load User Data
        loadUserProfile();

        // 🔹 Change Profile Picture
        btnChangeProfilePic.setOnClickListener(v -> selectImage());

        // 🔹 Date Picker for DOB
        etDOB.setOnClickListener(v -> showDatePicker());

        // 🔹 Gender Selection with Color Change
        radioGender.setOnCheckedChangeListener((group, checkedId) -> {
            resetRadioColors(); // Reset all colors before changing

            if (checkedId == R.id.radioMale) {
                gender = "Male";
                radioMale.setTextColor(Color.BLUE);
                radioMale.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.BLUE));
            } else if (checkedId == R.id.radioFemale) {
                gender = "Female";
                radioFemale.setTextColor(Color.MAGENTA);
                radioFemale.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.MAGENTA));
            } else {
                gender = "Other";
                radioOther.setTextColor(Color.parseColor("#FFC107"));
                radioOther.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFC107")));
            }
        });

        // 🔹 Save Profile Data
        btnSaveProfile.setOnClickListener(v -> saveProfileData());

        // 🔹 Change Password
        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(CustomerEditProfileActivity.this, CustomerChangePasswordActivity.class)));
    }

    // 🔹 Load User Data from Firebase
    private void loadUserProfile() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                etName.setText(snapshot.child("name").getValue(String.class));
                etEmail.setText(snapshot.child("email").getValue(String.class));
                etPhone.setText(snapshot.child("phone").getValue(String.class));
                etAddress.setText(snapshot.child("address").getValue(String.class));
                etDOB.setText(snapshot.child("dob").getValue(String.class));
                gender = snapshot.child("gender").getValue(String.class);

                if ("Female".equals(gender)) {
                    radioGender.check(R.id.radioFemale);
                } else if ("Other".equals(gender)) {
                    radioGender.check(R.id.radioOther);
                } else {
                    radioGender.check(R.id.radioMale);
                }

                // 🔹 Load Profile Image
                String imageUrl = snapshot.child("profileImage").getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).into(imgProfile);
                }
            }
        });
    }

    // 🔹 Save Updated Profile Data
    private void saveProfileData() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("email", email);
        updateData.put("phone", phone);
        updateData.put("address", address);
        updateData.put("dob", dob);
        updateData.put("gender", gender);

        userRef.updateChildren(updateData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (imageUri != null) {
                    uploadProfileImage();
                } else {
                    Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to Update Profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Upload Profile Image to Firebase Storage
    private void uploadProfileImage() {
        storageRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    userRef.child("profileImage").setValue(uri.toString());
                    Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                Toast.makeText(this, "Failed to Upload Image!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Reset Radio Button Colors
    private void resetRadioColors() {
        radioMale.setTextColor(Color.BLACK);
        radioFemale.setTextColor(Color.BLACK);
        radioOther.setTextColor(Color.BLACK);

        radioMale.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
        radioFemale.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
        radioOther.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
    }

    // 🔹 Select Profile Image
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }

    // 🔹 Date Picker Dialog for DOB
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etDOB.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
