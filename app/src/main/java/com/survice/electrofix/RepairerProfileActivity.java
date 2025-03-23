package com.survice.electrofix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RepairerProfileActivity extends AppCompatActivity {

    private ImageView repairerProfileImage;
    private TextView repairerName;
    private Button btnProfileInfo, btnViewRatings, btnViewWorkHistory, btnLogout;
    private ImageButton homeButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_profile);

        // UI Elements Initialization
        repairerProfileImage = findViewById(R.id.repairerProfileImage);
        repairerName = findViewById(R.id.repairerName);

        btnProfileInfo = findViewById(R.id.btnProfileInfo);
        btnViewRatings = findViewById(R.id.btnViewRatings);
        btnViewWorkHistory = findViewById(R.id.btnViewWorkHistory);
        btnLogout = findViewById(R.id.btnLogout);
        homeButton = findViewById(R.id.home_button);

        // Button Click Listeners
        btnProfileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairerProfileActivity.this, RepairerProfileInfoActivity.class);
                startActivity(intent);
            }
        });

        btnViewRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairerProfileActivity.this, RepairerSetBudgetActivity.class);
                startActivity(intent);
            }
        });

        btnViewWorkHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairerProfileActivity.this, RepairerWorkHistoryActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairerProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    // Logout Function
    private void logoutUser() {
        // Firebase Authentication Logout
        FirebaseAuth.getInstance().signOut();

        // Clear SharedPreferences (যদি লোকালভাবে ইউজার ইনফো সংরক্ষণ করে থাকো)
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // ChoiceActivity-তে নিয়ে যাওয়া
        Intent intent = new Intent(RepairerProfileActivity.this, ChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // ব্যাক প্রেস করলে আগের স্ক্রিনে না ফেরার জন্য
        startActivity(intent);
        finish();
    }
}