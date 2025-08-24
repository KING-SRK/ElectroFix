package com.survice.electrofix;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    // UI Elements
    private ImageButton btnBack;
    private TextView serviceTitleTextView;
    private TextView categoryTitleTextView;
    private TextView priceSectionTextView;
    private TextView priceDescriptionTextView;
    private Switch switchEdit;
    private EditText etName;
    private EditText etPinCode;
    private EditText etPhone;
    private EditText etAddress;
    private MaterialButton btnUseMyLocation;
    private Button btnBookService;

    // Firebase References
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseFirestore db;

    // Data to be loaded dynamically
    private String currentServiceTitle;
    private String currentCategoryTitle;
    private String currentPrice;
    private String currentPriceDescription;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private String readableUserId; // Variable to store the human-readable user ID

    // Permissions arrays
    private static final String[] LOCATION_PERMS_BELOW_Q = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] LOCATION_PERMS_Q_AND_ABOVE = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize database references
        db = FirebaseFirestore.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        initUI();
        loadDynamicContent();
        loadAndAutofillUserDetails();
        setupListeners();
        updateEditFieldsState(switchEdit.isChecked());
        setupActivityResultLaunchers();
    }

    /**
     * Initializes all UI elements by finding them by their IDs.
     */
    private void initUI() {
        btnBack = findViewById(R.id.btnBack);
        serviceTitleTextView = findViewById(R.id.serviceTitle);
        categoryTitleTextView = findViewById(R.id.catagoryTitle);
        priceSectionTextView = findViewById(R.id.pricesection);
        priceDescriptionTextView = findViewById(R.id.pricedescription);
        switchEdit = findViewById(R.id.switchEdit);
        etName = findViewById(R.id.etName);
        etPinCode = findViewById(R.id.etPinCode);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnUseMyLocation = findViewById(R.id.btnUseMyLocation);
        btnBookService = findViewById(R.id.btnBookService);
    }

    /**
     * Loads service details from the Intent extras.
     */
    private void loadDynamicContent() {
        Intent intent = getIntent();
        if (intent != null) {
            currentServiceTitle = intent.getStringExtra("SERVICE_TITLE");
            currentCategoryTitle = intent.getStringExtra("CATEGORY_TITLE");
            currentPrice = intent.getStringExtra("PRICE");
            currentPriceDescription = intent.getStringExtra("PRICE_DESCRIPTION");

            if (currentServiceTitle != null) {
                serviceTitleTextView.setText(currentServiceTitle);
            }
            if (currentCategoryTitle != null) {
                categoryTitleTextView.setText(currentCategoryTitle);
            }
            if (currentPrice != null) {
                priceSectionTextView.setText(currentPrice);
            }
            if (currentPriceDescription != null) {
                priceDescriptionTextView.setText(currentPriceDescription);
            }
        }
    }

    /**
     * Fetches the logged-in user's details from Firebase and autofills the form.
     */
    private void loadAndAutofillUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get data from Firebase and set to EditTexts
                    String name = snapshot.child("userId").getValue(String.class); // Assuming 'userId' is the name field
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    // The pin code might not be in the database, but we can try to get it
                    String pinCode = snapshot.child("pinCode").getValue(String.class); // Make sure 'pinCode' exists in your Realtime DB

                    // Set the retrieved data to the EditTexts
                    if (name != null && !name.isEmpty()) {
                        etName.setText(name);
                        readableUserId = name;
                    } else {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null && currentUser.getEmail() != null) {
                            readableUserId = currentUser.getEmail();
                        }
                    }
                    if (phone != null && !phone.isEmpty()) {
                        etPhone.setText(phone);
                    }
                    if (address != null && !address.isEmpty()) {
                        etAddress.setText(address);
                    }
                    if (pinCode != null && !pinCode.isEmpty()) {
                        etPinCode.setText(pinCode);
                    }

                } else {
                    Toast.makeText(BookingActivity.this, "User profile not found. Please fill details.", Toast.LENGTH_LONG).show();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null && currentUser.getEmail() != null) {
                        readableUserId = currentUser.getEmail();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Sets up all click and change listeners for UI elements.
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        switchEdit.setOnCheckedChangeListener((buttonView, isChecked) -> updateEditFieldsState(isChecked));

        btnBookService.setOnClickListener(v -> {
            String customerName = etName.getText().toString().trim();
            String customerPhone = etPhone.getText().toString().trim();
            String customerAddress = etAddress.getText().toString().trim();
            String customerPinCode = etPinCode.getText().toString().trim();

            if (customerName.isEmpty() || customerPhone.isEmpty() || customerAddress.isEmpty() || customerPinCode.isEmpty()) {
                showBookingFailedDialog("Please fill in all customer details.");
                return;
            }

            saveBookingToFirestore(customerName, customerPhone, customerAddress, customerPinCode);
        });

        btnUseMyLocation.setOnClickListener(v -> {
            if (!isLocationEnabled()) {
                showEnableLocationDialog();
                return;
            }
            requestLocationPermissionsAndFetch();
        });
    }

    private void setupActivityResultLaunchers() {
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean granted = true;
                    for (Boolean v : result.values()) {
                        if (v == null || !v) {
                            granted = false;
                            break;
                        }
                    }
                    if (granted) {
                        fetchCurrentAddressIntoField();
                    } else {
                        Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    private void showEnableLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("Please turn on Location to use current location.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    try {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "Cannot open settings", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void requestLocationPermissionsAndFetch() {
        String[] perms = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ? LOCATION_PERMS_Q_AND_ABOVE
                : LOCATION_PERMS_BELOW_Q;

        boolean allGranted = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            fetchCurrentAddressIntoField();
        } else {
            locationPermissionLauncher.launch(perms);
        }
    }

    private void fetchCurrentAddressIntoField() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission missing", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);
                    String fullAddress = a.getAddressLine(0);
                    etAddress.setText(fullAddress);
                    if (TextUtils.isEmpty(etPinCode.getText()) && a.getPostalCode() != null) {
                        etPinCode.setText(a.getPostalCode());
                    }
                } else {
                    Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateEditFieldsState(boolean isEnabled) {
        etName.setEnabled(isEnabled);
        etPinCode.setEnabled(isEnabled);
        etPhone.setEnabled(isEnabled);
        etAddress.setEnabled(isEnabled);
        btnUseMyLocation.setEnabled(isEnabled);
    }

    private void showBookingSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Booking Successful!")
                .setMessage("Your service booking has been confirmed.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void showBookingFailedDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Booking Failed")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    /**
     * ⭐ CORRECTED: Method to save the booking data to Firestore.
     */
    private void saveBookingToFirestore(String customerName, String customerPhone, String customerAddress, String customerPinCode) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showBookingFailedDialog("User not logged in. Please log in to book a service.");
            return;
        }

        // Use a HashMap to save data, including the readable ID
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("userId", currentUser.getUid()); // The unreadable ID for security
        bookingData.put("readableUserId", readableUserId); // The readable ID for you to see
        bookingData.put("serviceTitle", currentServiceTitle);
        bookingData.put("categoryTitle", currentCategoryTitle);
        bookingData.put("price", currentPrice);
        bookingData.put("customerName", customerName);
        bookingData.put("customerPhone", customerPhone);
        bookingData.put("customerAddress", customerAddress);
        bookingData.put("customerPinCode", customerPinCode);
        bookingData.put("status", "Pending");
        bookingData.put("message", "Your booking is confirmed");
        bookingData.put("createdAt", FieldValue.serverTimestamp()); // Firestore's server timestamp

        // Push the data to Firestore
        db.collection("Bookings")
                .add(bookingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Booking document has been created with an auto-generated ID
                        showBookingSuccessDialog();
                        // Optionally, you can log the new booking ID for your reference
                        Log.d("BookingActivity", "New booking added with ID: " + task.getResult().getId());
                    } else {
                        showBookingFailedDialog("Failed to save booking. Please check your internet connection and try again.");
                        Log.e("BookingActivity", "Error adding booking", task.getException());
                    }
                });
    }
}