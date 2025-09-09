package com.pollution.project.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.pollution.dto.HourlyIndexResponse;
import com.pollution.dto.HourlyIndexResponse.Site;
import com.pollution.dto.HourlyIndexResponse.Species;
import com.pollution.dto.MonitoringSite;
import com.pollution.dto.Trie;
import com.pollution.project.entity.AirQualityData;
import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location; 
import com.pollution.project.repository.AirQualitySnapshotRepository;

@Service    
public class SiteCodeResolver {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";
    private static final Logger logger = LoggerFactory.getLogger(SiteCodeResolver.class);
    private final AirQualitySnapshotRepository snapshotRepository;

    public SiteCodeResolver(AirQualitySnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

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

    public void assignSiteCode(Location location, String userInput) {
        String siteCode = lookupSiteCode(userInput);
        if (siteCode == null) {
            siteCode = calculateSiteCode(location.getLatitude(), location.getLongitude());
        }
        location.setSiteCode(siteCode);
    }

    private Double getIndex(List<Species> speciesList, String speciesName) {
        for (Species species : speciesList) {
            if (species.getCode().equalsIgnoreCase(speciesName)) {
                try {
                    return Double.valueOf(species.getIndex());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public void populateLocationData(Location location, String userInput) {
        assignSiteCode(location, userInput);
        if (location.getSiteCode() == null) return;

        String url = "https://api.erg.ic.ac.uk/AirQuality/Hourly/MonitoringIndex/SiteCode="
                    + location.getSiteCode()
                    + "/Json";

        try {
            HourlyIndexResponse response = restTemplate.getForObject(url, HourlyIndexResponse.class);
    
            if (response != null
                && response.getHourlyAirQualityIndex() != null 
                && response.getHourlyAirQualityIndex().getLocalAuthority() != null 
                && response.getHourlyAirQualityIndex().getLocalAuthority().getSite() != null) {
                Site site = response.getHourlyAirQualityIndex()
                                    .getLocalAuthority()
                                    .getSite();
    
                List<Species> speciesList = site.getSpecies();
                LocalDateTime bulletinTime = LocalDateTime.parse(
                    site.getBulletinDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
    
                AirQualityData airData = new AirQualityData(
                    getIndex(speciesList, "PM25"),
                    getIndex(speciesList, "PM10"),
                    getIndex(speciesList, "NO2"),
                    getIndex(speciesList, "SO2"),
                    getIndex(speciesList, "O3"),
                    getIndex(speciesList, "CO"),
                    bulletinTime
                );
    
                location.setAirQualityData(airData);
                location.setName(site.getSiteName());
                location.setSiteCode(site.getSiteCode());
                logger.info("Populated air quality data for site code {}", location.getSiteCode());
            } else {
                location.setAirQualityData(null);
                logger.warn("No air quality data returned for site code {}", location.getSiteCode());
            }
        } catch (RestClientException e) {
            location.setAirQualityData(null);
            logger.error("Error fetching air quality data for site code {} ", location.getSiteCode(), e);
        }
    }

    public void refreshLocationData(Location location) {
        populateLocationData(location, location.getName());

        if (location.getAirQualityData() != null) {
            AirQualitySnapshot ss = new AirQualitySnapshot();
            ss.setLocation(location);
            ss.setTimestamp(LocalDateTime.now());
            ss.setPm25(location.getAirQualityData().getPm25());
            ss.setPm10(location.getAirQualityData().getPm10());
            ss.setNo2(location.getAirQualityData().getNo2());
            ss.setSo2(location.getAirQualityData().getSo2());
            ss.setO3(location.getAirQualityData().getO3());
            ss.setCo(location.getAirQualityData().getCo());
            snapshotRepository.save(ss);
        }
    }
}