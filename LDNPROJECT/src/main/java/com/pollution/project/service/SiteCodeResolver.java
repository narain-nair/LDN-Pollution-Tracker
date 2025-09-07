package com.pollution.project.service;

import org.springframework.web.client.RestTemplate;
import com.pollution.project.dto.MonitoringSite;

public class SiteCodeResolver {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";

    public String calculateSiteCode(double lat, double long) {
        MonitoringSite[] sites = restTemplate.getForObject(apiUrl, MonitoringSite[].class);
        MonitoringSite nearest = null;
        double minDistance = Double.MAX_VALUE;

        try {
            for (MonitoringSite site : sites) {
                double siteLat = Double.parseDouble(site.getLatitude());
                double siteLong = Double.parseDouble(site.getLongitude());
                double distance = GeoUtils.haversine(lat, long, siteLat, siteLong);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = site;
                }
            }

            return nearest != null ? nearest.getSiteCode() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
