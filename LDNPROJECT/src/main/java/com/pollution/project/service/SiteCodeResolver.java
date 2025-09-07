package com.pollution.project.service;

import java.util.List;

import org.springframework.web.client.RestTemplate;

import com.pollution.dto.MonitoringSite;
import com.pollution.dto.Trie;

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
}
