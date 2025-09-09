package com.pollution.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public class LocationRequest {

    @DecimalMin(value = "51.28", message = "Latitude must be >= 51.28 for London")
    @DecimalMax(value = "51.70", message = "Latitude must be <= 51.70 for London")
    private double lat;

    @DecimalMin(value = "-0.50", message = "Longitude must be >= -0.50 for London")
    @DecimalMax(value = "0.33", message = "Longitude must be <= 0.33 for London")
    private double lng;

    @Size(max = 100, message = "Site name must not exceed 100 characters")
    private String siteName;

    public LocationRequest() {}

    public LocationRequest(double lat, double lng, String siteName) {
        this.lat = lat;
        this.lng = lng;
        this.siteName = siteName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
}