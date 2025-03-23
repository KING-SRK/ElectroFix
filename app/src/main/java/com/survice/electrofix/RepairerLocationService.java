package com.survice.electrofix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RepairerLocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseReference databaseReference;
    private GeoFire geoFire;
    private String repairerID = "repairer_123"; // এখানে ডাইনামিকভাবে Repairer ID সেট করো

    @Override
    public void onCreate() {
        super.onCreate();

        databaseReference = FirebaseDatabase.getInstance().getReference("Repairers_Location");
        geoFire = new GeoFire(databaseReference);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // 🔥 Repairer-এর লোকেশন Firebase-এ GeoFire ব্যবহার করে আপলোড করা হচ্ছে
                    geoFire.setLocation(repairerID, new GeoLocation(latitude, longitude));

                }
            }
        };

        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10000) // প্রতি ১০ সেকেন্ডে লোকেশন আপডেট
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}