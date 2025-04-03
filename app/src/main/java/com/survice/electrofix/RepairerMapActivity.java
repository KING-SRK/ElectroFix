package com.survice.electrofix;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class RepairerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference customerLocationRef;
    private GeoFire geoFire;
    private String customerID = "customer_123"; // *🔥 Replace with actual customer ID*
    private Marker customerMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairer_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        customerLocationRef = FirebaseDatabase.getInstance().getReference("Customers_Location");
        geoFire = new GeoFire(customerLocationRef);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        trackCustomerLocation();
    }

    private void trackCustomerLocation() {
        customerLocationRef.child(customerID).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() == 2) {
                    double latitude = snapshot.child("0").getValue(Double.class);
                    double longitude = snapshot.child("1").getValue(Double.class);
                    LatLng customerLatLng = new LatLng(latitude, longitude);

                    if (customerMarker != null) {
                        customerMarker.setPosition(customerLatLng);
                    } else {
                        customerMarker = mMap.addMarker(new MarkerOptions()
                                .position(customerLatLng)
                                .title("Customer Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 15));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RepairerMap", "Error retrieving location: " + error.getMessage());
            }
        });

    }
}