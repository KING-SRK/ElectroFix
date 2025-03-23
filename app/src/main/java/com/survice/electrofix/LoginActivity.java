package com.survice.electrofix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvLoginTitle;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String userType;
    private ProgressDialog progressDialog;
    private ImageButton btnBack, btnTogglePassword; // ✅ Back Button & Password Toggle
    private boolean isPasswordVisible = false; // ✅ Password Toggle State

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Get user type from intent
        userType = getIntent().getStringExtra("user_type");

        // UI Elements
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        btnBack = findViewById(R.id.btnBack); // ✅ Back Button
        btnTogglePassword = findViewById(R.id.btnTogglePassword); // ✅ Password Toggle Button

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        // Set Login Title
        tvLoginTitle.setText(userType.equals("Customer") ? "[As Customer]" : "[As Repairer]");

        // Back Button Click (Choice Screen এ ফেরত যাওয়ার জন্য)
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChoiceActivity.class);
            startActivity(intent);
            finish();
        });

        // Password Visibility Toggle
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Login Button Click
        btnLogin.setOnClickListener(v -> loginUser());

        // Don't have an account? Go to Signup
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            intent.putExtra("user_type", userType);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show ProgressDialog (Loading Animation Start)
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            // Hide ProgressDialog (Loading Animation Stop)
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    verifyUserType(currentUser.getUid());
                }
            } else {
                Toast.makeText(LoginActivity.this, "Login Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyUserType(String userId) {
        userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedUserType = snapshot.child("userType").getValue(String.class);
                    if (storedUserType != null && storedUserType.equals(userType)) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        // Redirect to Main Screen
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user_type", userType);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Access Denied! Wrong User Type.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User Data Not Found!", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_hide);
        } else {
            // Show Password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_show);
        }
        etPassword.setSelection(etPassword.getText().length()); // Cursor ঠিক রাখার জন্য
        isPasswordVisible = !isPasswordVisible;
    }
}