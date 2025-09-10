package com.pollution.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HourlyIndexResponse {
    @JsonProperty("HourlyAirQualityIndex")
    private HourlyAirQualityIndex hourlyAirQualityIndex;
    
    public HourlyIndexResponse() {}

    public HourlyIndexResponse(HourlyAirQualityIndex hourlyAirQualityIndex) {
        this.hourlyAirQualityIndex = hourlyAirQualityIndex;
    }

    public HourlyAirQualityIndex getHourlyAirQualityIndex() {
        return hourlyAirQualityIndex;
    }

    public void setHourlyAirQualityIndex(HourlyAirQualityIndex hourlyAirQualityIndex) {
        this.hourlyAirQualityIndex = hourlyAirQualityIndex;
    }

    public static class HourlyAirQualityIndex {
        @JsonProperty("@TimeToLive")
        private String timeToLive;

        @JsonProperty("LocalAuthority")
        private LocalAuthority localAuthority;

        public HourlyAirQualityIndex() {}

        public HourlyAirQualityIndex(String timeToLive, LocalAuthority localAuthority) {
            this.timeToLive = timeToLive;
            this.localAuthority = localAuthority;
        }

        // getters/setters
        public String getTimeToLive() {
            return timeToLive;
        }

        public void setTimeToLive(String timeToLive) {
            this.timeToLive = timeToLive;
        }

        public LocalAuthority getLocalAuthority() {
            return localAuthority;
        }

        public void setLocalAuthority(LocalAuthority localAuthority) {
            this.localAuthority = localAuthority;
        }
    }

    public static class LocalAuthority {
        @JsonProperty("@LocalAuthorityName")
        private String name;

        @JsonProperty("@LocalAuthorityCode")
        private String code;

        @JsonProperty("@LaCentreLatitude")
        private String centreLatitude;

        @JsonProperty("@LaCentreLongitude")
        private String centreLongitude;

        @JsonProperty("Site")
        private Site site;

        public LocalAuthority() {}

        public LocalAuthority(String name, String code, String centreLatitude, String centreLongitude, Site site) {
            this.name = name;
            this.code = code;
            this.centreLatitude = centreLatitude;
            this.centreLongitude = centreLongitude;
            this.site = site;
        }

        // getters/setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }


        public String getCentreLatitude() {
            return centreLatitude;
        }

        public void setCentreLatitude(String centreLatitude) {
            this.centreLatitude = centreLatitude;
        }

        public String getCentreLongitude() {
            return centreLongitude;
        }

        public void setCentreLongitude(String centreLongitude) {
            this.centreLongitude = centreLongitude;
        }

        public Site getSite() {
            return site;
        }

        public void setSite(Site site) {
            this.site = site;
        }
    }

    public static class Site {
        @JsonProperty("@Latitude")
        private String latitude;

        @JsonProperty("@Longitude")
        private String longitude;

        @JsonProperty("@SiteCode")
        private String siteCode;

        @JsonProperty("@SiteName")
        private String siteName;

        @JsonProperty("@BulletinDate")
        private String bulletinDate;

        @JsonProperty("species")
        private List<Species> species;

        public Site() {}

        public Site(String latitude, String longitude, String siteCode, String siteName, String bulletinDate, List<Species> species) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.siteCode = siteCode;
            this.siteName = siteName;
            this.bulletinDate = bulletinDate;
            this.species = species;
        }

        // getters/setters
        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getSiteCode() {
            return siteCode;
        }

        public void setSiteCode(String siteCode) {
            this.siteCode = siteCode;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getBulletinDate() {
            return bulletinDate;
        }

        public void setBulletinDate(String bulletinDate) {
            this.bulletinDate = bulletinDate;
        }

        public List<Species> getSpecies() {
            return species;
        }

        public void setSpecies(List<Species> species) {
            this.species = species;
        }
    }

    public static class Species {
        @JsonProperty("@SpeciesName")
        private String name;

        @JsonProperty("@SpeciesCode")
        private String code;

        @JsonProperty("@AirQualityIndex")
        private String index;

        @JsonProperty("@AirQualityBand")
        private String band;

        @JsonProperty("@IndexSource")
        private String source;

        public Species() {}

        public Species(String name, String code, String index, String band, String source) {
            this.name = name;
            this.code = code;
            this.index = index;
            this.band = band;
            this.source = source;
        }

        // getters/setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getBand() {
            return band;
        }

        public void setBand(String band) {
            this.band = band;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}