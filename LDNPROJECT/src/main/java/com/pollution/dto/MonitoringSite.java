package com.pollution.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MonitoringSite {
    @JsonProperty("@SiteCode")
    private String siteCode;

    @JsonProperty("@Latitude")
    private String latitude;

    @JsonProperty("@Longitude")
    private String longitude;

    private void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getSiteCode() {
        return siteCode;
    }

    private void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLatitude() {
        return latitude;
    }
    
    private void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
