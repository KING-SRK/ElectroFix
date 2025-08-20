package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private ImageView btnClose, flashBtn, galleryBtn;
    private boolean isFlashOn = true;
    private static final int PICK_IMAGE_REQUEST = 101;
    private boolean alreadyScanned = false;
    private static final int CAMERA_PERMISSION_CODE = 123;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        btnClose = findViewById(R.id.btnClose);
        flashBtn = findViewById(R.id.flashbtn);
        galleryBtn = findViewById(R.id.galleryBtn);

        if (checkCameraPermission()) {
            setupQRScanner();
            setupButtons();
        } else {
            requestCameraPermission();
        }
    }

    private void setupQRScanner() {
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.setStatusText("");
        barcodeView.resume();
        barcodeView.setTorchOn();

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (alreadyScanned) return;

                String scannedData = result.getText();
                if (scannedData != null && scannedData.startsWith("upi://")) {
                    alreadyScanned = true;
                    barcodeView.pause();
                    handleUPIScan(scannedData);
                } else {
                    Toast.makeText(QRScannerActivity.this, "Not a valid UPI QR", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    private void setupButtons() {
        btnClose.setOnClickListener(v -> finish());

        flashBtn.setOnClickListener(v -> {
            if (isFlashOn) {
                barcodeView.setTorchOff();
                isFlashOn = false;
                flashBtn.setImageResource(R.drawable.ic_flash_off);
            } else {
                barcodeView.setTorchOn();
                isFlashOn = true;
                flashBtn.setImageResource(R.drawable.ic_flash_on);
            }
        });

        galleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupQRScanner();
                setupButtons();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void handleUPIScan(String data) {
        try {
            Uri uri = Uri.parse(data);
            String name = uri.getQueryParameter("pn");
            String upiId = uri.getQueryParameter("pa");
            String bank = uri.getQueryParameter("mc");

            if (upiId != null && !upiId.isEmpty()) {
                Intent intent = new Intent(QRScannerActivity.this, PayingAnyoneActivity.class);
                intent.putExtra("person_name", name != null ? name : "Unknown");
                intent.putExtra("upi_id", upiId);
                intent.putExtra("bank_name", bank != null ? bank : "Unknown Bank");
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid UPI QR: Missing UPI ID", Toast.LENGTH_SHORT).show();
                alreadyScanned = false;
                barcodeView.resume();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Invalid QR Format", Toast.LENGTH_SHORT).show();
            alreadyScanned = false;
            barcodeView.resume();
        }
    }

    private void decodeFromGallery(Bitmap bitmap) {
        BinaryBitmap binaryBitmap = new BinaryBitmap(
                new HybridBinarizer(new BitmapLuminanceSource(bitmap))
        );

        try {
            Result result = new QRCodeReader().decode(binaryBitmap);
            String data = result.getText();
            if (data != null && data.startsWith("upi://")) {
                handleUPIScan(data);
            } else {
                Toast.makeText(this, "Not a valid UPI QR", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "No valid QR found in image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        alreadyScanned = false;
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeView.pauseAndWait();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                decodeFromGallery(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}