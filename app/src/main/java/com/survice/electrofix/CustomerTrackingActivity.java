package com.survice.electrofix;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;

public class CustomerTrackingActivity extends BaseActivity {

    private TextView txtOrderStatus;
    private DatabaseReference orderRef, repairerLocationRef;
    private String customerID, repairerID;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_tracking);

        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            customerID = user.getUid();
            orderRef = FirebaseDatabase.getInstance().getReference("Orders");

            // অর্ডারের তথ্য নিয়ে আসবো
            fetchOrderStatus();
        }
    }

    private void fetchOrderStatus() {
        orderRef.orderByChild("customerID").equalTo(customerID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot order : snapshot.getChildren()) {
                            String status = order.child("status").getValue(String.class);
                            repairerID = order.child("repairerID").getValue(String.class);

                            txtOrderStatus.setText("Order Status: " + status);

                            // Repairer-এর লোকেশন নিয়ে আসবো
                            if (repairerID != null) {
                                fetchRepairerLocation();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void fetchRepairerLocation() {
        repairerLocationRef = FirebaseDatabase.getInstance().getReference("Users").child(repairerID);
        repairerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);

                    GeoPoint repairerLocation = new GeoPoint(latitude, longitude);
                    mapView.getController().setCenter(repairerLocation);
                    mapView.getController().setZoom(15.0);

                    // Repairer-এর লোকেশন দেখানোর জন্য Marker
                    Marker repairerMarker = new Marker(mapView);
                    repairerMarker.setPosition(repairerLocation);
                    repairerMarker.setTitle("Repairer Location");
                    mapView.getOverlays().clear();
                    mapView.getOverlays().add(repairerMarker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}