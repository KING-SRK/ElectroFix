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
    private String repairerId = "your_repairer_id"; // Firestore থেকে রিয়েল ডেটা পাওয়ার জন্য ডায়নামিকভাবে সেট করো

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_profileinfo);

        // UI ইনিশিয়ালাইজেশন
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

        // Firestore ইনিশিয়ালাইজেশন
        db = FirebaseFirestore.getInstance();
        profileRef = db.collection("Repairers").document(repairerId);

        // ব্যাক বাটনের জন্য ক্লিক লিসেনার
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // আগের স্ক্রিনে ফিরে যাবে
            }
        });

        // প্রোফাইল এডিট করার জন্য ক্লিক লিসেনার
        btnEditRepairerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairerProfileInfoActivity.this, RepairerEditProfileActivity.class);
                startActivity(intent);
            }
        });

        // Firestore থেকে রিয়েল-টাইম ডেটা লোড করা
        loadRepairerProfile();
    }

    private void loadRepairerProfile() {
        profileRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore Error", error.getMessage());
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    // ফায়ারস্টোর থেকে ডেটা পাওয়া গেলে UI-তে সেট করো
                    tvRepairerName.setText(snapshot.getString("name"));
                    tvRepairerEmail.setText(snapshot.getString("email"));
                    tvRepairerPhone.setText(snapshot.getString("phone"));
                    tvRepairerSkills.setText(snapshot.getString("skills"));
                    tvRepairerLocation.setText(snapshot.getString("location"));
                    tvRepairerPinCode.setText(snapshot.getString("pinCode"));
                    tvRepairerCharges.setText(snapshot.getString("charges"));
                    tvRepairerExperience.setText(snapshot.getString("experience"));

                    // Availability Status চেক করা
                    Boolean isAvailable = snapshot.getBoolean("availability");
                    if (isAvailable != null && isAvailable) {
                        tvRepairerAvailability.setText("Online");
                        tvRepairerAvailability.setTextColor(Color.GREEN);
                    } else {
                        tvRepairerAvailability.setText("Offline");
                        tvRepairerAvailability.setTextColor(Color.RED);
                    }

                    // প্রোফাইল ইমেজ লোড করা (Firebase Storage থেকে)
                    String profileImageUrl = snapshot.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(RepairerProfileInfoActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.repairer_man_logo) // ডিফল্ট ইমেজ
                                .into(imgRepairerProfile);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRepairerProfile(); // যখন Edit Profile থেকে ফিরে আসবে, তখনও আপডেট হবে
    }
}