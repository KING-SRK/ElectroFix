package com.survice.electrofix;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class TrackRepairerActivity extends AppCompatActivity {

    private MapView mapView;
    private IMapController mapController;
    private Marker customerMarker, repairerMarker;
    private Polyline line;
    private double customerLat, customerLon;
    private String repairerId;

    private ListenerRegistration repairerLocationListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_track_repairer);

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15.0);

        customerLat = getIntent().getDoubleExtra("customerLat", 0);
        customerLon = getIntent().getDoubleExtra("customerLon", 0);
        repairerId = getIntent().getStringExtra("repairerId");

        // Show customer marker
        GeoPoint customerPoint = new GeoPoint(customerLat, customerLon);
        customerMarker = new Marker(mapView);
        customerMarker.setPosition(customerPoint);
        customerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        customerMarker.setTitle("Your Location");
        Drawable customerIcon = ContextCompat.getDrawable(this, R.drawable.ic_customer_marker);
        if (customerIcon != null) customerMarker.setIcon(customerIcon);
        mapView.getOverlays().add(customerMarker);

        mapController.setCenter(customerPoint);

        startListeningRepairerLocation();
    }

    private void startListeningRepairerLocation() {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("RepairerLocations")
                .document(repairerId);

        repairerLocationListener = docRef.addSnapshotListener((snapshot, error) -> {
            if (snapshot != null && snapshot.exists()) {
                double lat = snapshot.getDouble("latitude");
                double lon = snapshot.getDouble("longitude");
                updateRepairerMarker(lat, lon);
            }
        });
    }

    private void updateRepairerMarker(double lat, double lon) {
        GeoPoint repairerPoint = new GeoPoint(lat, lon);

        if (repairerMarker == null) {
            repairerMarker = new Marker(mapView);
            repairerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            repairerMarker.setTitle("Repairer Location");
            Drawable repairerIcon = ContextCompat.getDrawable(this, R.drawable.ic_repairer_marker);
            if (repairerIcon != null) repairerMarker.setIcon(repairerIcon);
            mapView.getOverlays().add(repairerMarker);
        }

        repairerMarker.setPosition(repairerPoint);

        drawLineBetweenPoints(
                new GeoPoint(customerLat, customerLon),
                repairerPoint
        );

        mapView.invalidate();
    }

    private void drawLineBetweenPoints(GeoPoint point1, GeoPoint point2) {
        if (line != null) {
            mapView.getOverlays().remove(line);
        }

        ArrayList<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(point1);
        geoPoints.add(point2);

        line = new Polyline();
        line.setPoints(geoPoints);
        line.setColor(Color.BLUE);
        line.setWidth(5f);

        mapView.getOverlays().add(line);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repairerLocationListener != null) {
            repairerLocationListener.remove();
        }
    }
}