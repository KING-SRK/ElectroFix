package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChoiceActivity extends BaseActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 🔹 Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 🔹 Fetch user type from Firebase (Fix: Default to "Customer" if not found)
            String userType = getIntent().getStringExtra("user_type");
            if (userType == null) userType = "Customer"; // Default to Customer

            // 🔹 Redirect to Main Screen
            Intent intent = new Intent(ChoiceActivity.this,MainActivity.class);
            intent.putExtra("user_type", userType);
            startActivity(intent);
            finish();
            return; // Stop further execution
        }

        // 🔹 If not logged in, show choice screen
        setContentView(R.layout.activity_choice);

        ImageButton btnCustomer = findViewById(R.id.btnCustomer);
        ImageButton btnRepairer = findViewById(R.id.btnRepairer);

        // Customer Button Click
        btnCustomer.setOnClickListener(v -> {
            Toast.makeText(this, "User Selected", Toast.LENGTH_SHORT).show();
            navigateToSignup("User");
        });

        // Repairer Button Click
        btnRepairer.setOnClickListener(v -> {
            Toast.makeText(this, "Technician Selected", Toast.LENGTH_SHORT).show();
            navigateToSignup("Technician");
        });
    }

    private void navigateToSignup(String userType) {
        // 🔹 Always go to Signup Screen if not logged in
        Intent intent = new Intent(ChoiceActivity.this, SignupActivity.class);
        intent.putExtra("user_type", userType);
        startActivity(intent);
        finish();
    }
}