package com.pollution.project.service;

import org.springframework.web.client.RestTemplate;

import com.pollution.dto.MonitoringSite;

public class SiteCodeResolver {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";

    public String calculateSiteCode(double lat, double lng, String siteName) {
        MonitoringSite[] sites = restTemplate.getForObject(apiUrl, MonitoringSite[].class);
        if (sites == null || sites.length == 0) return null;

        MonitoringSite nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (MonitoringSite site : sites) {
            try {
                double siteLat = Double.parseDouble(site.getLatitude());
                double siteLng = Double.parseDouble(site.getLongitude());
                double distance = GeoUtils.haversine(lat, lng, siteLat, siteLng);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = site;
                }
            } catch (NumberFormatException e) {
                // Skip invalid coordinates
                continue;
            }
        }

        return nearest != null ? nearest.getSiteCode() : null;
    }

    public String lookupSiteCode(String siteName) {
        if (siteName == null || siteName.isEmpty()) return null;

        MonitoringSite[] sites = restTemplate.getForObject(apiUrl, MonitoringSite[].class);
        if (sites == null || sites.length == 0) return null;

        for (MonitoringSite site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                return site.getSiteCode();
            }
        }

        return null;
    }
}
