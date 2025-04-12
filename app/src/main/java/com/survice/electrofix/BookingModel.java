package com.survice.electrofix;

import com.google.firebase.firestore.DocumentId;

public class BookingModel {

    @DocumentId
    private String bookingId;
    private String name;
    private String phone;
    private String address;
    private String preferredTime;
    private String serviceName;
    private String servicePrice;
    private String status;       // "pending", "accepted", "rejected"
    private String acceptedBy;
    private double customerLatitude;
    private double customerLongitude;

    // Default constructor required for Firestore
    public BookingModel() {}

    // Parameterized constructor (optional, if you need to manually create objects)
    public BookingModel(String bookingId, String name, String phone, String address, String preferredTime,
                        String serviceName, String servicePrice, String status, String acceptedBy,
                        double customerLatitude, double customerLongitude) {
        this.bookingId = bookingId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.preferredTime = preferredTime;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.status = status;
        this.acceptedBy = acceptedBy;
        this.customerLatitude = customerLatitude;
        this.customerLongitude = customerLongitude;
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(String servicePrice) {
        this.servicePrice = servicePrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public double getCustomerLatitude() {
        return customerLatitude;
    }

    public void setCustomerLatitude(double customerLatitude) {
        this.customerLatitude = customerLatitude;
    }

    public double getCustomerLongitude() {
        return customerLongitude;
    }

    public void setCustomerLongitude(double customerLongitude) {
        this.customerLongitude = customerLongitude;
    }

    @Override
    public String toString() {
        return "BookingModel{" +
                "bookingId='" + bookingId + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", preferredTime='" + preferredTime + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", servicePrice='" + servicePrice + '\'' +
                ", status='" + status + '\'' +
                ", acceptedBy='" + acceptedBy + '\'' +
                ", customerLatitude=" + customerLatitude +
                ", customerLongitude=" + customerLongitude +
                '}';
    }
}