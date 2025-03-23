package com.survice.electrofix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class RepairerEditProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgProfilePicture;
    private Button btnChangePicture, btnSaveProfile;
    private EditText editRepairerName, editRepairerPhone, editRepairerSkills,
            etAddress, etPinCode, editRepairerCharges, editRepairerExperience;
    private Switch switchAvailability;
    private TextView txtAvailabilityStatus;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DocumentReference profileRef;

    private Uri imageUri;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        user = auth.getCurrentUser();
        storageReference = storage.getReference("profile_images");

        if (user != null) {
            profileRef = db.collection("repairers").document(user.getUid());
        } else {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack = findViewById(R.id.btnBack);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        btnChangePicture = findViewById(R.id.btnChangePicture);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        editRepairerName = findViewById(R.id.editRepairerName);
        editRepairerPhone = findViewById(R.id.editRepairerPhone);
        editRepairerSkills = findViewById(R.id.editRepairerSkills);
        etAddress = findViewById(R.id.etAddress);
        etPinCode = findViewById(R.id.etPinCode);
        editRepairerCharges = findViewById(R.id.editRepairerCharges);
        editRepairerExperience = findViewById(R.id.editRepairerExperience);
        switchAvailability = findViewById(R.id.switchAvailability);
        txtAvailabilityStatus = findViewById(R.id.txtAvailabilityStatus);
        progressBar = findViewById(R.id.progressBar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Profile...");

        btnBack.setOnClickListener(v -> finish());
        btnChangePicture.setOnClickListener(v -> openGallery());
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) ->
                txtAvailabilityStatus.setText(isChecked ? "Online" : "Offline"));

        loadUserProfile();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgProfilePicture.setImageURI(imageUri);
        }
    }

    private void loadUserProfile() {
        if (profileRef == null) return;

        progressBar.setVisibility(View.VISIBLE);

        profileRef.get().addOnSuccessListener(documentSnapshot -> {
            progressBar.setVisibility(View.GONE);

            if (documentSnapshot.exists()) {
                editRepairerName.setText(documentSnapshot.getString("name"));
                editRepairerPhone.setText(documentSnapshot.getString("phone"));
                editRepairerSkills.setText(documentSnapshot.getString("skills"));
                etAddress.setText(documentSnapshot.getString("address"));
                etPinCode.setText(documentSnapshot.getString("pinCode"));
                editRepairerCharges.setText(documentSnapshot.getString("charges"));
                editRepairerExperience.setText(documentSnapshot.getString("experience"));

                boolean isAvailable = documentSnapshot.getBoolean("availability") != null && documentSnapshot.getBoolean("availability");
                switchAvailability.setChecked(isAvailable);
                txtAvailabilityStatus.setText(isAvailable ? "Online" : "Offline");

                loadProfilePicture();
            }
        }).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void loadProfilePicture() {
        StorageReference fileReference = storageReference.child(user.getUid() + ".jpg");
        fileReference.getDownloadUrl().addOnSuccessListener(uri ->
                Glide.with(this).load(uri).into(imgProfilePicture)
        ).addOnFailureListener(e ->
                Log.e("ProfilePicture", "Failed to load profile image", e)
        );
    }

    private void saveProfile() {
        if (profileRef == null) {
            Toast.makeText(this, "User profile not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", editRepairerName.getText().toString().trim());
        userData.put("phone", editRepairerPhone.getText().toString().trim());
        userData.put("skills", editRepairerSkills.getText().toString().trim());
        userData.put("address", etAddress.getText().toString().trim());
        userData.put("pinCode", etPinCode.getText().toString().trim());
        userData.put("charges", editRepairerCharges.getText().toString().trim());
        userData.put("experience", editRepairerExperience.getText().toString().trim());
        userData.put("availability", switchAvailability.isChecked());

        profileRef.set(userData).addOnSuccessListener(aVoid -> {
            if (imageUri != null) {
                uploadProfileImage();
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                navigateToProfileInfo();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadProfileImage() {
        StorageReference fileReference = storageReference.child(user.getUid() + ".jpg");

        fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                profileRef.update("profilePicture", uri.toString()).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    navigateToProfileInfo();
                });
            });
        }).addOnFailureListener(e -> progressDialog.dismiss());
    }

    private void navigateToProfileInfo() {
        Intent intent = new Intent(RepairerEditProfileActivity.this, RepairerProfileInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}