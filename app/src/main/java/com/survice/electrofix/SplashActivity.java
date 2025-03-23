package com.survice.electrofix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashvideo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoView = findViewById(R.id.videoView);
        Uri videoPath = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.setVideoURI(videoPath);

        videoView.setOnCompletionListener(mp -> navigateToNextScreen()); // ভিডিও শেষ হলে পরবর্তী স্ক্রিনে যাবে

        videoView.start();
    }

    private void navigateToNextScreen() {
        // SharedPreferences থেকে লগইন স্ট্যাটাস চেক করা
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE); // *ফিক্স করা হয়েছে*
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class)); // লগইন থাকলে MainActivity
        } else {
            startActivity(new Intent(SplashActivity.this, ChoiceActivity.class)); // না থাকলে UserSelectionActivity
        }

        finish(); // স্প্ল্যাশ স্ক্রিন বন্ধ করবে
    }
}