package com.survice.electrofix;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
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

public class CustomerLocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference customerLocationRef;
    private GeoFire geoFire;
    private String customerID = "customer_123"; // *🔥 Replace with actual customer ID*

    @Override
    public void onCreate() {
        super.onCreate();

        customerLocationRef = FirebaseDatabase.getInstance().getReference("Customers_Location");
        geoFire = new GeoFire(customerLocationRef);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000) // 🔥 10 সেকেন্ডে 1 বার লোকেশন আপডেট হবে
                .setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    Log.d("CustomerLocation", "Customer Location: " + location.getLatitude() + ", " + location.getLongitude());
                    updateLocationToFirebase(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateLocationToFirebase(Location location) {
        geoFire.setLocation(customerID, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
            if (error != null) {
                Log.e("CustomerLocation", "Error updating location: " + error.getMessage());
            } else {
                Log.d("CustomerLocation", "Location updated successfully!");
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}