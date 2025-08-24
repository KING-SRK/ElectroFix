package com.survice.electrofix;

import android.graphics.Color;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ImageButton btnAc, btnPC, btnPhone, btnFan, btnWashingMachine, btnTv, btnXerox, btnFridge;
    private ImageButton btnPayment, btnTracking, btnHelpSupport;
    private ImageButton btnNotification;
    private ImageButton homeButton, categoryButton, settingsButton;
    private ImageButton customerProfileButton;
    private TextView customerProfileText;
    private ProgressBar loadingProgressBar;
    private SearchView searchView;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String currentUserType;
    private SharedPreferences sharedPreferences;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private BannerAdapter bannerAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isBannerTouched = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final Runnable autoSlideRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager.getAdapter() != null && !isBannerTouched) {
                int currentItem = viewPager.getCurrentItem();
                int totalItems = viewPager.getAdapter().getItemCount();
                int nextItem = (currentItem + 1) % totalItems;

                viewPager.setCurrentItem(nextItem, true);
            }
            handler.postDelayed(this, 5000);
        }
    };

    // 1️⃣ Declare once
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Network check
        if (!isConnected()) {
            startActivity(new Intent(MainActivity.this, NoNetworkActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // 2️⃣ Initialize in onCreate
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check permission at launch
        checkLocationPermission();

        // ✅ initialize your sharedPreferences like this
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        initializeUI();
        setupButtonClickListeners();
        initMainButtons();
        checkCurrentUser();
        checkLocationPermission();  // <-- Now it's safe!


        // --- START OF IMAGE SLIDER SETUP ---
        viewPager = findViewById(R.id.image_slider_viewpager);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(1);

        tabLayout = findViewById(R.id.tab_layout_indicator);

        List<Integer> imageList = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4,
                R.drawable.banner5,
                R.drawable.banner6,
                R.drawable.banner7,
                R.drawable.banner8
        );

        bannerAdapter = new BannerAdapter(imageList, new BannerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int imageResId) {
                // Click handling logic
            }
        });
        viewPager.setAdapter(bannerAdapter);

        if (tabLayout != null) {
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
        }

        int pageMarginPx = getResources().getDimensionPixelOffset(R.dimen.banner_page_margin);
        int pagerWidth = getResources().getDimensionPixelOffset(R.dimen.banner_viewpager_width);
        viewPager.setPadding(0, 0, 0, 0);
        viewPager.setPageTransformer((page, position) -> {
            float scale = 1 - 0.1f * Math.abs(position);
            page.setScaleY(scale);
            page.setAlpha(1 - 0.3f * Math.abs(position));
        });

        final Handler sliderHandler = new Handler(Looper.getMainLooper());
        final Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextPos = (viewPager.getCurrentItem() + 1) % imageList.size();
                viewPager.setCurrentItem(nextPos, true);
                sliderHandler.postDelayed(this, 3000);
            }
        };

        sliderHandler.postDelayed(sliderRunnable, 3000);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
        // --- NEW: OnTouchListener for direct holding (pause on touch, resume on release) ---
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            private final int HOLD_DURATION = 300;
            private boolean isHolding = false;
            private Runnable holdRunnable;
            private Runnable resumeRunnable;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View currentBanner = viewPager.getChildAt(0);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.removeCallbacks(autoSlideRunnable);
                        isHolding = false;

                        if (resumeRunnable != null) {
                            handler.removeCallbacks(resumeRunnable);
                        }

                        holdRunnable = () -> {
                            isBannerTouched = true;
                            isHolding = true;

                            if (currentBanner != null) {
                                currentBanner.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
                                currentBanner.setElevation(10f);
                            }

                            viewPager.invalidate();
                        };
                        handler.postDelayed(holdRunnable, HOLD_DURATION);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getX() - event.getHistoricalX(0));
                        if (deltaX > 30 && !isHolding) {
                            handler.removeCallbacks(holdRunnable);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(holdRunnable);

                        if (isBannerTouched) {
                            isBannerTouched = false;

                            if (currentBanner != null) {
                                currentBanner.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                                currentBanner.setElevation(0f);
                            }

                            viewPager.invalidate();
                        }
                        resumeRunnable = () -> {
                            if (!isBannerTouched) {
                                handler.postDelayed(autoSlideRunnable, 0);
                            }
                        };
                        handler.postDelayed(resumeRunnable, 5000);
                        break;
                }
                return false;
            }
        });
        // Register OnPageChangeCallback for pause/resume logic based on user interaction (dragging/scrolling)
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    // User has started dragging (or fling) the pager
                    isBannerTouched = true; // Treat dragging as "touched" to keep effects
                    handler.removeCallbacks(autoSlideRunnable); // Pause auto-sliding immediately
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // Pager has settled (user released or animation finished)
                    // Only resume if the user is NOT currently touching the banner AND if it just came from a drag
                    // We also want to re-post if it just settled from an auto-scroll
                    if (!isBannerTouched) { // Only resume if user is NOT actively touching
                        handler.removeCallbacks(autoSlideRunnable);
                        handler.postDelayed(autoSlideRunnable, 5000); // Normal auto-slide interval
                    }
                }
            }
            @Override
            public void onPageSelected(int position) {
                // When a new page is selected, if not due to a user drag, ensure auto-slide is timed
                // This will re-post the runnable if it's an auto-slide or a quick tap/release
                if (!isBannerTouched && viewPager.getScrollState() == ViewPager2.SCROLL_STATE_IDLE) {
                    handler.removeCallbacks(autoSlideRunnable);
                    handler.postDelayed(autoSlideRunnable, 5000);
                }
            }
        });

        SearchView searchView = findViewById(R.id.searchView);
        styleSearchView(searchView);  // <-- Call it right after you get the view

        // Your other search listeners here
        if (searchView != null) {
            searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                    searchView.clearFocus();
                }
            });
        }
        customerProfileButton = findViewById(R.id.customer_profile_button);  // ✅ must match XML id
        checkCurrentUser();

        customerProfileText = findViewById(R.id.customer_profile_text
        );  // ✅ ID must match XML
        checkCurrentUser();
        // --- END OF IMAGE SLIDER SETUP ---
    } // End of onCreate


    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Start automatic sliding when the activity is visible, unless already touched
        if (!isBannerTouched) {
            handler.postDelayed(autoSlideRunnable, 3000); // Start after 3 seconds
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop automatic sliding when the activity is not visible to prevent memory leaks
        handler.removeCallbacks(autoSlideRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure all callbacks are removed to prevent memory leaks
        handler.removeCallbacks(autoSlideRunnable);
    }

    private void initializeUI() {
        searchView = findViewById(R.id.searchView);
        SearchView searchView = findViewById(R.id.searchView);
        homeButton = findViewById(R.id.home_button);
        categoryButton = findViewById(R.id.category_button);
        settingsButton = findViewById(R.id.settings_button);
        btnNotification = findViewById(R.id.btnNotification);
        btnAc = findViewById(R.id.btnAC);
        btnPC = findViewById(R.id.btnPC);
        btnPhone = findViewById(R.id.btnphone);
        btnFan = findViewById(R.id.btnfan);
        btnWashingMachine = findViewById(R.id.btnwashingmachine);
        btnTv = findViewById(R.id.btntv);
        btnXerox = findViewById(R.id.btnxerox);
        btnFridge = findViewById(R.id.btnfridge);
        btnPayment = findViewById(R.id.btnPayment);
        btnTracking = findViewById(R.id.btnTracking);
        btnHelpSupport = findViewById(R.id.btnHelpSupport);
        customerProfileButton = findViewById(R.id.customer_profile_button);
        customerProfileText = findViewById(R.id.customer_profile_text);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        // themeSwitch = findViewById(R.id.themeSwitch); // ডার্ক মোড সুইচের জন্য findViewById কল সরানো হয়েছে
        // themeSwitchLabel = findViewById(R.id.themeSwitchLabel); // ডার্ক মোড লেবেলের জন্য findViewById কল সরানো হয়েছে

        if (customerProfileButton != null) customerProfileButton.setVisibility(View.GONE);
        if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);

        int searchPlateId = searchView.getContext()
                .getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.TRANSPARENT); // removes black line
        }

        int searchTextId = searchView.getContext()
                .getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = searchView.findViewById(searchTextId);
        if (searchText != null) {
            if (isDarkTheme()) {
                searchText.setHintTextColor(Color.WHITE);  // light hint in dark mode
                searchText.setTextColor(Color.WHITE);       // user typed text color
            } else {
                searchText.setHintTextColor(Color.BLACK);  // dark hint in light mode
                searchText.setTextColor(Color.BLACK);
            }
        }
    }

    private boolean isDarkTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
    private void styleSearchView(SearchView searchView) {
        if (searchView == null) return;

        // 1. Remove inner underline (search_plate background)
        int plateId = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View plate = searchView.findViewById(plateId);
        if (plate != null) plate.setBackgroundColor(Color.TRANSPARENT);

        // 2. Control hint text + user text color
        int textId = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = searchView.findViewById(textId);
        if (searchText != null) {
            if (isDarkTheme()) {
                searchText.setHintTextColor(Color.WHITE); // hint text color in dark theme
                searchText.setTextColor(Color.WHITE);       // typed text color
            } else {
                searchText.setHintTextColor(Color.BLACK);  // hint text color in light theme
                searchText.setTextColor(Color.BLACK);       // typed text color
            }
        }

        // 3. Control magnifying glass color
        int magId = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        ImageView magImage = searchView.findViewById(magId);
        if (magImage != null) {
            if (isDarkTheme()) {
                magImage.setColorFilter(Color.WHITE); // icon tint in dark theme
            } else {
                magImage.setColorFilter(Color.BLACK); // icon tint in light theme
            }
        }
    }
    private void initMainButtons(){
        btnAc.setOnClickListener(v -> openServiceList("AC Repair"));
        btnPC.setOnClickListener(v -> openServiceList("Computer Repair"));
        btnWashingMachine.setOnClickListener(v -> openServiceList("Washing Machine Repair"));
        btnTv.setOnClickListener(v -> openServiceList("TV Repair"));
        btnPhone.setOnClickListener(v -> openServiceList("Mobile Phone Repair"));
        btnFridge.setOnClickListener(v -> openServiceList("Fridge Repair"));
        btnFan.setOnClickListener(v -> openServiceList("Fan Repair"));
    }
    private void openServiceList(String categoryName) {
        Intent intent = new Intent(MainActivity.this, ServiceListActivity.class);
        intent.putExtra("category", categoryName);
        startActivity(intent);
    }

    private void setupButtonClickListeners() {
        // ✅ Payment (restricted)
        if (btnPayment != null) {
            btnPayment.setOnClickListener(v -> {
                if (isLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, PaymentActivity.class));
                } else {
                    showLoginPrompt("Login required to use Payments.");
                }
            });
        }

        // ✅ Tracking (restricted)
        if (btnTracking != null) {
            btnTracking.setOnClickListener(v -> {
                if (isLoggedIn()) {
                    Intent intent = new Intent(MainActivity.this, TrackingActivity.class);
                    intent.putExtra("bookingId", "YOUR_BOOKING_ID"); // pass actual bookingId
                    startActivity(intent);
                } else {
                    showLoginPrompt("Please login to track your orders.");
                }
            });
        }

        // ✅ Profile (restricted)
        if (customerProfileButton != null) {
            customerProfileButton.setOnClickListener(v -> {
                if (isLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, CustomerProfileActivity.class));
                } else {
                    showLoginPrompt("Please login to access your profile.");
                }
            });
        }
// ✅ Notification (can be free access or restricted, your choice)
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
            });
        }
        // ✅ Help & Support (can be open without login)
        if (btnHelpSupport != null) {
            btnHelpSupport.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, HelpSupportActivity.class));
            });
        }

        // ✅ Categories (can explore without login)
        if (categoryButton != null) {
            categoryButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, CategoryActivity.class));
            });
        }

        // ✅ Settings (can be open without login)
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            });
        }
    }
    private boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
    private void showLoginPrompt(String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Login Required")
                .setMessage(message)
                .setPositiveButton("Login / Signup", (dialog, which) -> {
                    startActivity(new Intent(MainActivity.this, SignupActivity.class));
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Not logged in → show button with login popup
            customerProfileButton.setVisibility(View.VISIBLE);
            customerProfileText.setVisibility(View.VISIBLE);

            customerProfileButton.setOnClickListener(v ->
                    showLoginPrompt("Please login to access your profile.")
            );
        } else {
            // Logged in → open profile directly
            customerProfileButton.setVisibility(View.VISIBLE);
            customerProfileText.setVisibility(View.VISIBLE);

            customerProfileButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CustomerProfileActivity.class);
                startActivity(intent);
            });
        }

        // Always hide the loader once we checked
        loadingProgressBar.setVisibility(View.GONE);
    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);

        } else {
            // Already granted
            getUserLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        if (!isLocationEnabled()) {
            // ❌ Remove the toast here
            // ✅ Just return silently without showing anything
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);

                    } else {
                        // 👉 Optional: only show toast if GPS is ON but still no location
                        Toast.makeText(this,
                                "Unable to fetch location right now. Try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
