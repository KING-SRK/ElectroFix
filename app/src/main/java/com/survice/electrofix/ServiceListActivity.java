package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

        // Intent থেকে ক্যাটাগরির নাম পাওয়া
        String category = getIntent().getStringExtra("category");
        tvCategoryTitle.setText(category + " Services");

        // সার্ভিস লোড
        serviceList = new ArrayList<>();
        loadServices(category);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // 🔹 Click listener সহ Adapter সেট করা
        serviceAdapter = new ServiceAdapter(serviceList, this, new ServiceAdapter.OnServiceClickListener() {
            @Override
            public void onServiceClick(Service service) {
                Toast.makeText(ServiceListActivity.this, "Selected: " + service.getServiceName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ServiceListActivity.this, ServiceDetailActivity.class);
                intent.putExtra("service_name", service.getServiceName());
                intent.putExtra("service_price", service.getServicePrice());
                intent.putExtra("service_icon", service.getServiceIcon());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(serviceAdapter);
    }

    private void loadServices(String category) {
        switch (category) {
            case "AC Repair":
                serviceList.add(new Service("AC Gas Refill", "₹50", R.drawable.ac_gas_refill));
                serviceList.add(new Service("AC Cleaning", "₹30", R.drawable.ac_cleaning));
                serviceList.add(new Service("AC Installation", "₹80", R.drawable.ac_installation));
                break;

            case "Computer Repair":
                serviceList.add(new Service("Software Installation", "₹20", R.drawable.software_installation));
                serviceList.add(new Service("Screen Replacement", "₹100", R.drawable.screen_replacement));
                serviceList.add(new Service("Keyboard Repair", "₹40", R.drawable.keyboard_rapair));
                break;

            case "Washing Machine Repair":
                serviceList.add(new Service("Drum Cleaning", "₹35", R.drawable.drum_cleaning));
                serviceList.add(new Service("Water Pump Fix", "₹60", R.drawable.water_pump_fix));
                serviceList.add(new Service("Motor Replacement", "₹90", R.drawable.motor_replacement));
                break;

            case "Laptop Repair":
                serviceList.add(new Service("Battery Replacement", "₹70", R.drawable.laptop_battery));
                serviceList.add(new Service("Motherboard Repair", "₹120", R.drawable.laptop_motherboard));
                serviceList.add(new Service("Software Installation", "₹50", R.drawable.software_installation));
                break;

            case "TV Repair":
                serviceList.add(new Service("Screen Fix", "₹150", R.drawable.tv_screen_fix));
                serviceList.add(new Service("Remote Issue", "₹20", R.drawable.tv_remote_fix));
                serviceList.add(new Service("Speaker Repair", "₹60", R.drawable.tv_speaker_fix));
                break;

            case "Mobile Phone Repair":
                serviceList.add(new Service("Screen Replacement", "₹90", R.drawable.mobile_screen_replacement));
                serviceList.add(new Service("Battery Change", "₹70", R.drawable.mobile_battery_change));
                serviceList.add(new Service("Charging Port Repair", "₹40", R.drawable.mobile_charging_port));
                break;

            case "Fridge Repair":
                serviceList.add(new Service("Gas Filling", "₹85", R.drawable.fridge_gas_filling));
                serviceList.add(new Service("Compressor Repair", "₹140", R.drawable.fridge_compressor));
                serviceList.add(new Service("Thermostat Replace", "₹60", R.drawable.fridge_thermostat));
                break;

            case "Fan Repair":
                serviceList.add(new Service("Blade Replacement", "₹25", R.drawable.fan_blade));
                serviceList.add(new Service("Motor Rewinding", "₹55", R.drawable.fan_motor));
                serviceList.add(new Service("Switch Fix", "₹15", R.drawable.fan_switch));
                break;

            case "Water Purifier Repair":
                serviceList.add(new Service("Filter Change", "₹50", R.drawable.purifier_filter));
                serviceList.add(new Service("Leakage Fix", "₹30", R.drawable.purifier_leak));
                serviceList.add(new Service("Pump Repair", "₹70", R.drawable.purifier_pump));
                break;

            default:
                serviceList.add(new Service("No services available", "₹0", R.drawable.no_image));
                break;
        }
    }
}
