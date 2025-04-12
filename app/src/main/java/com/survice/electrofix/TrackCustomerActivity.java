package com.survice.electrofix;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackCustomerActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private Marker customerMarker, repairerMarker;
    private Polyline line;
    private CompassOverlay compassOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private IMapController mapController;

    private double customerLatitude, customerLongitude;
    private GeoPoint customerPoint;

    private Button btnCustomer, btnRepairer;
    private TextView txtTimeEstimate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_track_customer);

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15.0);

        btnCustomer = findViewById(R.id.btnCustomer);
        btnRepairer = findViewById(R.id.btnRepairer);
        txtTimeEstimate = findViewById(R.id.txtTimeEstimate);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        customerLatitude = getIntent().getDoubleExtra("latitude", 0);
        customerLongitude = getIntent().getDoubleExtra("longitude", 0);
        customerPoint = new GeoPoint(customerLatitude, customerLongitude);

        customerMarker = new Marker(mapView);
        customerMarker.setPosition(customerPoint);
        customerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        customerMarker.setTitle("Customer Location");
        Drawable customerIcon = ContextCompat.getDrawable(this, R.drawable.ic_customer_marker);
        if (customerIcon != null) customerMarker.setIcon(customerIcon);
        mapView.getOverlays().add(customerMarker);
        mapController.setCenter(customerPoint);

        compassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        btnCustomer.setOnClickListener(v -> {
            mapController.animateTo(customerPoint);
            mapController.setZoom(17.0);
        });

        btnRepairer.setOnClickListener(v -> {
            if (repairerMarker != null) {
                mapController.animateTo(repairerMarker.getPosition());
                mapController.setZoom(17.0);
            } else {
                Toast.makeText(this, "Repairer location not yet available", Toast.LENGTH_SHORT).show();
            }
        });

        if (!isLocationEnabled()) {
            showLocationEnableDialog();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showLocationEnableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Required")
                .setMessage("Please enable location to view tracking.")
                .setCancelable(false)
                .setPositiveButton("Enable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
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
        GeoPoint repairerPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (repairerMarker == null) {
            repairerMarker = new Marker(mapView);
            repairerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            repairerMarker.setTitle("Repairer Location");
            Drawable repairerIcon = ContextCompat.getDrawable(this, R.drawable.ic_repairer_marker);
            if (repairerIcon != null) repairerMarker.setIcon(repairerIcon);
            mapView.getOverlays().add(repairerMarker);
        }

        repairerMarker.setPosition(repairerPoint);

        fetchAndDrawRoute(
                repairerPoint.getLatitude(), repairerPoint.getLongitude(),
                customerPoint.getLatitude(), customerPoint.getLongitude()
        );

        float[] results = new float[1];
        Location.distanceBetween(
                repairerPoint.getLatitude(), repairerPoint.getLongitude(),
                customerPoint.getLatitude(), customerPoint.getLongitude(),
                results
        );
        float distanceInMeters = results[0];
        float averageSpeedMetersPerSecond = 5.0f;
        int timeInSeconds = (int) (distanceInMeters / averageSpeedMetersPerSecond);
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;

        txtTimeEstimate.setText(String.format(Locale.getDefault(),
                "Estimated time: %d min %d sec", minutes, seconds));

        mapView.invalidate();
    }

    private void fetchAndDrawRoute(double startLat, double startLon, double endLat, double endLon) {
        String url = String.format(
                Locale.getDefault(),
                "https://api.openrouteservice.org/v2/directions/foot-walking?api_key=5b3ce3597851110001cf6248fbc6cfe78bda4e1e8f3b9a946d665d4b&start=%f,%f&end=%f,%f",
                startLon, startLat, endLon, endLat
        );

        new Thread(() -> {
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");

                int responseCode = con.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject json = new JSONObject(response.toString());
                    JSONArray coordinates = json
                            .getJSONArray("features")
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONArray("coordinates");

                    List<GeoPoint> routePoints = new ArrayList<>();
                    for (int i = 0; i < coordinates.length(); i++) {
                        JSONArray point = coordinates.getJSONArray(i);
                        double lon = point.getDouble(0);
                        double lat = point.getDouble(1);
                        routePoints.add(new GeoPoint(lat, lon));
                    }

                    runOnUiThread(() -> {
                        if (line == null) {
                            line = new Polyline();
                            line.setWidth(6f);
                            line.setColor(Color.BLUE);
                            mapView.getOverlays().add(line);
                        }
                        line.setPoints(routePoints);
                        mapView.invalidate();
                    });

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Route API Error", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Route Fetch Failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (compassOverlay != null) compassOverlay.enableCompass();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (compassOverlay != null) compassOverlay.disableCompass();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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