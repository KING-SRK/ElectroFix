package com.survice.electrofix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RepairerProfileActivity extends BaseActivity {

    private ImageView repairerProfileImage;
    private TextView repairerName;
    private Button btnProfileInfo, btnViewRatings, btnViewWorkHistory, btnLogout;
    private ImageButton homeButton;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_repairer_profile);

        repairerProfileImage = findViewById(R.id.repairerProfileImage);
        repairerName = findViewById(R.id.repairerName);
        btnProfileInfo = findViewById(R.id.btnProfileInfo);
        btnViewRatings = findViewById(R.id.btnViewRatings);
        btnViewWorkHistory = findViewById(R.id.btnViewWorkHistory);
        btnLogout = findViewById(R.id.btnLogout);
        homeButton = findViewById(R.id.home_button);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            logoutUser();
        }

        btnProfileInfo.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerProfileInfoActivity.class);
            intent.putExtra("repairer_id", currentUser.getUid()); // Repairer ID পাঠানো
            startActivity(intent);
        });

        btnViewRatings.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerSetBudgetActivity.class);
            startActivity(intent);
        });

        btnViewWorkHistory.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, RepairerWorkHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutUser());

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(RepairerProfileActivity.this, ChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}