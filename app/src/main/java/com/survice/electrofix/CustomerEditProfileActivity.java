package com.survice.electrofix;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat; // Import for ContextCompat

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerEditProfileActivity extends AppCompatActivity {

    // Views
    private ImageButton btnBack;
    private TextView PaymentPage; // title
    private ImageView imgGalleryIcon;
    private Button btnChangeProfilePic;
    private EditText etName, etPinCode, etDOB, etEmail, etPhone, etAddress;
    private RadioGroup radioGender;
    private RadioButton radioMale, radioFemale, radioOther;
    private Switch switchEdit;
    private MaterialButton btnUseMyLocation;
    private Button btnSaveProfile;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    // Launchers
    // Removed galleryPicker as AvatarChooserActivity handles gallery picking
    private ActivityResultLauncher<String[]> locationPermissionLauncher; // Renamed for clarity
    private ActivityResultLauncher<String> readMediaImagesPermissionLauncher; // for Android 13+ images (if needed directly in this activity)
    private ActivityResultLauncher<Intent> avatarChooserLauncher; // For AvatarChooserActivity

    // State
    private Uri selectedImageUri = null; // Holds URI for gallery selected images to be uploaded

    // Permissions arrays
    private static final String[] LOCATION_PERMS_BELOW_Q = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] LOCATION_PERMS_Q_AND_ABOVE = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_edit_profile);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        if (fbUser == null) {
            // Not logged in → go to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(fbUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(fbUser.getUid());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupActivityResultLaunchers(); // Initialize launchers
        setupListeners();

        // Initially lock only the fields controlled by the switch
        setEditableFieldsEnabled(false);

        // Load existing profile
        loadUserProfile();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        PaymentPage = findViewById(R.id.PaymentPage);
        imgGalleryIcon = findViewById(R.id.imgGalleryIcon);
        btnChangeProfilePic = findViewById(R.id.btnChangeProfilePic);

        etName = findViewById(R.id.etName);
        etPinCode = findViewById(R.id.etPinCode);
        etDOB = findViewById(R.id.etDOB);

        radioGender = findViewById(R.id.radioGender);
        radioMale = findViewById(R.id.radioMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioOther = findViewById(R.id.radioOther);

        switchEdit = findViewById(R.id.switchEdit);

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        btnUseMyLocation = findViewById(R.id.btnUseMyLocation);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void setupActivityResultLaunchers() {
        // Request location permissions
        locationPermissionLauncher = registerForActivityResult( // Renamed
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean granted = true;
                    for (Boolean v : result.values()) {
                        if (v == null || !v) {
                            granted = false;
                            break;
                        }
                    }
                    if (granted) {
                        fetchCurrentAddressIntoField();
                    } else {
                        Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Android 13+ read images permission (This launcher is used only IF AvatarChooserActivity
        // would somehow trigger it indirectly, or if this activity itself needed to check)
        // Given the flow, AvatarChooserActivity will request its own gallery permissions.
        // We'll keep this just in case, but it's less critical for the current flow.
        readMediaImagesPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        // This might be for future direct gallery access from this activity,
                        // or if AvatarChooserActivity relies on a pre-granted permission.
                        // For the current flow, AvatarChooserActivity should handle its own permission.
                        Toast.makeText(this, "Gallery permission granted (via Edit Profile).", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permission required for gallery access.", Toast.LENGTH_SHORT).show();
                    }
                }
        );


        // Launcher for AvatarChooserActivity
        avatarChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.hasExtra("selectedAvatar")) {
                            // User selected one of the default avatars (drawable resource ID)
                            int drawableRes = data.getIntExtra("selectedAvatar", 0);
                            if (drawableRes != 0) {
                                imgGalleryIcon.setImageResource(drawableRes);
                                selectedImageUri = null; // No URI for upload when a default drawable is chosen
                                // ⭐ Save the drawable ID to Firebase
                                userRef.child("profileImageDrawableId").setValue(drawableRes);
                                // ⭐ Clear any existing profile image URL
                                userRef.child("profileImage").removeValue();
                                Toast.makeText(this, "Default avatar set.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (data.hasExtra("galleryImageUri")) {
                            // User chose from gallery (URI string)
                            String uriString = data.getStringExtra("galleryImageUri");
                            if (uriString != null) {
                                Uri uri = Uri.parse(uriString);
                                selectedImageUri = uri; // Set this for potential upload
                                Glide.with(CustomerEditProfileActivity.this)
                                        .load(uri)
                                        .placeholder(R.drawable.ic_default_avatar)
                                        .into(imgGalleryIcon);
                                // ⭐ Call uploadProfileImage here for gallery selection
                                uploadProfileImage(uri);
                                // ⭐ Clear any existing profile drawable ID
                                userRef.child("profileImageDrawableId").removeValue();
                            }
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        // ⭐ Corrected: Always open AvatarChooserActivity
        btnChangeProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerEditProfileActivity.this, AvatarChooserActivity.class);
            avatarChooserLauncher.launch(intent);
        });

        // ⭐ Corrected: Switch only affects Email, Phone, Address, and Location button
        switchEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setEditableFieldsEnabled(isChecked);
        });

        etDOB.setOnClickListener(v -> showDatePicker());
        etDOB.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showDatePicker();
        });

        btnUseMyLocation.setOnClickListener(v -> {
            if (!isLocationEnabled()) {
                showEnableLocationDialog();
                return;
            }
            requestLocationPermissionsAndFetch();
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    // ⭐ Corrected: This method ONLY affects fields below the "Want to Change?" switch.
    private void setEditableFieldsEnabled(boolean enabled) {
        etEmail.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        btnUseMyLocation.setEnabled(enabled);
        // Note: etName, etPinCode, etDOB, radioGender are always editable and not controlled by this switch
    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String dd = String.format(Locale.getDefault(), "%02d", dayOfMonth);
                    String mm = String.format(Locale.getDefault(), "%02d", month + 1);
                    String yyyy = String.valueOf(year);
                    etDOB.setText(dd + "/" + mm + "/" + yyyy);
                },
                y, m, d
        );
        dialog.show();
    }

    // ===== Load & Save Profile =====

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;

                String name = snap.child("name").getValue(String.class);
                String pin = snap.child("pinCode").getValue(String.class);
                String dob = snap.child("dob").getValue(String.class);
                String gender = snap.child("gender").getValue(String.class);
                String email = snap.child("email").getValue(String.class);
                String phone = snap.child("phone").getValue(String.class);
                String address = snap.child("address").getValue(String.class);
                String profileUrl = snap.child("profileImage").getValue(String.class);
                // ⭐ NEW: Load drawable ID for default avatars
                Integer profileDrawableId = snap.child("profileImageDrawableId").getValue(Integer.class);


                if (!TextUtils.isEmpty(name)) etName.setText(name);
                if (!TextUtils.isEmpty(pin)) etPinCode.setText(pin);
                if (!TextUtils.isEmpty(dob)) etDOB.setText(dob);

                if (!TextUtils.isEmpty(gender)) {
                    if ("Male".equalsIgnoreCase(gender)) radioMale.setChecked(true);
                    else if ("Female".equalsIgnoreCase(gender)) radioFemale.setChecked(true);
                    else radioOther.setChecked(true);
                }

                // These fields are loaded regardless of switch state
                if (!TextUtils.isEmpty(email)) etEmail.setText(email);
                if (!TextUtils.isEmpty(phone)) etPhone.setText(phone);
                if (!TextUtils.isEmpty(address)) etAddress.setText(address);

                // ⭐ Logic to load profile image based on what's stored
                if (profileDrawableId != null && profileDrawableId != 0) {
                    // Load default avatar if ID is stored
                    imgGalleryIcon.setImageResource(profileDrawableId);
                } else if (!TextUtils.isEmpty(profileUrl)) {
                    // Otherwise, load from URL using Glide
                    Glide.with(CustomerEditProfileActivity.this)
                            .load(profileUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar) // Fallback if Glide fails
                            .into(imgGalleryIcon);
                } else {
                    // Default if neither is found
                    imgGalleryIcon.setImageResource(R.drawable.ic_default_avatar);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerEditProfileActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String pin = etPinCode.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate always-required fields
        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter full name");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pin) || pin.length() < 6) {
            etPinCode.setError("Enter valid 6-digit PIN");
            etPinCode.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(dob)) {
            etDOB.setError("Select DOB");
            etDOB.requestFocus();
            return;
        }

        // Validate gender
        String gender = null;
        int selectedId = radioGender.getCheckedRadioButtonId();
        if (selectedId == R.id.radioMale) gender = "Male";
        else if (selectedId == R.id.radioFemale) gender = "Female";
        else if (selectedId == R.id.radioOther) gender = "Other";
        else {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate editable fields ONLY if switch is ON
        if (switchEdit.isChecked()) {
            if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                etEmail.requestFocus();
                return;
            }
            if (!TextUtils.isEmpty(phone) && phone.length() < 10) {
                etPhone.setError("Enter valid phone");
                etPhone.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(address)) {
                etAddress.setError("Enter address");
                etAddress.requestFocus();
                return;
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("pinCode", pin);
        map.put("dob", dob);
        map.put("gender", gender);

        // Only update these if switchEdit is on; otherwise leave existing
        if (switchEdit.isChecked()) {
            map.put("email", email);
            map.put("phone", phone);
            map.put("address", address);
        } else {
            // If switch is off, and these fields were changed, they won't be saved.
            // If you want to *clear* them if switch is off and they are empty, add logic here.
            // For now, they just won't be included in the update.
        }


        btnSaveProfile.setEnabled(false);
        userRef.updateChildren(map).addOnCompleteListener(task -> {
            btnSaveProfile.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                // Optionally lock fields again
                setEditableFieldsEnabled(false);
                switchEdit.setChecked(false);

                // ⭐ CRUCIAL: Finish this activity to go back to CustomerProfileInfoActivity
                finish();

            } else {
                Toast.makeText(this, "Update failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== Profile Image Upload =====

    private void uploadProfileImage(@NonNull Uri uri) {
        // Store as "avatar.jpg"
        storageRef.child("avatar.jpg")
                .putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.child("avatar.jpg").getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            userRef.child("profileImage").setValue(downloadUri.toString());
                            // ⭐ Clear drawable ID if a new image URL is uploaded
                            userRef.child("profileImageDrawableId").removeValue();
                            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ===== Location Helpers =====

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    private void showEnableLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("Please turn on Location to use current location.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    try {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "Cannot open settings", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void requestLocationPermissionsAndFetch() {
        String[] perms = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ? LOCATION_PERMS_Q_AND_ABOVE
                : LOCATION_PERMS_BELOW_Q;

        boolean allGranted = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            fetchCurrentAddressIntoField();
        } else {
            locationPermissionLauncher.launch(perms); // Use the renamed launcher
        }
    }

    private void fetchCurrentAddressIntoField() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission missing", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);

                    // ⭐ CORRECTED LOGIC: Use getAddressLine(0) for a full, well-formatted address.
                    String fullAddress = a.getAddressLine(0);
                    etAddress.setText(fullAddress);

                    // If PIN empty, try fill from geocoder
                    if (TextUtils.isEmpty(etPinCode.getText()) && a.getPostalCode() != null) {
                        etPinCode.setText(a.getPostalCode());
                    }
                } else {
                    Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}