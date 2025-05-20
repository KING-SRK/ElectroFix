package com.survice.electrofix;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackCustomerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private Marker customerMarker, repairerMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private double customerLatitude, customerLongitude;
    private LatLng customerLatLng;

    private Button btnCustomer, btnRepairer;
    private TextView txtTimeEstimate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_customer);

        btnCustomer = findViewById(R.id.btnCustomer);
        btnRepairer = findViewById(R.id.btnRepairer);
        txtTimeEstimate = findViewById(R.id.txtTimeEstimate);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        customerLatitude = getIntent().getDoubleExtra("latitude", 0);
        customerLongitude = getIntent().getDoubleExtra("longitude", 0);
        customerLatLng = new LatLng(customerLatitude, customerLongitude);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        customerMarker = mMap.addMarker(new MarkerOptions()
                .position(customerLatLng)
                .title("Customer Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 15));

        btnCustomer.setOnClickListener(v -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 17));
        });

        btnRepairer.setOnClickListener(v -> {
            if (repairerMarker != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(repairerMarker.getPosition(), 17));
            } else {
                Toast.makeText(this, "Repairer location not yet available", Toast.LENGTH_SHORT).show();
            }
        });
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
                if (location != null) {
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
        float distanceInMeters = results[0];
        float averageSpeedMetersPerSecond = 5.0f;
        int timeInSeconds = (int) (distanceInMeters / averageSpeedMetersPerSecond);
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;

        txtTimeEstimate.setText(String.format(Locale.getDefault(),
                "Estimated time: %d min %d sec", minutes, seconds));

        drawRoute(repairerLatLng, customerLatLng);
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        String apiKey = "AIzaSyB-i5EiY3aBPeMAln7nNgTEb6KEUl0TMbE";
        String url = String.format(Locale.getDefault(),
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&mode=walking&key=%s",
                origin.latitude, origin.longitude, destination.latitude, destination.longitude, apiKey);

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                JSONObject json = new JSONObject(responseBuilder.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    String encodedPoints = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points");

                    List<LatLng> steps = decodePolyline(encodedPoints);

                    runOnUiThread(() -> {
                        if (steps != null && !steps.isEmpty()) {
                            mMap.addPolyline(new PolylineOptions()
                                    .addAll(steps)
                                    .width(8f)
                                    .color(ContextCompat.getColor(this, R.color.blue)));
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
