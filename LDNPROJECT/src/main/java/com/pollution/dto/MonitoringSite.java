package com.pollution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MonitoringSite {
    @JsonProperty("@SiteCode")
    private String siteCode;

    @JsonProperty("@Latitude")
    private String latitude;

    @JsonProperty("@Longitude")
    private String longitude;

    @JsonProperty("@SiteName")
    private String siteName;

    public MonitoringSite() {}

    public MonitoringSite(String siteName, String siteCode) {
        this.siteName = siteName;
        this.siteCode = siteCode;
    }

    public MonitoringSite(String siteCode, String latitude, String longitude) {
        this.siteCode = siteCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MonitoringSite(String siteCode, String latitude, String longitude, String siteName) {
        this.siteCode = siteCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.siteName = siteName;
    }

    @SuppressWarnings("unused")
    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getSiteCode() {
        return siteCode;
    }

    @SuppressWarnings("unused")
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLatitude() {
        return latitude;
    }
    
    @SuppressWarnings("unused")
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude() {
        return longitude;
    }

    @SuppressWarnings("unused")
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteName() {
        return siteName;
    }
}
