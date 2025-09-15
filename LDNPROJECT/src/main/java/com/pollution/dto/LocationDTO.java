package com.pollution.dto;

import com.pollution.project.entity.AirQualityData;

public class LocationDTO {
    private String name;
    private double lat;
    private double lng;
    private AirQualityData airQualityData;
    public LocationDTO() {}

    public LocationDTO(String name, double lat, double lng, AirQualityData airQualityData) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.airQualityData = airQualityData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public AirQualityData getAirQualityData() {
        return airQualityData;
    }

    public void setAirQualityData(AirQualityData airQualityData) {
        this.airQualityData = airQualityData;
    }
}   
