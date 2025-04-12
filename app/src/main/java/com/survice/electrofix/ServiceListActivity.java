package com.survice.electrofix;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private List<Service> serviceList;
    private TextView tvCategoryTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_list_activity);

        tvCategoryTitle = findViewById(R.id.tv_category_title);
        recyclerView = findViewById(R.id.recycler_service_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Intent থেকে ক্যাটাগরির নাম পাওয়া
        String category = getIntent().getStringExtra("category");
        tvCategoryTitle.setText(category + " Services");

        // ক্যাটাগরি অনুযায়ী সার্ভিস লোড করা
        serviceList = new ArrayList<>();
        loadServices(category);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        serviceAdapter = new ServiceAdapter(serviceList);
        recyclerView.setAdapter(serviceAdapter);
    }

    private void loadServices(String category) {
        if (category.equals("AC Repair")) {
            serviceList.add(new Service("AC Gas Refill", "50₹", R.drawable.ac_gas_refill));
            serviceList.add(new Service("AC Cleaning", "30₹", R.drawable.ac_cleaning));
            serviceList.add(new Service("AC Installation", "80₹", R.drawable.ac_installation));
        } else if (category.equals("Computer Repair")) {
            serviceList.add(new Service("Software Installation", "20₹", R.drawable.software_installation));
            serviceList.add(new Service("Screen Replacement", "100₹", R.drawable.screen_replacement));
            serviceList.add(new Service("Keyboard Repair", "40₹", R.drawable.keyboard_rapair));
        } else if (category.equals("Washing Machine Repair")) {
            serviceList.add(new Service("Drum Cleaning", "35₹", R.drawable.drum_cleaning));
            serviceList.add(new Service("Water Pump Fix", "60₹", R.drawable.water_pump_fix));
            serviceList.add(new Service("Motor Replacement", "90₹", R.drawable.motor_replacement));
        }
    }
}