package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ServiceDetailActivity extends BaseActivity {

    private ImageView imgService;
    private TextView tvServiceName, tvServicePrice, tvServiceDescription;
    private Button btnBookService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        // View গুলো রেফারেন্স নেওয়া
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

        // UI-তে ডাটা সেট করা
        if (serviceName != null) {
            tvServiceName.setText(serviceName);
        }
        if (servicePrice != null) {
            tvServicePrice.setText("Price: " + servicePrice);
        }
        imgService.setImageResource(serviceImage);

        // সার্ভিসের বিস্তারিত বিবরণ (আপনি চাইলে এখানে ডায়নামিক করে নিতে পারেন)
        tvServiceDescription.setText("This is a detailed description of " + serviceName + ". Here you can add more information about the service.");

        // বুক সার্ভিস বাটনে ক্লিক ইভেন্ট
        btnBookService.setOnClickListener(v -> {
            Intent bookingIntent = new Intent(ServiceDetailActivity.this, BookingActivity.class);
            bookingIntent.putExtra("service_name", serviceName);
            bookingIntent.putExtra("service_price", servicePrice);
            startActivity(bookingIntent);
        });
    }
}
