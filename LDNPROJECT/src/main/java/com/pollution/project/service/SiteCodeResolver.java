package com.pollution.project.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollution.dto.HourlyIndexResponse;
import com.pollution.dto.HourlyIndexResponse.HourlyAirQualityIndex;
import com.pollution.dto.HourlyIndexResponse.Site;
import com.pollution.dto.HourlyIndexResponse.Species;
import com.pollution.dto.MonitoringSite;
import com.pollution.dto.Trie;
import com.pollution.dto.MonitoringSiteResponse;
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

    public MonitoringSiteResponse fetchMonitoringSites() {
        try {
            // Step 1: Always get raw JSON as String
            String rawJson = restTemplate.getForObject(apiUrl, String.class);
            logger.info("Raw monitoring sites JSON snippet: {}",
                rawJson != null && rawJson.length() > 500 
                    ? rawJson.substring(0, 500) + "..." 
                    : rawJson
            );

            // Step 2: Parse manually with Jackson
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(rawJson, MonitoringSiteResponse.class);

        } catch (IOException e) {
            logger.error("Failed to parse MonitoringSites JSON: {}", e.getMessage());
            return null;
        }
    }

    // !! Thread-safe lazy initialization of the Trie to prevent multiple tries being created.
    public synchronized Trie getSiteTrie() {
        if (siteTrie == null) {
            siteTrie = new Trie();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
                String rawJson = response.getBody();

                logger.info("Raw API response for monitoring sites: {}", 
                    rawJson != null 
                        ? rawJson.substring(0, Math.min(500, rawJson.length())) + "..." 
                        : "null"
                );

                String cleanJson = rawJson != null ? rawJson.replaceAll("\\\\'", "'") : null;
                ObjectMapper mapper = new ObjectMapper();

                MonitoringSiteResponse siteResponse = mapper.readValue(cleanJson, MonitoringSiteResponse.class);

                MonitoringSite[] sites = siteResponse != null ? siteResponse.getMonitoringSites() : new MonitoringSite[0];
                if (sites != null) {
                    for (MonitoringSite site : sites) {
                        if (site.getSiteName() != null) {
                            siteTrie.insert(site.getSiteName().trim(), site.getSiteCode());
                        } else {
                            logger.warn("Skipping site with null name, code={}", site.getSiteCode());
                        }
                    }
                    logger.info("Loaded {} monitoring sites into trie.", sites.length);
                } else {
                    logger.warn("No monitoring sites found in API response.");
                }

            } catch(IOException e) {
                logger.error("Error parsing API response: {}", e.getMessage());
            } catch(RestClientException e) {
                logger.error("Error fetching monitoring sites: {}", e.getMessage());
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
    public void refreshSiteTrie() throws JsonMappingException, JsonProcessingException {
        try {
            
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            String rawJson = response.getBody();
            
            logger.info("Raw API response for monitoring sites: {}", 
                rawJson != null 
                    ? rawJson.substring(0, Math.min(500, rawJson.length())) + "..." 
                    : "null"
            );

            String cleanJson = rawJson != null ? rawJson.replaceAll("\\\\'", "'") : null;
            ObjectMapper mapper = new ObjectMapper();

            MonitoringSiteResponse siteResponse = mapper.readValue(cleanJson, MonitoringSiteResponse.class);
            MonitoringSite[] sites = response != null ? siteResponse.getMonitoringSites() : new MonitoringSite[0];
            if (sites != null) {
                Trie newTrie = new Trie();
                for (MonitoringSite site : sites) {
                    if (site.getSiteName() != null) {
                        newTrie.insert(site.getSiteName().trim(), site.getSiteCode());
                    } else {
                        logger.warn("Skipping site with null name, code={}", site.getSiteCode());
                    }
                }
                
                setSiteTrie(newTrie);
                logger.info("Site trie refreshed successfully, with {} sites.", sites.length);
            } else {
                logger.warn("No monitoring sites found in API response during trie refresh.");
            }
        } catch (RestClientException e) {
            logger.error("Error refreshing site trie: {}", e);
        }
    }

    public String calculateSiteCode(double lat, double lng) {
        String code = null;
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
            String rawJson = responseEntity.getBody();
            
            logger.info("Raw API response for monitoring sites: {}", 
                rawJson != null 
                    ? rawJson.substring(0, Math.min(500, rawJson.length())) + "..." 
                    : "null"
            );

            String cleanJson = rawJson != null ? rawJson.replaceAll("\\\\'", "'") : null;
            ObjectMapper mapper = new ObjectMapper();
            MonitoringSiteResponse response = mapper.readValue(cleanJson, MonitoringSiteResponse.class);

            MonitoringSite[] sites = response != null ? response.getMonitoringSites() : new MonitoringSite[0];
            
            if (sites == null || sites.length == 0) return null;
            logger.info("calculateSiteCode: got {} sites from API", sites.length);

            MonitoringSite nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (MonitoringSite site : sites) {
                if (site.getSiteName() == null) {
                    logger.warn("Skipping site {} due to missing data", site.getSiteCode());
                    continue;
                }
                try {
                    double siteLat = Double.parseDouble(site.getLatitude());
                    double siteLng = Double.parseDouble(site.getLongitude());
                    double distance = GeoUtils.haversine(lat, lng, siteLat, siteLng);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = site;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Skipping site {} due to invalid coords", site.getSiteCode());
                }
            }

            code = nearest != null ? nearest.getSiteCode() : null;
            logger.info("calculateSiteCode: nearest site code = {}", code);
        } catch (RestClientException | IOException e) {
            logger.error("Error calculating site code: {}", e.getMessage());
            return null;
        }
        return code;
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

    public List<String> lookupPotentialSiteCodes(String input) {
        if (input == null || input.isEmpty()) return List.of();
    
        // Normalize input
        input = input.toLowerCase().trim();
        if (input.isEmpty()) return List.of();
    
        Trie trie = getSiteTrie();
    
        // Get suggestions from Trie
        List<String> suggestions = trie.getSuggestions(input);
    
        // Extract site codes from suggestions
        return suggestions.stream()
            .map(s -> {
                int colonIndex = s.indexOf(":");
                int endIndex = s.indexOf(")", colonIndex);
                if (colonIndex != -1 && endIndex != -1) {
                    String code = s.substring(colonIndex + 2, endIndex).trim();
                    return "null".equalsIgnoreCase(code) ? null : code;
                }
                return null;
            })
            .filter(c -> c != null) // Remove nulls
            .toList();
    }

    public void assignSiteCode(Location location, String userInput) {
        String siteCode = lookupSiteCode(userInput);
        if (siteCode == null || siteCode.isEmpty()) {
            siteCode = calculateSiteCode(location.getLatitude(), location.getLongitude());
        }
        location.setSiteCode(siteCode);
    }

    Double getIndex(List<Species> speciesList, String speciesName) {
        for (Species species : speciesList) {
            if (species.getCode().equalsIgnoreCase(speciesName)) {
                if ("No data".equalsIgnoreCase(species.getBand())) {
                    return null; // treat as missing
                }
                try {
                    return Double.valueOf(species.getIndex());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public void populateLocationData(Location location, String userInput) throws JsonMappingException, JsonProcessingException {
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

        try {
            logger.info("Fetching air quality data from URL: {}", url);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            String rawJson = responseEntity.getBody();
            
            if (rawJson == null || rawJson.trim().isEmpty() || rawJson.trim().equalsIgnoreCase("null")) {
                logger.warn("Received null or empty JSON for siteCode {}. Setting empty AirQualityData.", location.getSiteCode());
                location.setAirQualityData(null);
                return;
            }

            String cleanJson = rawJson != null ? rawJson.replaceAll("\\\\'", "'") : null;

            logger.info("Raw API response for siteCode {}: {}", location.getSiteCode(), cleanJson);
            ObjectMapper mapper = new ObjectMapper();
            HourlyIndexResponse response = mapper.readValue(cleanJson, HourlyIndexResponse.class);

            if (response == null || response.getHourlyAirQualityIndex() == null
            || response.getHourlyAirQualityIndex().getLocalAuthority() == null
            || response.getHourlyAirQualityIndex().getLocalAuthority().getSite() == null) {
                logger.warn("Incomplete or null response for siteCode {}. Setting empty AirQualityData.", location.getSiteCode());
                location.setAirQualityData(null);                
                return;
            }

            HourlyAirQualityIndex hqi = response.getHourlyAirQualityIndex();
            Site site = hqi.getLocalAuthority().getSite();
            logger.info("Fetched site data: {}", site);

            List<Species> speciesList = site.getSpecies();
            logger.info("Species list: {}", speciesList);

            LocalDateTime bulletinTime = null;
            try {
                bulletinTime = LocalDateTime.parse(site.getBulletinDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException e) {
                logger.warn("Failed to parse bulletin date '{}' for siteCode {}: {}", site.getBulletinDate(), location.getSiteCode(), e.getMessage());
            }

            // Always instantiate AirQualityData
            AirQualityData airData = new AirQualityData(
                getIndexOrDefault(speciesList, "PM25"),
                getIndexOrDefault(speciesList, "PM10"),
                getIndexOrDefault(speciesList, "NO2"),
                getIndexOrDefault(speciesList, "SO2"),
                getIndexOrDefault(speciesList, "O3"),
                getIndexOrDefault(speciesList, "CO"),
                bulletinTime
            );

            // Check if all values are null
            boolean hasAnyData =
                    airData.getPm25() != null ||
                    airData.getPm10() != null ||
                    airData.getNo2()  != null ||
                    airData.getSo2()  != null ||
                    airData.getO3()   != null ||
                    airData.getCo()   != null;

            if (hasAnyData) {
                location.setAirQualityData(airData);
                location.setName(site.getSiteName());
                location.setSiteCode(site.getSiteCode());
            
                logger.info("PM25: {}, PM10: {}, NO2: {}, SO2: {}, O3: {}, CO: {}", 
                    airData.getPm25(), airData.getPm10(), airData.getNo2(),
                    airData.getSo2(), airData.getO3(), airData.getCo()
                );
            
                logger.info("Populated AirQualityData: {}", airData);
                logger.info("Location after population: {}", location);
            } else {
                logger.warn("Skipping site {} ({}) — no valid air quality data", site.getSiteName(), site.getSiteCode());
                location.setAirQualityData(null); 
            }

            logger.info("Populated AirQualityData: {}", airData);
            logger.info("Location after population: {}", location);

        } catch (HttpClientErrorException e) {
            logger.error("Client error ({}): {} for URL {}", e.getStatusCode(), e.getMessage(), url);
        } catch (HttpServerErrorException e) {
            logger.error("Server error ({}): {} for URL {}", e.getStatusCode(), e.getMessage(), url);
        } catch (ResourceAccessException e) {
            logger.error("Network error: {} for URL {}", e.getMessage(), url);
        } catch (RestClientException e) {
            logger.error("Unexpected RestTemplate error: {} for URL {}", e.getMessage(), url);
        }
    }

    private Double getIndexOrDefault(List<Species> speciesList, String speciesCode) {
        if (speciesList == null || speciesList.isEmpty()) return null;
    
        for (Species species : speciesList) {
            if (species.getCode().equalsIgnoreCase(speciesCode)) {
                String band = species.getBand();
                String indexStr = species.getIndex();
    
                if ("No data".equalsIgnoreCase(band)) {
                    return null;
                }
    
                try {
                    double index = Double.parseDouble(indexStr);
                    return index > 0 ? index : null;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public void refreshLocationData(Location location) throws JsonMappingException, JsonProcessingException {
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