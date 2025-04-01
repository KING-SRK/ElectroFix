package com.survice.electrofix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class RepairerProfileInfoActivity extends BaseActivity {

    private ImageView btnBack, imgRepairerProfile;
    private TextView tvRepairerName, tvRepairerEmail, tvRepairerPhone, tvRepairerSkills, tvRepairerLocation,
            tvRepairerPinCode, tvRepairerExperience, tvRepairerCharges, tvRepairerAvailability;
    private Button btnEditRepairerProfile;
    private FirebaseFirestore db;
    private DocumentReference profileRef;
    private String repairerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_profileinfo);

        repairerId = getIntent().getStringExtra("repairer_id");
        if (repairerId == null || repairerId.isEmpty()) {
            Log.e("RepairerProfile", "No Repairer ID found!");
            finish();
            return;
        }

        btnBack = findViewById(R.id.btnBack);
        imgRepairerProfile = findViewById(R.id.imgRepairerProfile);
        tvRepairerName = findViewById(R.id.tvRepairerName);
        tvRepairerEmail = findViewById(R.id.tvRepairerEmail);
        tvRepairerPhone = findViewById(R.id.tvRepairerPhone);
        tvRepairerSkills = findViewById(R.id.tvRepairerSkills);
        tvRepairerLocation = findViewById(R.id.tvRepairerLocation);
        tvRepairerPinCode = findViewById(R.id.tvRepairerPinCode);
        tvRepairerExperience = findViewById(R.id.tvRepairerExperience);
        tvRepairerCharges = findViewById(R.id.tvRepairerCharges);
        tvRepairerAvailability = findViewById(R.id.tvRepairerAvailability);
        btnEditRepairerProfile = findViewById(R.id.btnEditRepairerProfile);

        db = FirebaseFirestore.getInstance();
        profileRef = db.collection("Repairers").document(repairerId);

        btnBack.setOnClickListener(v -> finish());

        btnEditRepairerProfile.setOnClickListener(v -> {
            Intent intent = new Intent(RepairerProfileInfoActivity.this, RepairerEditProfileActivity.class);
            intent.putExtra("repairer_id", repairerId);
            startActivity(intent);
        });

        loadRepairerProfile();
    }

    private void loadRepairerProfile() {
        profileRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("Firestore Error", error.getMessage());
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                tvRepairerName.setText(snapshot.getString("name"));
                tvRepairerEmail.setText(snapshot.getString("email"));
                tvRepairerPhone.setText(snapshot.getString("phone"));
                tvRepairerSkills.setText(snapshot.getString("skills"));
                tvRepairerLocation.setText(snapshot.getString("location"));
                tvRepairerPinCode.setText(snapshot.getString("pinCode"));
                tvRepairerCharges.setText(snapshot.getString("charges"));
                tvRepairerExperience.setText(snapshot.getString("experience"));
                Boolean isAvailable = snapshot.getBoolean("availability");
                tvRepairerAvailability.setText(isAvailable ? "Online" : "Offline");
                tvRepairerAvailability.setTextColor(isAvailable ? Color.GREEN : Color.RED);
                String profileImageUrl = snapshot.getString("profileImageUrl");
                Glide.with(this).load(profileImageUrl).placeholder(R.drawable.repairer_man_logo).into(imgRepairerProfile);
            }
        });
    }
}