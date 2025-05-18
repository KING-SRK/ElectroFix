package com.survice.electrofix;

import java.io.Serializable;

public class Service implements Serializable {
    private String serviceName;
    private String servicePrice;
    private int serviceIcon;

    // Constructor
    public Service(String serviceName, String servicePrice, int serviceIcon) {
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.serviceIcon = serviceIcon;
    }

    // Getter Methods
    public String getServiceName() {
        return serviceName;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public int getServiceIcon() {
        return serviceIcon;
    }
}
