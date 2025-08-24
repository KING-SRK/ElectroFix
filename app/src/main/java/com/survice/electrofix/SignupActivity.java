package com.survice.electrofix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser; // Add this import
import com.google.firebase.database.DataSnapshot; // Add this import
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query; // Add this import
import com.google.firebase.database.ValueEventListener; // Add this import

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "SignupActivity";
    private EditText etUserId, etPassword, etConfirmPassword, etEmail, etPhone, etAddress;
    private TextView tvUserIdError, tvPasswordError, tvConfirmPasswordError, tvEmailError, tvPhoneError;
    private TextView tvLogin;
    private Button btnSignup, btnUseLocation;
    private ImageView ivShowHidePassword, ivShowHideConfirmPassword;
    private ProgressBar progressBar;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private FirebaseAuth mAuth;
    // Change your userDatabase reference
    private DatabaseReference usernamesDatabase;
    private DatabaseReference userDatabase;
    private ImageButton btnBack;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLocationLat, currentLocationLng;
    private String currentLocationString = "";

    // Define UserModel as a public static nested class or a separate top-level class
    // This is important for Firebase to be able to deserialize it properly
    public static class UserModel {
        public String userId, email, phone, address, userType;
        public String signupTimestamp; // ✅ new field

        public UserModel() {
            // empty constructor required for Firebase
        }

        public UserModel(String userId, String email, String phone, String address) {
            this.userId = userId;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.userType = "Customer"; // default

            // ✅ store formatted timestamp
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault());
            this.signupTimestamp = sdf.format(new java.util.Date());
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users"); // "Users" is good
        usernamesDatabase = FirebaseDatabase.getInstance().getReference("usernames");

        // UI element binding (looks correct)
        progressBar = findViewById(R.id.progressBar);
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSignup = findViewById(R.id.btnsignup);
        btnUseLocation = findViewById(R.id.btnUseMyLocation);
        tvLogin = findViewById(R.id.tvLogin);
        btnBack = findViewById(R.id.btnBack); // Changed from btnBack as per common practice, verify your XML
        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);
        ivShowHideConfirmPassword = findViewById(R.id.ivShowHideConfirmPassword);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bind Error TextViews (important for displaying validation errors)
        tvUserIdError = findViewById(R.id.tvUserIdError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPhoneError = findViewById(R.id.tvPhoneError);

        // TextWatchers for real-time validation (good practice)
        etUserId.addTextChangedListener(simpleWatcher(etUserId));
        etPhone.addTextChangedListener(simpleWatcher(etPhone));
        etEmail.addTextChangedListener(simpleWatcher(etEmail));
        etPassword.addTextChangedListener(simpleWatcher(etPassword));
        etConfirmPassword.addTextChangedListener(simpleWatcher(etConfirmPassword));

        // Password toggle (looks correct)
        ivShowHidePassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, true));
        ivShowHideConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, false));

        // Button listeners (looks correct)
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        });

        btnUseLocation.setOnClickListener(v -> checkLocationPermission());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this,LoginActivity .class);
            startActivity(intent);
            finish();
        });

        btnSignup.setOnClickListener(v -> {
            // Validate all fields before attempting signup
            boolean validUserId = validateUserId();
            boolean validPassword = validatePassword();
            boolean validConfirm = validateConfirmPassword();
            boolean validEmail = validateEmail();
            boolean validPhone = validatePhone();

            if (validUserId && validPassword && validConfirm && validEmail && validPhone) {
                // All fields correct → proceed with signup
                registerUser();
            } else {
                // Show errors
                Toast.makeText(this, "Please fix errors before signup", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // `simpleWatcher` is a good way to handle multiple EditTexts
    private TextWatcher simpleWatcher(final Object o) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call respective validation when text changes
                if (o == etUserId) {
                    validateUserId();
                } else if (o == etPhone) {
                    validatePhone();
                } else if (o == etEmail) {
                    validateEmail();
                } else if (o == etPassword) {
                    validatePassword();
                } else if (o == etConfirmPassword) {
                    validateConfirmPassword();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Optional, can be left empty
            }
        };
    }

    // Validation methods are well-defined
    private boolean validatePassword() {
        String password = etPassword.getText().toString().trim();
        // 10–15 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,15}$";

        if (!password.matches(passwordRegex)) {
            tvPasswordError.setText("Password must be 10-15 chars, contain uppercase, lowercase, digit, and special char."); // Add a helpful message
            tvPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else {
            tvPasswordError.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (confirmPassword.isEmpty()) {
            tvConfirmPasswordError.setText("Confirm password cannot be empty."); // Add a helpful message
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else if (!confirmPassword.equals(password)) {
            tvConfirmPasswordError.setText("Passwords do not match."); // Add a helpful message
            tvConfirmPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else {
            tvConfirmPasswordError.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.setText("Please enter a valid email address."); // Add a helpful message
            tvEmailError.setVisibility(View.VISIBLE);
            return false;
        } else {
            tvEmailError.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean validatePhone() {
        String phone = etPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            tvPhoneError.setText("Phone number cannot be empty.");
            tvPhoneError.setVisibility(View.VISIBLE);
            return false;
        } else if (!phone.matches("^[6-9]\\d{9}$")) { // Indian 10-digit numbers starting with 6–9
            tvPhoneError.setText("Please enter a valid 10-digit Indian phone number (starts with 6-9)."); // Add a helpful message
            tvPhoneError.setVisibility(View.VISIBLE);
            return false;
        } else {
            tvPhoneError.setVisibility(View.GONE);
            return true;
        }
    }

    private boolean validateUserId() {
        String userId = etUserId.getText().toString().trim();
        if (userId.isEmpty()) {
            tvUserIdError.setText("User ID cannot be empty.");
            tvUserIdError.setVisibility(View.VISIBLE);
            return false;
        } else if (!userId.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$")) {
            tvUserIdError.setText("User ID must be 8-20 characters, contain letters and digits."); // Add a helpful message
            tvUserIdError.setVisibility(View.VISIBLE);
            return false;
        } else {
            tvUserIdError.setVisibility(View.GONE);
            return true;
        }
    }

    private void togglePasswordVisibility(EditText editText, boolean isPassword) {
        if (isPassword) {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivShowHidePassword.setImageResource(R.drawable.ic_eye_show);
            } else {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivShowHidePassword.setImageResource(R.drawable.ic_eye_hide);
            }
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ivShowHideConfirmPassword.setImageResource(R.drawable.ic_eye_show);
            } else {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ivShowHideConfirmPassword.setImageResource(R.drawable.ic_eye_hide);
            }
        }
        editText.setSelection(editText.getText().length());
    }

    // Location related methods are fine, assuming necessary permissions are requested in Manifest
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkGPSAndFetchLocation();
        }
    }

    private void checkGPSAndFetchLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDialog();
        } else {
            getUserLocation();
        }
    }

    private void showGPSDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Location is required for this feature. Please enable Location.")
                .setCancelable(false)
                .setPositiveButton("Turn On Location", (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        // First try last location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateAddressWithLocation(location);
                    } else {
                        // If no cached location, request a fresh one
                        requestNewLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    requestNewLocation(); // fallback
                });
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocation() {
        com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create()
                .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)          // 1s interval
                .setFastestInterval(500)    // 0.5s fastest
                .setNumUpdates(1);          // only once

        fusedLocationClient.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                if (locationResult == null) return;
                Location freshLocation = locationResult.getLastLocation();
                if (freshLocation != null) {
                    updateAddressWithLocation(freshLocation);
                }
            }
        }, getMainLooper());
    }


    private void updateAddressWithLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, "Fresh Location: Lat=" + latitude + ", Lng=" + longitude);

            new Thread(() -> {
                Geocoder geocoder = new Geocoder(SignupActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    runOnUiThread(() -> {
                        if (addresses != null && !addresses.isEmpty()) {
                            Address addr = addresses.get(0);

                            // ✅ Use full formatted address
                            String fullAddress = addr.getAddressLine(0);

                            etAddress.setText(fullAddress);
                            Toast.makeText(SignupActivity.this, "Address found!", Toast.LENGTH_SHORT).show();
                        } else {
                            etAddress.setText("Lat: " + latitude + ", Lng: " + longitude);
                            Toast.makeText(SignupActivity.this, "Only coordinates available.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        etAddress.setText("Lat: " + latitude + ", Lng: " + longitude);
                        Toast.makeText(SignupActivity.this, "Geocoder failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        }
    }



    @SuppressLint("RestrictedApi")
    private void registerUser() {
        String userId = etUserId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Re-check fields (good final safeguard)
        if (userId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Ensure progress bar is hidden on error
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Ensure progress bar is hidden on error
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show progress bar at the start of the async operation

        // --- STEP 1: Check if userId (username) already exists in the Realtime Database ---
        // Query userIdQuery = userDatabase.orderByChild("userId").equalTo(userId);
        usernamesDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // If snapshot exists, it means a user with this userId already exists
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                    Toast.makeText(SignupActivity.this, "This User ID is already taken. Please choose another.", Toast.LENGTH_LONG).show();
                    etUserId.setError("User ID already exists"); // Set an error on the EditText
                } else {
                    // --- STEP 2: If userId is unique, proceed with Firebase Authentication (Email uniqueness) ---
                    // The username is unique, proceed with authentication
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE); // Hide progress bar regardless of auth outcome
                                if (task.isSuccessful()) {
                                    // User created successfully, now save data
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String uid = firebaseUser.getUid();

                                        // Create user data model
                                        UserModel newUser = new UserModel(userId, email, phone, address);

                                        // Save to both nodes
                                        userDatabase.child(uid).setValue(newUser);
                                        usernamesDatabase.child(userId).setValue(true)
                                                .addOnCompleteListener(saveTask -> {
                                                    if (saveTask.isSuccessful()) {
                                                        Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                        finish();
                                                    } else {
                                                        // This is a rare case where Auth succeeded but DB save failed.
                                                        // Consider deleting the Firebase Auth user here to prevent orphaned accounts.
                                                        // Or, guide the user to try again later.
                                                        Toast.makeText(SignupActivity.this, "Failed to save user details. Please try again or contact support.", Toast.LENGTH_LONG).show();
                                                        Log.e(TAG, "Firebase Database Save Error: ", saveTask.getException());
                                                        // Option to delete Firebase Auth user if DB write fails
                                                        firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                                                            if(deleteTask.isSuccessful()) Log.d(TAG, "Orphaned Firebase Auth user deleted.");
                                                            else Log.e(TAG, "Failed to delete orphaned Firebase Auth user.");
                                                        });
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Firebase user is null after registration.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Firebase Authentication failed (e.g., email already in use, weak password)
                                    String errorMessage = "Signup failed.";
                                    if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                        errorMessage += " Password is too weak.";
                                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        errorMessage += " Invalid email format or email already in use.";
                                    } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        errorMessage += " This email address is already registered.";
                                    } else if (task.getException() != null) {
                                        errorMessage += " " + task.getException().getMessage();
                                    }
                                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Firebase Auth Signup Error: ", task.getException());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide progress bar on error
                Toast.makeText(SignupActivity.this, "Database error during User ID check: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "User ID check failed: ", error.toException());
            }
        });
    }
    // Handle permission result (looks correct)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGPSAndFetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}