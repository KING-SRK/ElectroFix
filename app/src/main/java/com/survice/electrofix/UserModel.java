package com.survice.electrofix;

public class UserModel {
    public String email, phone, userType;

    public UserModel() {
        // Default Constructor for Firebase
    }

    public UserModel(String email, String phone, String userType) {
        this.email = email;
        this.phone = phone;
        this.userType = userType;
    }
}