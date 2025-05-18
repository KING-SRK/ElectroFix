package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private TextView tvHistory;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private List<String> recommendations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // লেআউট আইডি খুঁজে বের করা
        searchView = findViewById(R.id.search_view);
        tvHistory = findViewById(R.id.tv_history);
        recyclerView = findViewById(R.id.recycler_view);

        // রেকমেন্ডেশন ডেটা তৈরি
        recommendations = new ArrayList<>();
        recommendations.add("AC Repair");
        recommendations.add("Computer Repair");
        recommendations.add("Washing Machine Repair");
        recommendations.add("Refrigerator Repair");
        recommendations.add("TV Repair");

        // রিসাইক্লার ভিউ সেটআপ
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(recommendations);
        recyclerView.setAdapter(adapter);

        // সার্চবারে ইনপুটের জন্য লিসেনার
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tvHistory.setVisibility(View.VISIBLE);
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }
}