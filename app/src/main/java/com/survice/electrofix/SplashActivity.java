package com.survice.electrofix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

public class SplashActivity extends BaseActivity {
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 *Status Bar এবং Notification Bar হাইড করা*
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splashvideo);

        videoView = findViewById(R.id.videoView);
        Uri videoPath = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.setVideoURI(videoPath);

        videoView.setOnCompletionListener(mp -> navigateToNextScreen()); // ভিডিও শেষ হলে পরবর্তী স্ক্রিনে যাবে

        videoView.start();
    }

    private void navigateToNextScreen() {
        // 🔹 *SharedPreferences থেকে লগইন স্ট্যাটাস চেক করা*
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // যদি লগইন করা থাকে, MainActivity তে যাবে
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // 💡 **এই লাইনটি পরিবর্তন করা হয়েছে:**
            // যদি লগইন করা না থাকে, তাহলেও MainActivity তে যাবে
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }

        finish(); // স্প্ল্যাশ স্ক্রিন বন্ধ করবে
    }
}