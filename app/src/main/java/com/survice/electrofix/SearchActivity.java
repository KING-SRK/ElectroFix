package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter serviceAdapter;
    private List<Service> serviceList;
    private List<Service> allServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchView searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.recycler_search_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allServices = new ArrayList<>();
        loadAllServices();
        serviceList = new ArrayList<>(allServices);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        serviceAdapter = new ServiceAdapter(serviceList, this, service -> {
            Intent intent = new Intent(SearchActivity.this, ServiceDetailActivity.class);
            intent.putExtra("service_name", service.getServiceName());
            intent.putExtra("service_price", service.getServicePrice());
            intent.putExtra("service_image", service.getServiceIcon());
            startActivity(intent);
        }, false);  // এখানে false দিবে


        recyclerView.setAdapter(serviceAdapter);

        // SearchView আইকন ও টেক্সট কালার সেট করা (Dark/Light মোড অনুযায়ী)
        setSearchViewColors(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Submit এ কোনো স্পেশাল কাজ না করলে false দিন
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterServices(newText);
                return true;
            }
        });
    }

    private void loadAllServices() {
        // AC Repair
        allServices.add(new Service("AC Gas Refill", "₹50", R.drawable.ac_gas_refill));
        allServices.add(new Service("AC Cleaning", "₹30", R.drawable.ac_cleaning));
        allServices.add(new Service("AC Installation", "₹80", R.drawable.ac_installation));

        // Computer Repair
        allServices.add(new Service("Computer Software Installation", "₹20", R.drawable.software_installation));
        allServices.add(new Service("Computer Screen Replacement", "₹100", R.drawable.screen_replacement));
        allServices.add(new Service("Computer Keyboard Repair", "₹40", R.drawable.keyboard_rapair));

        // Washing Machine Repair
        allServices.add(new Service("Washing Machine Drum Cleaning", "₹35", R.drawable.drum_cleaning));
        allServices.add(new Service("Washing Machine Water Pump Fix", "₹60", R.drawable.water_pump_fix));
        allServices.add(new Service("Washing Machine Motor Replacement", "₹90", R.drawable.motor_replacement));

        // Laptop Repair
        allServices.add(new Service("Laptop Battery Replacement", "₹70", R.drawable.laptop_battery));
        allServices.add(new Service("Laptop Motherboard Repair", "₹120", R.drawable.laptop_motherboard));
        allServices.add(new Service("Laptop Software Installation", "₹50", R.drawable.software_installation));

        // TV Repair
        allServices.add(new Service("TV Screen Fix", "₹150", R.drawable.tv_screen_fix));
        allServices.add(new Service("TV Remote Issue", "₹20", R.drawable.tv_remote_fix));
        allServices.add(new Service("TV Speaker Repair", "₹60", R.drawable.tv_speaker_fix));

        // Mobile Phone Repair
        allServices.add(new Service("Mobile Phone Screen Replacement", "₹90", R.drawable.mobile_screen_replacement));
        allServices.add(new Service("Mobile Phone Battery Change", "₹70", R.drawable.mobile_battery_change));
        allServices.add(new Service("Mobile Phone Charging Port Repair", "₹40", R.drawable.mobile_charging_port));

        // Fridge Repair
        allServices.add(new Service("Fridge Gas Filling", "₹85", R.drawable.fridge_gas_filling));
        allServices.add(new Service("Fridge Compressor Repair", "₹140", R.drawable.fridge_compressor));
        allServices.add(new Service("Fridge Thermostat Replace", "₹60", R.drawable.fridge_thermostat));

        // Fan Repair
        allServices.add(new Service("Fan Blade Replacement", "₹25", R.drawable.fan_blade));
        allServices.add(new Service("Fan Motor Rewinding", "₹55", R.drawable.fan_motor));
        allServices.add(new Service("Fan Switch Fix", "₹15", R.drawable.fan_switch));

        // Water Purifier Repair
        allServices.add(new Service("Water Purifier Filter Change", "₹50", R.drawable.purifier_filter));
        allServices.add(new Service("Water Purifier Leakage Fix", "₹30", R.drawable.purifier_leak));
        allServices.add(new Service("Water Purifier Pump Repair", "₹70", R.drawable.purifier_pump));
    }

    private void filterServices(String query) {
        List<Service> filtered = new ArrayList<>();
        for (Service service : allServices) {
            if (service.getServiceName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(service);
            }
        }
        serviceList.clear();
        serviceList.addAll(filtered);
        serviceAdapter.notifyDataSetChanged();
    }

    private void setSearchViewColors(SearchView searchView) {
        // SearchView এর EditText আইডি সঠিকভাবে নেওয়া
        int searchEditTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = searchView.findViewById(searchEditTextId);

        // SearchView এর আইকন আইডি নেওয়া
        int searchIconId = searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchIcon = searchView.findViewById(searchIconId);

        int closeIconId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeIcon = searchView.findViewById(closeIconId);

        int textColor = ContextCompat.getColor(this, R.color.search_text_color);
        int iconColor = ContextCompat.getColor(this, R.color.search_icon_color);

        if (searchEditText != null) {
            searchEditText.setTextColor(textColor);
            searchEditText.setHintTextColor(textColor);
        } else {
            Log.e("SearchActivity", "SearchView EditText is null");
        }

        if (searchIcon != null) {
            searchIcon.setColorFilter(iconColor);
        } else {
            Log.e("SearchActivity", "SearchView searchIcon is null");
        }

        if (closeIcon != null) {
            closeIcon.setColorFilter(iconColor);
        } else {
            Log.e("SearchActivity", "SearchView closeIcon is null");
        }
    }
}
