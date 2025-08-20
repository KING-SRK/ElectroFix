package com.survice.electrofix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends BaseActivity {

    private TextInputEditText etName, etPhone, etAddress, etPreferredTime;
    private MaterialButton btnUseLocation, btnConfirmBooking;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;

    private boolean isBookingInProgress = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private String serviceName, servicePrice;

    private double latitude = 0.0, longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_form);

        progressBar = findViewById(R.id.progress_bar);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etPreferredTime = findViewById(R.id.et_preferred_time);
        btnUseLocation = findViewById(R.id.btn_use_location);
        btnConfirmBooking = findViewById(R.id.btn_confirm_booking);

        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        serviceName = getIntent().getStringExtra("service_name");
        servicePrice = getIntent().getStringExtra("service_price");

        etPreferredTime.setOnClickListener(v -> showDateTimePicker());
        btnUseLocation.setOnClickListener(v -> getCurrentLocation());
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                String formatted = android.text.format.DateFormat.format("dd MMM yyyy, hh:mm aa", calendar).toString();
                                etPreferredTime.setText(formatted);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void confirmBooking() {
        if (isBookingInProgress) return; // multiple click handle

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String preferredTime = etPreferredTime.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || preferredTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == 0.0 || longitude == 0.0) {
            getCurrentLocationWithCallback(() -> confirmBooking());
            return;
        }

        isBookingInProgress = true;
        progressBar.setVisibility(View.VISIBLE);
        btnConfirmBooking.setEnabled(false);

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("name", name);
        bookingData.put("phone", phone);
        bookingData.put("address", address);
        bookingData.put("preferredTime", preferredTime);
        bookingData.put("serviceName", serviceName);
        bookingData.put("servicePrice", servicePrice);
        bookingData.put("timestamp", System.currentTimeMillis());
        bookingData.put("status", "pending");
        bookingData.put("acceptedBy", "");
        bookingData.put("customerLatitude", latitude);
        bookingData.put("customerLongitude", longitude);

        firestore.collection("Bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_LONG).show();
                    clearFields();
                    progressBar.setVisibility(View.GONE);
                    btnConfirmBooking.setEnabled(true);
                    Intent intent = new Intent(BookingActivity.this, BookingSuccessActivity.class);
                    startActivity(intent);
                    finish();
                    isBookingInProgress = false;
                })

                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnConfirmBooking.setEnabled(true);
                    isBookingInProgress = false;
                });
    }

    private void clearFields() {
        etName.setText("");
        etPhone.setText("");
        etAddress.setText("");
        etPreferredTime.setText("");
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                getAddressFromLocation(location);
            } else {
                Toast.makeText(this, "Locating you, please wait...", Toast.LENGTH_SHORT).show();
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(fallbackLocation -> {
                            if (fallbackLocation != null) {
                                latitude = fallbackLocation.getLatitude();
                                longitude = fallbackLocation.getLongitude();
                                getAddressFromLocation(fallbackLocation);
                            } else {
                                Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getCurrentLocationWithCallback(Runnable onLocationReady) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                getAddressFromLocation(location);
                onLocationReady.run();
            } else {
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(fallbackLocation -> {
                            if (fallbackLocation != null) {
                                latitude = fallbackLocation.getLatitude();
                                longitude = fallbackLocation.getLongitude();
                                getAddressFromLocation(fallbackLocation);
                                onLocationReady.run();
                            } else {
                                Toast.makeText(this, "Please turn on your location", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getAddressFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                etAddress.setText(address.getAddressLine(0));
            } else {
                Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

}