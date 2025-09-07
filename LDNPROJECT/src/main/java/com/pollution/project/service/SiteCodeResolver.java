package com.pollution.project.service;

import com.pollution.project.entity.AirQualityData;
import com.pollution.project.entity.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import com.pollution.dto.MonitoringSite;
import com.pollution.dto.Trie;

@Service    
public class SiteCodeResolver {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";

    public String calculateSiteCode(double lat, double lng) {
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
            }
        }

        return nearest != null ? nearest.getSiteCode() : null;
    }

    public String lookupSiteCode(String siteName) {
        if (siteName == null || siteName.isEmpty()) return null;
        MonitoringSite[] sites = restTemplate.getForObject(apiUrl, MonitoringSite[].class);

        if (sites == null || sites.length == 0) return null;

        Trie trie = new Trie();
        for (MonitoringSite site : sites) {
            trie.insert(site.getSiteName(), site.getSiteCode());
        }

        String exactMatch = trie.searchExact(siteName);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        List<String> potential = trie.getSuggestions(siteName);

        if (!potential.isEmpty()) {
            String firstSuggestion = potential.get(0);  // e.g., "bexley west (BQ8)"
            int start = firstSuggestion.indexOf('(');
            int end = firstSuggestion.indexOf(')');
            if (start != -1 && end != -1) {
                return firstSuggestion.substring(start + 1, end);  // returns "BQ8"
            }
        }
        
        return null;
    }

    public void assignSiteCode(Location location) {
        String siteCode = lookupSiteCode(location.getSiteName());
        if (siteCode == null) {
            siteCode = calculateSiteCode(location.getLatitude(), location.getLongitude());
        }
        location.setSiteCode(siteCode);
    }

    // Fetch and populate air quality data
    public void populateLocationData(Location location) {
        assignSiteCode(location);
        if (location.getSiteCode() == null) return;

        String today = LocalDate.now().toString();
        String url = "https://api.erg.ic.ac.uk/AirQuality/Data/Wide/Site/SiteCode="
                     + location.getSiteCode()
                     + "/StartDate=" + today + "/EndDate=" + today + "/Json";

        WideDataPoint[] readings = restTemplate.getForObject(url, WideDataPoint[].class);
        if (readings != null && readings.length > 0) {
            WideDataPoint latest = readings[readings.length - 1];
            AirQualityData airData = new AirQualityData(
                latest.getPm25(),
                latest.getPm10(),
                latest.getNo2(),
                latest.getSo2(),
                latest.getO3(),
                latest.getCo(),
                LocalDateTime.parse(latest.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
            location.setAirQualityData(airData);
        }
    }
}
