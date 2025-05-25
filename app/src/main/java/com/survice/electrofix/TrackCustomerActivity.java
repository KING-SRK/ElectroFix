package com.survice.electrofix;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TrackCustomerActivity extends BaseActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_REQUEST_CODE_RESOLUTION = 1002;

    private GoogleMap mMap;
    private Marker customerMarker, repairerMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private LatLng customerLatLng;
    private Polyline currentPolyline;

    private Button btnCustomer, btnRepairer;
    private TextView txtTimeEstimate;

    private static final String GOOGLE_API_KEY = "AIzaSyCuJ7OEIo7gmIQbTIBO-PTihjflx1qm7WY"; // নিজের API KEY ব্যবহার করো

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_customer);

        btnCustomer = findViewById(R.id.btnCustomer);
        btnRepairer = findViewById(R.id.btnRepairer);
        txtTimeEstimate = findViewById(R.id.txtTimeEstimate);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        double customerLatitude = getIntent().getDoubleExtra("latitude", 0);
        double customerLongitude = getIntent().getDoubleExtra("longitude", 0);
        customerLatLng = new LatLng(customerLatitude, customerLongitude);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map Fragment not found!", Toast.LENGTH_SHORT).show();
        }

        btnCustomer.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 17));
            }
        });

        btnRepairer.setOnClickListener(v -> {
            if (repairerMarker != null && mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(repairerMarker.getPosition(), 17));
            } else {
                Toast.makeText(this, "Repairer location not yet available", Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationSettings();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        if (customerLatLng != null) {
            customerMarker = mMap.addMarker(new MarkerOptions()
                    .position(customerLatLng)
                    .title("Customer Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            CameraPosition position = new CameraPosition.Builder()
                    .target(customerLatLng)
                    .zoom(17f)
                    .bearing(90f)
                    .tilt(30f)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }

        startLocationUpdates(); // 🟢 এই লাইনে যোগ করা হয়েছে
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(this, LOCATION_REQUEST_CODE_RESOLUTION);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        task.addOnSuccessListener(locationSettingsResponse -> startLocationUpdates()); // ✅ সঠিকভাবে শুরু
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(4000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null && mMap != null) {
                    updateRepairerMarker(location);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateRepairerMarker(Location location) {
        LatLng repairerLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (repairerMarker == null) {
            repairerMarker = mMap.addMarker(new MarkerOptions()
                    .position(repairerLatLng)
                    .title("Repairer Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else {
            repairerMarker.setPosition(repairerLatLng);
        }

        float[] results = new float[1];
        Location.distanceBetween(
                repairerLatLng.latitude, repairerLatLng.longitude,
                customerLatLng.latitude, customerLatLng.longitude,
                results
        );

        float distance = results[0];
        float speed = 5.0f;
        int time = (int) (distance / speed);
        int min = time / 60;
        int sec = time % 60;

        txtTimeEstimate.setText(String.format(Locale.getDefault(),
                "Estimated time: %d min %d sec", min, sec));

//        drawRoute(repairerLatLng, customerLatLng);
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        String url = String.format(Locale.getDefault(),
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&mode=driving&key=%s",
                origin.latitude, origin.longitude,
                destination.latitude, destination.longitude,
                GOOGLE_API_KEY);

        Log.d("RouteURL", url);

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject json = new JSONObject(response.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    String encodedPoints = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points");

                    List<LatLng> steps = decodePolyline(encodedPoints);

                    runOnUiThread(() -> {
                        if (steps != null && !steps.isEmpty()) {
                            if (currentPolyline != null) currentPolyline.remove();
                            currentPolyline = mMap.addPolyline(new PolylineOptions()
                                    .addAll(steps)
                                    .width(10f)
                                    .color(ContextCompat.getColor(this, R.color.blue))
                                    .geodesic(true));
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No routes found between current location and customer.", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error fetching route from API.", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng(lat / 1E5, lng / 1E5);
            poly.add(point);
        }
        return poly;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                }
            } else {
                Toast.makeText(this, "Location is required for tracking!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationSettings();
        } else {
            Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
