package com.survice.electrofix;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;

import com.google.android.gms.tasks.Task;

public class PaymentActivity extends AppCompatActivity {

    private EditText amountEt, noteEt;
    private ImageButton btnClearNote, qrBtn, backBtn;

    private Button payBtn, locAccessBtn;
    private View dimOverlay;
    private Button[] quickBtns;
    private boolean isFormatting = false;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> refreshLocationGate()
            );

    private static final int REQUEST_CHECK_SETTINGS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        amountEt     = findViewById(R.id.editTextAmount);
        noteEt       = findViewById(R.id.btnAddNote);
        noteEt.setFocusable(true);
        noteEt.setFocusableInTouchMode(true);
        noteEt.setCursorVisible(true);
        noteEt.setEnabled(true);
        noteEt.setSingleLine(false); // allow multiple lines if needed
        noteEt.setMaxLines(4);       // optional, for note input

        btnClearNote = findViewById(R.id.btnClear);
        payBtn       = findViewById(R.id.upiPaymentButton);
        locAccessBtn = findViewById(R.id.LocationAccessbtn);
        dimOverlay   = findViewById(R.id.dummyDim);
        qrBtn        = findViewById(R.id.btnQRScanner);
        backBtn      = findViewById(R.id.btnBack);

        quickBtns = new Button[]{
                findViewById(R.id.hundredrsBalence),
                findViewById(R.id.twohundred),
                findViewById(R.id.fivehundred),
                findViewById(R.id.oneThousand),
                findViewById(R.id.twoThousand)
        };

        backBtn.setOnClickListener(v -> finish());
        qrBtn.setOnClickListener(v -> startActivity(new Intent(this, QRScannerActivity.class)));
        btnClearNote.setOnClickListener(v -> noteEt.setText(""));

        amountEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (isFormatting) return;
                isFormatting = true;
                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.isEmpty()) {
                    amountEt.setText("");
                    payBtn.setEnabled(false);
                } else {
                    String formatted = "₹" + digits;
                    amountEt.setText(formatted);
                    amountEt.setSelection(formatted.length());
                    payBtn.setEnabled(true);
                }
                isFormatting = false;
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        setupQuickButton(R.id.hundredrsBalence, 100);
        setupQuickButton(R.id.twohundred, 200);
        setupQuickButton(R.id.fivehundred, 500);
        setupQuickButton(R.id.oneThousand, 1000);
        setupQuickButton(R.id.twoThousand, 2000);

        payBtn.setOnClickListener(v -> {
            String raw = amountEt.getText().toString().replaceAll("[^0-9]", "");
            if (raw.isEmpty() || "0".equals(raw)) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, PaymentConformationActivity.class);
            i.putExtra("amount", raw);
            i.putExtra("note", noteEt.getText().toString().trim());
            startActivity(i);
        });

        locAccessBtn.setOnClickListener(v -> requestLocationAccess());
        refreshLocationGate();
    }

    private void setupQuickButton(int id, int value) {
        Button b = findViewById(id);
        b.setOnClickListener(v -> {
            String raw = amountEt.getText().toString().replaceAll("[^0-9]", "");
            int current = raw.isEmpty() ? 0 : Integer.parseInt(raw);
            int total = current + value;
            isFormatting = true;
            amountEt.setText("₹" + total);
            amountEt.setSelection(("₹" + total).length());
            isFormatting = false;
            payBtn.setEnabled(true);
        });
    }

    private void requestLocationAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setFastestInterval(5000);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(true);

            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(this, response -> refreshLocationGate());

            task.addOnFailureListener(this, e -> {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(PaymentActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            refreshLocationGate(); // Check again if location is now enabled
        }
    }

    private void refreshLocationGate() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean gpsEnabled = ((LocationManager) getSystemService(LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        boolean isGateOpen = permissionGranted && gpsEnabled;

        backBtn.setVisibility(View.VISIBLE);
        locAccessBtn.setVisibility(isGateOpen ? View.GONE : View.VISIBLE);
        dimOverlay.setVisibility(isGateOpen ? View.GONE : View.VISIBLE);

        amountEt.setEnabled(isGateOpen);
        noteEt.setEnabled(isGateOpen);
        payBtn.setEnabled(isGateOpen);
        btnClearNote.setEnabled(isGateOpen);
        for (Button b : quickBtns) b.setEnabled(isGateOpen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLocationGate();

        backBtn.bringToFront();
        locAccessBtn.bringToFront();

        dimOverlay.setOnTouchListener((v, event) -> {
            float x = event.getRawX();
            float y = event.getRawY();

            int[] backLoc = new int[2];
            int[] locLoc = new int[2];

            backBtn.getLocationOnScreen(backLoc);
            locAccessBtn.getLocationOnScreen(locLoc);

            int backLeft = backLoc[0];
            int backTop = backLoc[1];
            int backRight = backLeft + backBtn.getWidth();
            int backBottom = backTop + backBtn.getHeight();

            int locLeft = locLoc[0];
            int locTop = locLoc[1];
            int locRight = locLeft + locAccessBtn.getWidth();
            int locBottom = locTop + locAccessBtn.getHeight();

            boolean insideBack = x >= backLeft && x <= backRight && y >= backTop && y <= backBottom;
            boolean insideLoc = x >= locLeft && x <= locRight && y >= locTop && y <= locBottom;

            return !(insideBack || insideLoc); // allow only those 2 buttons
        });
    }
}