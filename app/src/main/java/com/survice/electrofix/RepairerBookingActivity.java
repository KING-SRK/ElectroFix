package com.survice.electrofix;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RepairerBookingActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private BookingListAdapter adapter;
    private List<BookingModel> bookingList;

    private FirebaseFirestore firestore;
    private String currentRepairerId;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_booking);

        recyclerView = findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progress_bar);

        firestore = FirebaseFirestore.getInstance();
        currentRepairerId = FirebaseAuth.getInstance().getUid();

        bookingList = new ArrayList<>();
        adapter = new BookingListAdapter(this, bookingList, currentRepairerId);
        recyclerView.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        loadBookings();
    }

    private void loadBookings() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar before loading

        firestore.collection("Bookings").get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE); // Hide progress bar after loading

            if (task.isSuccessful()) {
                bookingList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    BookingModel booking = document.toObject(BookingModel.class);
                    booking.setBookingId(document.getId());

                    String status = booking.getStatus() != null ? booking.getStatus() : "";
                    String acceptedBy = booking.getAcceptedBy() != null ? booking.getAcceptedBy() : "";

                    if ("pending".equalsIgnoreCase(status) ||
                            ("accepted".equalsIgnoreCase(status) && currentRepairerId.equals(acceptedBy))) {
                        bookingList.add(booking);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
            }
        });
    }
}