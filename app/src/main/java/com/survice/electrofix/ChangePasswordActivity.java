package com.survice.electrofix;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends BaseActivity {

    private EditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private Button btnUpdatePassword;
    private ProgressBar progressBar;
    private ImageButton btnToggleCurrentPassword, btnToggleNewPassword, btnToggleConfirmPassword;
    private TextView tvForgotPassword;

    private FirebaseUser currentUser;

    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // UI উপাদানগুলো ইনিশিয়ালাইজ করছি
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        progressBar = findViewById(R.id.progressBar);
        btnToggleCurrentPassword = findViewById(R.id.btnToggleCurrentPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // কারেন্ট পাসওয়ার্ড দেখানোর/লুকানোর টগল
        btnToggleCurrentPassword.setOnClickListener(v -> {
            if (isCurrentPasswordVisible) {
                edtCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggleCurrentPassword.setImageResource(R.drawable.ic_eye_hide);
            } else {
                edtCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggleCurrentPassword.setImageResource(R.drawable.ic_eye_show);
            }
            edtCurrentPassword.setSelection(edtCurrentPassword.getText().length());
            isCurrentPasswordVisible = !isCurrentPasswordVisible;
        });

        // নতুন পাসওয়ার্ড দেখানোর/লুকানোর টগল
        btnToggleNewPassword.setOnClickListener(v -> {
            if (isNewPasswordVisible) {
                edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggleNewPassword.setImageResource(R.drawable.ic_eye_hide);
            } else {
                edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggleNewPassword.setImageResource(R.drawable.ic_eye_show);
            }
            edtNewPassword.setSelection(edtNewPassword.getText().length());
            isNewPasswordVisible = !isNewPasswordVisible;
        });

        // কনফার্ম পাসওয়ার্ড দেখানোর/লুকানোর টগল
        btnToggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_hide);
            } else {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_eye_show);
            }
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        });

        // পাসওয়ার্ড আপডেট বাটনে ক্লিক
        btnUpdatePassword.setOnClickListener(v -> updatePassword());

        // Forgot Password টেক্সট এ ক্লিক করলে পাসওয়ার্ড রিসেট ইমেইল পাঠানো হবে
        tvForgotPassword.setOnClickListener(v -> sendPasswordResetEmail());

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void updatePassword() {
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            edtCurrentPassword.setError("Current password is required");
            edtCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            edtNewPassword.setError("New password is required");
            edtNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            edtNewPassword.setError("Password should be at least 6 characters");
            edtNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdatePassword.setEnabled(false);

        // রি-অথেনটিকেটেশন
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                // সফল হলে পাসওয়ার্ড আপডেট করব
                currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdatePassword.setEnabled(true);
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                btnUpdatePassword.setEnabled(true);
                Toast.makeText(ChangePasswordActivity.this, "Re-authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendPasswordResetEmail() {
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().sendPasswordResetEmail(currentUser.getEmail())
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password reset email sent to " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
