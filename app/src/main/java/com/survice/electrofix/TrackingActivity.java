package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class TrackingActivity extends AppCompatActivity {

    private TextView tvServiceCategory, tvServiceTitle, tvMessage, tvBookingId, tvCreatedAt;
    private ImageView noOrdersImage,dustbinImage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // init views
        tvServiceCategory = findViewById(R.id.tvServiceCategory);
        tvServiceTitle = findViewById(R.id.tvServiceTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        noOrdersImage = findViewById(R.id.noOrdersImage);
        dustbinImage = findViewById(R.id.dustbinImage);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.dustbinImage).setOnClickListener(v -> onDeleteClick(v));
        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Example: bookingId passed from previous activity
        String bookingId = getIntent().getStringExtra("bookingId");
        if (bookingId == null) {
            Toast.makeText(this, "No booking ID found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listenBookingUpdates(userId, bookingId);
    }

    private void listenBookingUpdates(String userId, String bookingId) {
        DocumentReference bookingRef = db.collection("Bookings")
                .document(userId)
                .collection("UserBookings")
                .document(bookingId);

        bookingRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("TrackingActivity", "Listen failed.", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // ✅ Booking exists → show details, hide placeholder
                    noOrdersImage.setVisibility(View.GONE);

                    tvServiceCategory.setVisibility(View.VISIBLE);
                    tvServiceTitle.setVisibility(View.VISIBLE);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvCreatedAt.setVisibility(View.VISIBLE);
                    tvBookingId.setVisibility(View.VISIBLE);

                    String serviceCategory = snapshot.getString("serviceCategory");
                    String serviceTitle = snapshot.getString("serviceTitle");
                    String message = snapshot.getString("message");
                    String status = snapshot.getString("status");
                    String createdAt = snapshot.getString("createdAt");
                    String bookingId = snapshot.getString("bookingId");

                    tvServiceCategory.setText(serviceCategory != null ? serviceCategory : "N/A");
                    tvServiceTitle.setText(serviceTitle != null ? serviceTitle : "N/A");
                    tvMessage.setText(message != null ? message : "N/A");
                    tvCreatedAt.setText(createdAt != null ? createdAt : "-");
                    tvBookingId.setText(bookingId != null ? bookingId : "-");

                } else {
                    // ❌ No booking found → show placeholder image
                    noOrdersImage.setVisibility(View.VISIBLE);

                    tvServiceCategory.setVisibility(View.GONE);
                    tvServiceTitle.setVisibility(View.GONE);
                    tvMessage.setVisibility(View.GONE);
                    tvCreatedAt.setVisibility(View.GONE);
                    tvBookingId.setVisibility(View.GONE);
                }
            }
        });
    }

    public void onDeleteClick(View view) {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            String bookingId = getIntent().getStringExtra("bookingId");
            if (bookingId != null) {
                deleteBooking(userId, bookingId);
            } else {
                Toast.makeText(this, "No booking ID to delete!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteBooking(String userId, String bookingId) {
        db.collection("Bookings").document(userId).collection("UserBookings").document(bookingId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TrackingActivity", "DocumentSnapshot successfully deleted!");
                    Toast.makeText(TrackingActivity.this, "Booking successfully deleted!", Toast.LENGTH_SHORT).show();
                    // The onSnapshot listener will automatically update the UI to show the "no orders" image.
                })
                .addOnFailureListener(e -> {
                    Log.w("TrackingActivity", "Error deleting document", e);
                    Toast.makeText(TrackingActivity.this, "Error deleting booking.", Toast.LENGTH_SHORT).show();
                });
    }
}
