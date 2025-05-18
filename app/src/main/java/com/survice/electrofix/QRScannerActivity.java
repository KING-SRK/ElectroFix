package com.survice.electrofix;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.survice.electrofix.R;

public class QRScannerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        Toast.makeText(this, "QR Scanner Page (UI Only)", Toast.LENGTH_SHORT).show();

        // Later: Add real scanner logic here
    }
}