package com.survice.electrofix;

import android.app.Dialog; import android.content.Context; import android.content.Intent; import android.content.res.Configuration; import android.net.ConnectivityManager; import android.net.NetworkInfo; import android.os.Bundle; import android.os.Handler; import android.view.View; import android.view.animation.Animation; import android.view.animation.AnimationUtils; import android.widget.Button; import android.widget.ImageView; import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

public class NoNetworkActivity extends BaseActivity {

    private Button retryButton, quitButton;
    private ImageView noInternetImage;
    private Dialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);

        // UI Elements
        retryButton = findViewById(R.id.retryButton);
        quitButton = findViewById(R.id.quitButton);
        noInternetImage = findViewById(R.id.noInternetImage);

        // Fade-in Animation for ImageView
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        noInternetImage.startAnimation(fadeIn);

        // ডার্ক মোড চেক করে ইমেজ আপডেট করা
        updateImageForTheme();

        // Progress Dialog তৈরি করা হচ্ছে
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);

        // Retry Button Click Listener
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show(); // Progress Dialog দেখানো শুরু করবে

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss(); // Progress Dialog বন্ধ করবে
                        if (isInternetAvailable()) {
                            Intent intent = new Intent(NoNetworkActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Snackbar.make(v, "No Internet Connection!", Snackbar.LENGTH_LONG)
                                    .setAction("Retry", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            retryButton.performClick();
                                        }
                                    }).show();
                        }
                    }
                }, 2000);
            }
        });

        // Quit Button Click Listener
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
    }

    // ইন্টারনেট চেক করার ফাংশন
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    // ডার্ক মোড হলে ভিন্ন ইমেজ সেট করার ফাংশন
    private void updateImageForTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            noInternetImage.setImageResource(R.drawable.no_internet_dark);
        } else {
            noInternetImage.setImageResource(R.drawable.no_internet_light);
        }
    }

}