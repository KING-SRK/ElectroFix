package com.survice.electrofix;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class TrackingMapActivity extends BaseActivity implements SensorEventListener {

    private MapView mapView;
    private ImageView btnBack;
    private MyLocationNewOverlay myLocationOverlay;
    private CompassOverlay compassOverlay;
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float[] gravity, geomagnetic;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // 🔥 Firebase & GeoFire Setup
    private DatabaseReference databaseReference;
    private GeoFire geoFire;
    private String repairerID = "repairer_123"; // Repairer-এর ID (ডাইনামিক করতে পারো)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_map);

        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        startService(serviceIntent);

        // ✅ Repairer-এর লাইভ লোকেশন আপডেট চালু করা
        startService(new Intent(this, LocationUpdateService.class));
        findNearbyRepairers(); // 🔥 কাছাকাছি Repairer-দের দেখাও

        startService(new Intent(this, CustomerLocationService.class)); // 🔥 Customer-এর লোকেশন Firebase-এ আপডেট হবে



        // ✅ OSMDroid ম্যাপ সেটআপ
        Configuration.getInstance().setUserAgentValue(getApplicationContext().getPackageName());
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // ✅ ম্যাপে ডিফল্ট লোকেশন সেট করা (ভারতের)
        GeoPoint startPoint = new GeoPoint(20.5937, 78.9629);
        mapView.getController().setZoom(5.5);
        mapView.getController().setCenter(startPoint);

        // ✅ ম্যাপ টাইল রিপিট হওয়া বন্ধ করা
        mapView.getOverlayManager().getTilesOverlay().setUseDataConnection(false);
        mapView.setScrollableAreaLimitLatitude(85.0511, -85.0511, 0);
        mapView.setMinZoomLevel(4.0);

        // ✅ Firebase & GeoFire Initialize
        databaseReference = FirebaseDatabase.getInstance().getReference("Repairers_Location");
        geoFire = new GeoFire(databaseReference);

        // ✅ লোকেশন পারমিশন চেক করা
        checkLocationPermission();



        // ✅ ব্যাক বাটন ফাংশনালিটি
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ✅ সেন্সর সেটআপ (কম্পাসের জন্য)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // 🔥 Firebase থেকে Repairer-এর লোকেশন লোড করা
        loadRepairerLocation();
    }

    // ✅ লোকেশন পারমিশন চেক করা এবং অনুমতি না থাকলে চাইতে হবে
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            addUserLocation(); // পারমিশন থাকলে লোকেশন দেখাও
        }
    }

    // ✅ পারমিশন রেজাল্ট চেক করা
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addUserLocation();
            }
        }
    }

    // ✅ ইউজারের লাইভ লোকেশন এবং কম্পাস দেখানোর ফাংশন
    private void addUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // ✅ লোকেশন ইনিশিয়ালাইজ করা হচ্ছে
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);
        mapView.invalidate();

        // ✅ কম্পাস যুক্ত করা হচ্ছে
        setupCompass();
    }

    // ✅ কম্পাস সেটআপ
    private void setupCompass() {
        compassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        compassOverlay.enableCompass();
        compassOverlay.setCompassCenter(320, 45);
        mapView.getOverlays().add(compassOverlay);
    }

    // ✅ Firebase থেকে Repairer-এর লোকেশন রিয়েল-টাইমে লোড করার ফাংশন
    private void loadRepairerLocation() {
        geoFire.getLocation(repairerID, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    double lat = location.latitude;
                    double lng = location.longitude;

                    // 🔥 OpenStreetMap-এ Marker আপডেট করা হচ্ছে
                    GeoPoint repairerLocation = new GeoPoint(lat, lng);
                    mapView.getController().setCenter(repairerLocation);
                    mapView.getController().setZoom(15.0);

                    Marker repairerMarker = new Marker(mapView);
                    repairerMarker.setPosition(repairerLocation);
                    repairerMarker.setTitle("Repairer");
                    mapView.getOverlays().add(repairerMarker);
                    mapView.invalidate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 🔥 Error Handle
            }
        });
    }

    // ✅ Compass Orientation Sensor Data (SensorEventListener Implementation)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuthInDegrees = (float) Math.toDegrees(orientation[0]);
                azimuthInDegrees = (azimuthInDegrees + 360) % 360;

                mapView.invalidate();
            }
        }
    }

    private void findNearbyRepairers() {
        double userLat = 22.5726;  // 🔥 Replace with actual user latitude
        double userLng = 88.3639;  // 🔥 Replace with actual user longitude
        double searchRadius = 10.0; // 🔥 ১০ কিলোমিটার রেঞ্জ

        NearbyRepairers nearbyRepairers = new NearbyRepairers(new NearbyRepairers.NearbyRepairerListener() {
            @Override
            public void onRepairerFound(String repairerID, double lat, double lng) {
                Log.d("TrackingMap", "Nearby Repairer: " + repairerID + " at (" + lat + ", " + lng + ")");
                // 🔥 OpenStreetMap-এ Repairer-এর Marker যোগ করা
                addRepairerMarker(lat, lng);
            }

            @Override
            public void onRepairerRemoved(String repairerID) {
                Log.d("TrackingMap", "Repairer Removed: " + repairerID);
            }
        });

        nearbyRepairers.findNearbyRepairers(userLat, userLng, searchRadius);
    }
    private void addRepairerMarker(double lat, double lng) {
        GeoPoint repairerLocation = new GeoPoint(lat, lng);
        Marker repairerMarker = new Marker(mapView);
        repairerMarker.setPosition(repairerLocation);
        repairerMarker.setTitle("Nearby Repairer");
        mapView.getOverlays().add(repairerMarker);
        mapView.invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}