package com.survice.electrofix;

import android.util.Log;
import androidx.annotation.NonNull;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;

public class NearbyRepairers {

    private DatabaseReference databaseReference;
    private GeoFire geoFire;
    private GeoQuery geoQuery;

    public interface NearbyRepairerListener {
        void onRepairerFound(String repairerID, double lat, double lng);
        void onRepairerRemoved(String repairerID);
    }

    private NearbyRepairerListener listener;

    public NearbyRepairers(NearbyRepairerListener listener) {
        this.listener = listener;
        databaseReference = FirebaseDatabase.getInstance().getReference("Repairers_Location");
        geoFire = new GeoFire(databaseReference);
    }

    public void findNearbyRepairers(double userLat, double userLng, double radius) {
        geoQuery = geoFire.queryAtLocation(new GeoLocation(userLat, userLng), radius);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("NearbyRepairers", "Repairer Found: " + key);
                listener.onRepairerFound(key, location.latitude, location.longitude);
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("NearbyRepairers", "Repairer Removed: " + key);
                listener.onRepairerRemoved(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {}

            @Override
            public void onGeoQueryReady() {}

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("NearbyRepairers", "GeoQuery Error: " + error.getMessage());
            }
        });
    }

    public void stopQuery() {
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
    }
}