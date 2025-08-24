package com.survice.electrofix;

import android.util.Log;
import android.view.View;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class BookingService {

    private FirebaseFirestore db;

    public BookingService() {
        // Initialize Firestore in the constructor
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new booking document in Firestore.
     * @param userId The ID of the current user.
     * @param serviceCategory The category of the service.
     * @param serviceTitle The title of the service.
     */
    public void createBooking(String userId, String serviceCategory, String serviceTitle) {
        // Generate a unique booking ID
        String bookingId = "BOOKING" + System.currentTimeMillis();

        // Create a Map with the booking data
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("message", "Your booking is confirmed");
        bookingData.put("bookingId", bookingId);
        bookingData.put("createdAt", FieldValue.serverTimestamp()); // Use server timestamp for accuracy
        bookingData.put("userId", userId);
        bookingData.put("serviceCategory", serviceCategory);
        bookingData.put("serviceTitle", serviceTitle);
        bookingData.put("status", "Pending");

        // Write the data to Firestore
        db.collection("Bookings")
                .document(userId)
                .collection("UserBookings")
                .document(bookingId)
                .set(bookingData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("BookingService", "Booking document created successfully with ID: " + bookingId);
                    // The TrackingActivity will automatically update because of its listener.
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingService", "Error creating booking document", e);
                });
    }
    // Assuming this is the code in your 'ServiceDetailsActivity'
    public void onBookNowClick(View view) {
        String currentUserId = "your_user_id_here"; // Get this from Firebase Auth
        String selectedServiceCategory = "General Services";
        String selectedServiceTitle = "AC Repair";

        // Create an instance of your new service class
        BookingService bookingService = new BookingService();

        // Call the method to create the booking in Firestore
        bookingService.createBooking(currentUserId, selectedServiceCategory, selectedServiceTitle);

        // After calling this, the Firestore listener in your TrackingActivity
        // will automatically receive the new data and display it.

        // You can then navigate to the TrackingActivity
        // Intent intent = new Intent(this, TrackingActivity.class);
        // startActivity(intent);
    }
}