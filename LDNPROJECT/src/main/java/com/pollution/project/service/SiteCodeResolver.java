package com.pollution.project.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollution.dto.HourlyIndexResponse;
import com.pollution.dto.HourlyIndexResponse.HourlyAirQualityIndex;
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
    private final Object lock = new Object();
    private final String apiUrl = "https://api.erg.ic.ac.uk/AirQuality/Information/MonitoringSites/GroupName=London/Json";
    private static final Logger logger = LoggerFactory.getLogger(SiteCodeResolver.class);
    private final AirQualitySnapshotRepository snapshotRepository;
    private Trie siteTrie;
    private final RestTemplate restTemplate;
    
    protected SiteCodeResolver() {
        this.snapshotRepository = null;
        this.restTemplate = null;
    }

    @Autowired
    public SiteCodeResolver(RestTemplate restTemplate, AirQualitySnapshotRepository snapshotRepository) {
        this.restTemplate = restTemplate;
        this.snapshotRepository = snapshotRepository;
    }

    // !! Thread-safe lazy initialization of the Trie to prevent multiple tries being created.
    public synchronized Trie getSiteTrie() {
        if (siteTrie == null) {
            siteTrie = new Trie();
            MonitoringSite[] sites = restTemplate.getForObject(apiUrl, MonitoringSite[].class);
            if (sites != null) {
                for (MonitoringSite site : sites) {
                    siteTrie.insert(site.getSiteName(), site.getSiteCode());
                }
            }
        }
        return siteTrie;
    }

    public void setSiteTrie(Trie trie) {
        synchronized (lock) {
            this.siteTrie = trie;
        }
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "GMT") // Every day at midnight
    public void refreshSiteTrie() {
        try {
            MonitoringSite[] sites = restTemplate.getForObject(apiUrl, MonitoringSite[].class);
            if (sites != null) {
                Trie newTrie = new Trie();
                for (MonitoringSite site : sites) {
                    newTrie.insert(site.getSiteName().trim(), site.getSiteCode());
                }
                
                setSiteTrie(newTrie);
                logger.info("Site trie refreshed successfully, with {} sites.", sites.length);
            }
        } catch (RestClientException e) {
            logger.error("Error refreshing site trie: {}", e);
        }
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

        siteName = siteName.toLowerCase().trim();
        if (siteName.isEmpty()) return null;
        
        Trie trie = getSiteTrie();
        
        String exactMatch = trie.searchExact(siteName);
        if (exactMatch != null) {
            return exactMatch;
        }
        
        List<String> potential = trie.getSuggestions(siteName);
        if (!potential.isEmpty()) {
            // Pick the "most specific" match (longest site name)
            String bestMatch = potential.stream().max(Comparator.comparingInt(String::length)).orElse(null);
            if (bestMatch != null) {
                int colonIndex = bestMatch.indexOf(":");
                int endIndex = bestMatch.indexOf(")", colonIndex);
                if (colonIndex != -1 && endIndex != -1) {
                    String code = bestMatch.substring(colonIndex + 2, endIndex);
                    return "null".equals(code) ? null : code;
                }
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

    Double getIndex(List<Species> speciesList, String speciesName) {
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
        logger.info("Starting populateLocationData for userInput: {}, location before assignSiteCode: {}", userInput, location);

        // Assign site code first
        assignSiteCode(location, userInput);
        logger.info("After assignSiteCode, location: {}", location);

        if (location.getSiteCode() == null) {
            logger.warn("Site code is null after assignSiteCode, returning early");
            return;
        }

        String url = "https://api.erg.ic.ac.uk/AirQuality/Hourly/MonitoringIndex/SiteCode="
                    + location.getSiteCode()
                    + "/Json";

        String rawJson = restTemplate.getForObject(url, String.class);
        logger.info("Raw API response for siteCode {}: {}", location.getSiteCode(), rawJson);

        HourlyIndexResponse response = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            response = mapper.readValue(rawJson, HourlyIndexResponse.class);
            logger.info("Mapped response successfully for siteCode {}: {}", location.getSiteCode(), response);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize API response for siteCode {}: {}", location.getSiteCode(), e.getMessage(), e);
            location.setAirQualityData(new AirQualityData(null, null, null, null, null, null, null));
            return;
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