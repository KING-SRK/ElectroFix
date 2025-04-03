package com.survice.electrofix;

public class UserLocation {
    public double latitude;
    public double longitude;

    public UserLocation() {
        // Default constructor required for Firebase
    }

    public UserLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}