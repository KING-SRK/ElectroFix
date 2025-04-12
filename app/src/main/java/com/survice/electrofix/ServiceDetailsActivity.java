package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ServiceDetailsActivity extends BaseActivity {

    private ImageView imgService;
    private TextView tvServiceName, tvServicePrice, tvServiceDescription;
    private Button btnBookService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_details_activity);

        imgService = findViewById(R.id.img_service_detail);
        tvServiceName = findViewById(R.id.tv_service_detail_name);
        tvServicePrice = findViewById(R.id.tv_service_detail_price);
        tvServiceDescription = findViewById(R.id.tv_service_description);
        btnBookService = findViewById(R.id.btn_book_service);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Intent থেকে সার্ভিসের তথ্য নেওয়া
        Intent intent = getIntent();
        String serviceName = intent.getStringExtra("service_name");
        String servicePrice = intent.getStringExtra("service_price");
        int serviceImage = intent.getIntExtra("service_image", R.drawable.repairer_profile_icon);

        // UI-তে সেট করা
        tvServiceName.setText(serviceName);
        tvServicePrice.setText("Price: " + servicePrice);
        imgService.setImageResource(serviceImage);

        // সার্ভিসের বিবরণ (ডেমো টেক্সট)
        tvServiceDescription.setText("This is a detailed description of " + serviceName + ". Here you can add more information about the service.");

        // "Book Service" বাটনে ক্লিক করলে বুকিং পেজে যাবে
        btnBookService.setOnClickListener(v -> {
            Intent bookingIntent = new Intent(ServiceDetailsActivity.this, BookingActivity.class);
            bookingIntent.putExtra("service_name", serviceName);
            bookingIntent.putExtra("service_price", servicePrice);
            startActivity(bookingIntent);
        });
    }
}