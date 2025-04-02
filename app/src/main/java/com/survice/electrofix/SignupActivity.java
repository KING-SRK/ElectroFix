package com.survice.electrofix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class SignupActivity extends BaseActivity {

    private EditText etEmail, etPhone, etPassword;
    private CheckBox cbRememberMe;
    private ImageView ivShowHidePassword;
    private ImageButton btnBack;  // ✅ Back Button
    private Button btnSignup;
    private TextView tvLogin, tvSignupTitle;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private boolean isPasswordVisible = false;
    private String userType;
    private ProgressDialog progressDialog; // ✅ ProgressDialog Object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Get user type from intent
        userType = getIntent().getStringExtra("user_type");

        // UI Elements
        btnBack = findViewById(R.id.btnBack); // ✅ Back Button
        tvSignupTitle = findViewById(R.id.tvSignupTitle);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        // Initialize ProgressDialog ✅
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing up...");
        progressDialog.setCancelable(false);

        // Set Signup Title
        tvSignupTitle.setText(userType.equals("Customer") ? "[As Customer]" : "[As Repairer]");

        // Password Show/Hide Functionality
        ivShowHidePassword.setOnClickListener(v -> togglePasswordVisibility());

        // ✅ Back Button Functionality
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, ChoiceActivity.class);
            startActivity(intent);
            finish();
        });

        // Signup Button Click
        btnSignup.setOnClickListener(v -> registerUser());

        // Already have an account? Go to Login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            intent.putExtra("user_type", userType);
            startActivity(intent);
            finish();
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivShowHidePassword.setImageResource(R.drawable.ic_eye_hide);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            ivShowHidePassword.setImageResource(R.drawable.ic_eye_show);
        }
        etPassword.setSelection(etPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Show ProgressDialog (Loading Animation Start)
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    // Firebase-এ User Data সংরক্ষণ করা
                    HashMap<String, String> userData = new HashMap<>();
                    userData.put("email", email);
                    userData.put("phone", phone);
                    userData.put("userType", userType);

                    userDatabase.child(userId).setValue(userData).addOnCompleteListener(task1 -> {
                        // ✅ Hide ProgressDialog (Loading Animation Stop)
                        progressDialog.dismiss();

                        if (task1.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();

                            // Main Screen-এ পাঠানো
                            Intent intent = new Intent(SignupActivity.this, mainActivity.class);
                            intent.putExtra("user_type", userType);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            } else {
                // ✅ Hide ProgressDialog if signup fails
                progressDialog.dismiss();
                Toast.makeText(SignupActivity.this, "Signup Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}