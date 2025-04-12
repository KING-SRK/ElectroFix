package com.survice.electrofix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

public class CategoryActivity extends BaseActivity {

    private ImageButton homeButton;
    private ImageButton btnAcRepair, btnComputerRepair, btnWashingMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 Title Bar Hide করা
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 🔹 Full Screen Mode (Status Bar + Navigation Bar Hide করা)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 🔹 DecorView ব্যবহার করে System UI লুকানো
        hideSystemUI();

        setContentView(R.layout.activity_category);

        // 🔹 Home Button
        homeButton = findViewById(R.id.home_button);

        // 🔹 Category Buttons
        btnAcRepair = findViewById(R.id.btn_ac_repair);
        btnComputerRepair = findViewById(R.id.btn_computer_repair);
        btnWashingMachine = findViewById(R.id.btn_washing_machine);

        // 🔹 Home Button Click Listener
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 🔹 AC Repair Button Click
        btnAcRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openServiceList("AC Repair");
            }
        });

        // 🔹 Computer Repair Button Click
        btnComputerRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openServiceList("Computer Repair");
            }
        });

        // 🔹 Washing Machine Button Click
        btnWashingMachine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openServiceList("Washing Machine Repair");
            }
        });
    }

    // 🔹 Intent Method: Service List Page এ যাওয়ার জন্য
    private void openServiceList(String categoryName) {
        Intent intent = new Intent(CategoryActivity.this, ServiceListActivity.class);
        intent.putExtra("category", categoryName);
        startActivity(intent);
    }

    // 🔹 Method: System UI লুকানো (Navigation Bar + Status Bar)
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    // 🔹 যখনই UI তে পরিবর্তন হবে তখনও System UI লুকাবে
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}