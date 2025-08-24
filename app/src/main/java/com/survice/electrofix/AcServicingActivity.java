package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // Import AlertDialog for simpler dialogs
import androidx.cardview.widget.CardView;

public class AcServicingActivity extends BaseActivity { // Or AppCompatActivity
    private ImageView btnBack;
    private CardView generalServicesCard;
    private CardView installationCard;
    private CardView coolingIssuesCard;
    private CardView gasWorksCard;
    private CardView electricalCard, Compressoreworkscard, WaterLeakagecard, FanMotorServicescard, NoiseVibrationIssuescard;
    private CardView RemoteSensorIssuescard, DeepCleaningServicescard, SpecializedACServicescard,ValueAddedServicescard;
    private EditText etSearch;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();

        setContentView(R.layout.activity_ac_servicing);

        // Bind CardViews to their IDs from XML
        btnBack = findViewById(R.id.btnBack);
        generalServicesCard = findViewById(R.id.generalServicesCard);
        installationCard = findViewById(R.id.installationCard);
        coolingIssuesCard = findViewById(R.id.coolingIssuesCard);
        gasWorksCard = findViewById(R.id.gasWorksCard);
        electricalCard = findViewById(R.id.electricalCard);
        Compressoreworkscard = findViewById(R.id.Compressoreworkscard);
        WaterLeakagecard = findViewById(R.id.WaterLeakagecard);
        FanMotorServicescard = findViewById(R.id.FanMotorServicescard);
        NoiseVibrationIssuescard = findViewById(R.id.NoiseVibrationIssuescard);
        RemoteSensorIssuescard = findViewById(R.id.RemoteSensorIssuescard);
        DeepCleaningServicescard = findViewById(R.id.DeepCleaningServicescard);
        SpecializedACServicescard = findViewById(R.id.SpecializedACServicescard);
        ValueAddedServicescard = findViewById(R.id.ValueAddedServicescard);

        // Implement search functionality here if desired
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(AcServicingActivity.this, CategoryActivity.class));
        });
// ⭐ UPDATED PART: Pass data for each service
        generalServicesCard.setOnClickListener(v -> {
            navigateToBooking(
                    "General Services",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased" // A brief description for the booking page
            );
        });

        installationCard.setOnClickListener(v -> {
            navigateToBooking(
                    "Installation & Uninstallation",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });

        coolingIssuesCard.setOnClickListener(v -> {
            navigateToBooking(
                    "Cooling / Performance Issues",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });

        gasWorksCard.setOnClickListener(v -> {
            navigateToBooking(
                    "Gas & Refrigerant Works",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });

        electricalCard.setOnClickListener(v -> {
            navigateToBooking(
                    "Electrical & Wiring Repairs",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        Compressoreworkscard.setOnClickListener(v -> {
            navigateToBooking(
                    "Compressor Works",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        WaterLeakagecard.setOnClickListener(v -> {
            navigateToBooking(
                    " Water Leakage Problems",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        FanMotorServicescard.setOnClickListener(v -> {
            navigateToBooking(
                    "Fan & Motor Services",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        NoiseVibrationIssuescard.setOnClickListener(v -> {
            navigateToBooking(
                    "Noise & Vibration Issues",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        RemoteSensorIssuescard.setOnClickListener(v -> {
            navigateToBooking(
                    "Remote & Sensor Issues",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        DeepCleaningServicescard.setOnClickListener(v -> {
            navigateToBooking(
                    "Deep Cleaning Services",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        SpecializedACServicescard.setOnClickListener(v -> {
            navigateToBooking(
                    " Specialized AC Services",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });
        ValueAddedServicescard.setOnClickListener(v -> {
            navigateToBooking(
                    "Value-Added Services",
                    "AC Repair & Services",
                    "₹500.00",
                    "This Service Starting Charge Included In The Page, If Working Load Became High Then Charge Will Be Increased"
            );
        });

    }
    /**
     * Helper method to create an Intent and start BookingActivity with data.
     * This is the crucial part that passes the information.
     */
    private void navigateToBooking(String categoryTitle, String serviceTitle, String price, String priceDescription) {
        Intent intent = new Intent(AcServicingActivity.this, BookingActivity.class);
        // Ensure these keys exactly match the keys used in BookingActivity
        intent.putExtra("CATEGORY_TITLE", categoryTitle);
        intent.putExtra("SERVICE_TITLE", serviceTitle);
        intent.putExtra("PRICE", price);
        intent.putExtra("PRICE_DESCRIPTION", priceDescription);
        startActivity(intent);
    }

    // The showServiceDetailsDialog and other helper methods are no longer needed for this functionality,
    // as we're now navigating directly to the booking page with the data.
    // If you want to keep the dialogs for a preview, you can call them from a separate listener,
    // e.g., on a different button or long-click.


    // Existing hideSystemUI method
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}