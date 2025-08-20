package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log; // Added for logging exceptions
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Added FirebaseUser import
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot; // Added DataSnapshot import
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query; // Added Query import
import com.google.firebase.database.ValueEventListener; // Added ValueEventListener import

import java.util.concurrent.TimeUnit;

public class LoginActivity extends BaseActivity {

    private EditText etUserId, etOtp, etPassword, etNewPassword, etConfirmPassword, etMobile;
    private Button btnLogin, btnVerifyOtp, btnUpdatePassword;
    private TextView tvSignup, tvLoginTitle, tvForgetPassword, tvTimer;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private ProgressDialog progressDialog;
    private ImageButton btnBack, btnTogglePassword;
    private String mVerificationId;
    private String userUIDForPasswordReset; // Renamed from resetUserId for clarity, it stores Firebase UID
    private LinearLayout otpLayout, newPasswordLayout;
    private boolean isPasswordVisible = false;
    private CountDownTimer countDownTimer;

    // Make sure your UserModel is defined somewhere accessible, ideally as a public static class
    // in SignupActivity or its own file. It should look like this:
    /*
    public static class UserModel {
        public String userId, email, phone, address, userType; // userId (for username), phone (for mobile number)
        public UserModel() {}
        public UserModel(String userId, String email, String phone, String address) {
            this.userId = userId;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.userType = "Customer"; // Default type for signups
        }
    }
    */

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // UI initialization
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.loginBtn);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgetPassword = findViewById(R.id.tvForgetPassword);
        btnBack = findViewById(R.id.btnBack); // Check this ID in your XML if issues occur
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        otpLayout = findViewById(R.id.otpLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvTimer = findViewById(R.id.tvTimer);

        // New password fields from XML
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Mobile number field from XML
        etMobile = findViewById(R.id.etMobile);

        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Title
        tvLoginTitle.setText("Login");

        // Listeners
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> loginUser());

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        // Forgot Password flow
        tvForgetPassword.setOnClickListener(v -> {
            String userIdInput = etUserId.getText().toString().trim(); // Use a clear variable name
            if (userIdInput.isEmpty()) {
                Toast.makeText(this, "Enter User ID first to proceed with password reset", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Checking user...");
            progressDialog.show();

            // ✅ CRITICAL CHANGE: Query by "userId" not "userIdName"
            Query query = userDatabase.orderByChild("userId").equalTo(userIdInput);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    progressDialog.dismiss();
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            // ✅ CRITICAL CHANGE: Retrieve phone number from "phone" field
                            String phoneNumber = userSnap.child("phone").getValue(String.class);
                            // Store the actual Firebase UID for later password update
                            userUIDForPasswordReset = userSnap.getKey();

                            if (!TextUtils.isEmpty(phoneNumber)) {
                                // Add country code if not already part of your stored phone number
                                // Assuming +91 for India. Adjust if your users are from other regions.
                                if (!phoneNumber.startsWith("+")) {
                                    phoneNumber = "+91" + phoneNumber;
                                }
                                sendOtp(phoneNumber);
                                otpLayout.setVisibility(View.VISIBLE);
                                startOtpTimer();
                                // Hide other login fields for OTP flow
                                etUserId.setVisibility(View.GONE);
                                etPassword.setVisibility(View.GONE);
                                etMobile.setVisibility(View.GONE); // Ensure mobile field is hidden too
                                btnLogin.setVisibility(View.GONE);
                                tvSignup.setVisibility(View.GONE);
                                tvForgetPassword.setVisibility(View.GONE);
                                btnTogglePassword.setVisibility(View.GONE);

                            } else {
                                Toast.makeText(LoginActivity.this, "No phone number linked with this User ID. Cannot reset password.", Toast.LENGTH_SHORT).show();
                            }
                            break; // Found the user, no need to iterate further
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "User ID not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LoginActivity", "Forget Password DB error: ", error.toException());
                }
            });
        });

        // Verify OTP
        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.setMessage("Verifying OTP...");
                progressDialog.show();
                verifyOtp(otp);
            }
        });

        // Update Password after OTP success
        btnUpdatePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Enter both new password and confirm password", Toast.LENGTH_SHORT).show();
            } else if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // IMPORTANT: The password update happens on the currently authenticated FirebaseUser,
                // not by writing to the Realtime Database.
                updatePasswordInFirebaseAuth(newPassword);
            }
        });
    }

    // OTP Timer
    private void startOtpTimer() {
        tvTimer.setVisibility(View.VISIBLE);
        // btnVerifyOtp.setEnabled(true); // Don't enable here, it's enabled by default

        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancel any existing timer
        }

        countDownTimer = new CountDownTimer(60000, 1000) { // 60 seconds timer
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Resend OTP in 00:" + (seconds < 10 ? "0" + seconds : seconds));
                btnVerifyOtp.setEnabled(true); // Keep enabled during countdown to allow verification
            }

            @Override
            public void onFinish() {
                tvTimer.setText("OTP Expired. You can request a new one.");
                btnVerifyOtp.setEnabled(false); // Disable after timeout
                // You might want a "Resend OTP" button to become visible/enabled here
            }
        }.start();
    }

    // Send OTP
    private void sendOtp(String phoneNumber) {
        progressDialog.setMessage("Sending OTP...");
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS) // Wait for 60 seconds
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    progressDialog.dismiss();
                    // Auto-retrieval or instant verification
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "OTP Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LoginActivity", "PhoneAuth verification failed", e);
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(verificationId, token);
                    progressDialog.dismiss();
                    mVerificationId = verificationId;
                    Toast.makeText(LoginActivity.this, "OTP Sent to your registered mobile number!", Toast.LENGTH_LONG).show();
                }
            };

    // Verify OTP
    private void verifyOtp(String otp) {
        if (mVerificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
            signInWithPhoneAuthCredential(credential);
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Verification ID is missing. Please request OTP again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressDialog.setMessage("Signing in with OTP...");
        progressDialog.show();
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "OTP Verified. Set your new password.", Toast.LENGTH_SHORT).show();
                otpLayout.setVisibility(View.GONE);
                newPasswordLayout.setVisibility(View.VISIBLE);
                if (countDownTimer != null) {
                    countDownTimer.cancel(); // Stop the timer once OTP is verified
                }
            } else {
                Toast.makeText(this, "OTP Verification Failed. Please check the OTP.", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "OTP sign in failed", task.getException());
            }
        });
    }

    // ✅ IMPORTANT: Corrected Password Update Method (Updates Firebase Auth, not DB field)
    private void updatePasswordInFirebaseAuth(String newPassword) {
        progressDialog.setMessage("Updating password...");
        progressDialog.show();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Re-authenticate user before password change (optional but good security practice)
            // If the user's session is fresh from OTP verification, this might not be strictly needed,
            // but for production apps, always consider re-authentication for sensitive ops.
            // For simplicity here, we're assuming the session from OTP is sufficient.

            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password Updated Successfully. Please log in with your new password.", Toast.LENGTH_LONG).show();
                            // Sign out the user immediately after password change for security
                            mAuth.signOut();
                            // Navigate back to login screen to force new login
                            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("LoginActivity", "Firebase Auth password update failed", task.getException());
                        }
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "No active user session found. Please re-verify your phone number.", Toast.LENGTH_LONG).show();
        }
    }


    // Login flow
    private void loginUser() {
        String userIdInput = etUserId.getText().toString().trim();
        String mobileInput = etMobile.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (userIdInput.isEmpty() || mobileInput.isEmpty() || passwordInput.isEmpty()) {
            Toast.makeText(this, "Enter User ID, Mobile Number, and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        // ✅ CRITICAL CHANGE: Query by "userId" not "userIdName"
        Query query = userDatabase.orderByChild("userId").equalTo(userIdInput);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Do NOT dismiss progressDialog here, it will be dismissed after FirebaseAuth signInWithEmailAndPassword
                if (snapshot.exists()) {
                    String foundEmail = null;
                    String foundPhone = null;
                    String foundUid = null;

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        // ✅ CRITICAL CHANGE: Retrieve phone number from "phone" field
                        foundPhone = userSnap.child("phone").getValue(String.class);
                        foundEmail = userSnap.child("email").getValue(String.class);
                        foundUid = userSnap.getKey(); // Firebase UID

                        // Check if phone numbers match for the found user ID
                        if (foundPhone != null && foundPhone.equals(mobileInput) && !TextUtils.isEmpty(foundEmail)) {
                            // If user ID and Phone match a record, proceed with Email/Password Auth
                            String finalFoundUid = foundUid;
                            mAuth.signInWithEmailAndPassword(foundEmail, passwordInput).addOnCompleteListener(task -> {
                                progressDialog.dismiss(); // Dismiss here after Auth attempt
                                if (task.isSuccessful()) {
                                    loginSuccess(finalFoundUid);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid credentials. Check email/password.", Toast.LENGTH_LONG).show();
                                    Log.e("LoginActivity", "Email/Password sign-in failed", task.getException());
                                }
                            });
                            return; // Exit onDataChange as user is found and authentication started
                        }
                    }
                    // If the loop finishes, it means User ID was found, but phone didn't match any.
                    progressDialog.dismiss(); // Dismiss if no matching phone number found
                    Toast.makeText(LoginActivity.this, "User ID found, but Mobile Number does not match.", Toast.LENGTH_LONG).show();

                } else {
                    // No user found with the given User ID
                    progressDialog.dismiss(); // Dismiss if user ID not found
                    Toast.makeText(LoginActivity.this, "User ID not found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Database error during login: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LoginActivity", "Login DB error: ", error.toException());
            }
        });
    }

    private void loginSuccess(String uid) {
        // This method is called upon successful Firebase Authentication.
        // You can fetch more user details here if needed, but the core login is done.
        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_hide);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_show);
        }
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}