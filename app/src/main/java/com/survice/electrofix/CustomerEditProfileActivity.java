package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

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
    private String selectedAvatarName = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            imgProfile.setImageURI(imageUri);
                            selectedAvatarName = null;
                        }
                    });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("Customers").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages").child(currentUser.getUid());

        imgProfile = findViewById(R.id.imgAvatar);
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

        // These must exist in XML, otherwise check IDs
        btnChangeProfilePic = findViewById(R.id.btnChangeProfilePic);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadUserProfile();

        if (btnChangeProfilePic != null) {
            btnChangeProfilePic.setOnClickListener(v -> showImageOptionDialog());
        }
        etDOB.setOnClickListener(v -> showDatePicker());

        radioGender.setOnCheckedChangeListener((group, checkedId) -> {
            resetRadioColors();
            if (checkedId == R.id.radioMale) {
                gender = "Male";
                radioMale.setTextColor(Color.BLUE);
                radioMale.setButtonTintList(ColorStateList.valueOf(Color.BLUE));
            } else if (checkedId == R.id.radioFemale) {
                gender = "Female";
                radioFemale.setTextColor(Color.MAGENTA);
                radioFemale.setButtonTintList(ColorStateList.valueOf(Color.MAGENTA));
            } else {
                gender = "Other";
                radioOther.setTextColor(Color.parseColor("#FFC107"));
                radioOther.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
            }
        });

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveProfileData());
        }
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v ->
                    startActivity(new Intent(this, CustomerChangePasswordActivity.class))
            );
        }
    }


    private void showImageOptionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choose_avatar);

        int[] avatarIds = {
                R.id.avatar1, R.id.avatar2, R.id.avatar3, R.id.avatar4,
                R.id.avatar5, R.id.avatar6, R.id.avatar7, R.id.avatar8
        };
        int[] avatarRes = {
                R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4,
                R.drawable.avatar5, R.drawable.avatar6, R.drawable.avatar7, R.drawable.avatar8
        };

        // Avatar click listener
        View.OnClickListener avatarClickListener = v -> {
            for (int i = 0; i < avatarIds.length; i++) {
                if (v.getId() == avatarIds[i]) {
                    imgProfile.setImageResource(avatarRes[i]);
                    selectedAvatarName = "avatar" + (i + 1);
                    imageUri = null;
                    dialog.dismiss();
                    break;
                }
            }
        };

        for (int avatarId : avatarIds) {
            dialog.findViewById(avatarId).setOnClickListener(avatarClickListener);
        }

        // Gallery option without button
        ImageView imgGallery = dialog.findViewById(R.id.imgGalleryIcon);
        if (imgGallery != null) {
            imgGallery.setOnClickListener(v -> {
                selectImage();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void loadUserProfile() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                etName.setText(snapshot.child("name").getValue(String.class));
                etEmail.setText(snapshot.child("email").getValue(String.class));
                etPhone.setText(snapshot.child("phone").getValue(String.class));
                etAddress.setText(snapshot.child("address").getValue(String.class));
                etPinCode.setText(snapshot.child("pinCode").getValue(String.class));
                etDOB.setText(snapshot.child("dob").getValue(String.class));
                gender = snapshot.child("gender").getValue(String.class);

                if ("Female".equals(gender)) radioGender.check(R.id.radioFemale);
                else if ("Other".equals(gender)) radioGender.check(R.id.radioOther);
                else radioGender.check(R.id.radioMale);

                String imageVal = snapshot.child("profileImage").getValue(String.class);
                if (imageVal != null && !imageVal.isEmpty()) {
                    if (imageVal.startsWith("avatar")) {
                        int resId = getResources().getIdentifier(imageVal, "drawable", getPackageName());
                        if (resId != 0) imgProfile.setImageResource(resId);
                    } else {
                        Glide.with(this).load(imageVal).into(imgProfile);
                    }
                }
            }
        });
    }

    private void saveProfileData() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pinCode = etPinCode.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || pinCode.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updateEmail(email);
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("email", email);
        updateData.put("phone", phone);
        updateData.put("address", address);
        updateData.put("pinCode", pinCode);
        updateData.put("dob", dob);
        updateData.put("gender", gender);

        // Save avatar selection
        if (selectedAvatarName != null) {
            updateData.put("profileImage", selectedAvatarName);
        }

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

    private void resetRadioColors() {
        radioMale.setTextColor(Color.BLACK);
        radioFemale.setTextColor(Color.BLACK);
        radioOther.setTextColor(Color.BLACK);
        ColorStateList gray = ColorStateList.valueOf(Color.GRAY);
        radioMale.setButtonTintList(gray);
        radioFemale.setButtonTintList(gray);
        radioOther.setButtonTintList(gray);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

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
