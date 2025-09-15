package com.pollution.project.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.pollution.dto.LocationRequest;
import com.pollution.dto.MonitoringSite;
import com.pollution.dto.HourlyIndexResponse.Site;
import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location;
import com.pollution.project.repository.AirQualitySnapshotRepository;
import com.pollution.project.repository.LocationRepository;
import com.pollution.project.service.SiteCodeResolver;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/locations")
public class LocationController {
    private final SiteCodeResolver siteCodeResolver;
    private final LocationRepository locationRepository;
    private final AirQualitySnapshotRepository snapshotRepository;
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    public LocationController (SiteCodeResolver siteCodeResolver, LocationRepository locationRepository, AirQualitySnapshotRepository snapshotRepository) {
        System.out.println("LocationController initialized!");
        this.siteCodeResolver = siteCodeResolver;
        this.locationRepository = locationRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seedLocations() {
        try {
            MonitoringSite[] sites = siteCodeResolver.fetchMonitoringSites().getMonitoringSites();
            int count = 0;
    
            for (MonitoringSite site : sites) {
                if (site.getSiteName() == null || site.getSiteCode() == null) continue;
                
                String siteCode = site.getSiteCode().trim().toLowerCase();
                boolean exists = locationRepository.existsBySiteCode(siteCode); 
                if (!exists) {
                    try {
                        double lat = Double.parseDouble(site.getLatitude());
                        double lng = Double.parseDouble(site.getLongitude());
    
                        Location loc = new Location();
                        loc.setName(site.getSiteName());
                        loc.setLatitude(lat);
                        loc.setLongitude(lng);
                        loc.setSiteCode(site.getSiteCode());
    
                        locationRepository.save(loc);
                        count++;

                        try {
                            siteCodeResolver.populateLocationData(loc, loc.getName());
                            locationRepository.save(loc);
                        } catch (JsonMappingException e) {
                            // TODO Auto-generated catch block
                            logger.error("Error populating location data for location ID {}: {}", loc.getId(), e.getMessage());
                            e.printStackTrace();
                        } catch (JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            logger.error("Error populating location data for location ID {}: {}", loc.getId(), e.getMessage());
                            e.printStackTrace();
                        }

                    } catch (NumberFormatException e) {
                        logger.warn("Skipping site {} due to invalid coordinates: {}", site.getSiteName(), e.getMessage());
                    }
                }
            }
    
            return ResponseEntity.ok("Seeded " + count + " locations successfully.");
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to seed locations: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllLocations() {
        List<Location> locations = locationRepository.findAll();

        for (Location loc : locations) {
            try {
                siteCodeResolver.populateLocationData(loc, loc.getName());
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                logger.error("Error populating location data for location ID {}: {}", loc.getId(), e.getMessage());
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                logger.error("Error populating location data for location ID {}: {}", loc.getId(), e.getMessage());
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(locations);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationData(@PathVariable Long id) {
        logger.info("GET /locations/{} called", id);

        var locationOpt = locationRepository.findById(id);
        if (locationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found");
        }

        var location = locationOpt.get();
        try {
            siteCodeResolver.populateLocationData(location, location.getName());
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            logger.error("Error populating location data for location ID {}: {}", id, e.getMessage());
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            logger.error("Error populating location data for location ID {}: {}", id, e.getMessage());
            e.printStackTrace();
        }

        if (location.getAirQualityData() == null) {
            Map<String, Object> faultyRes = new HashMap<>();
            faultyRes.put("error", "No air quality data found for this location");
            faultyRes.put("siteCode", location.getSiteCode());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(faultyRes);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("location", location);
        response.put("airQualityData", location.getAirQualityData());
        response.put("message", "Location data retrieved successfully.");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getSiteByCoords(@RequestParam double lat, @RequestParam double lng) {
        Location tempLoc = new Location("temp", lat, lng);
        try {
            siteCodeResolver.populateLocationData(tempLoc, null);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            logger.error("Error populating location data for coordinates ({}, {}): {}", lat, lng, e.getMessage());
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            logger.error("Error populating location data for coordinates ({}, {}): {}", lat, lng, e.getMessage());
            e.printStackTrace();
        }

        if (tempLoc.getAirQualityData() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the given coordinates");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("siteCode", tempLoc.getSiteCode());
        response.put("airQualityData", tempLoc.getAirQualityData());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addLocation(@Valid @RequestBody LocationRequest request) {
        logger.info("POST /locations called with lat={} lng={} siteName={}", request.getLat(), request.getLng(), request.getSiteName());
        Location location = new Location(request.getSiteName(), request.getLat(), request.getLng());

        try {
            siteCodeResolver.populateLocationData(location, request.getSiteName());
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            logger.error("Error populating location data for new location: {}", e.getMessage());
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            logger.error("Error populating location data for new location: {}", e.getMessage());
            e.printStackTrace();
        }
        if (location.getSiteCode() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not determine site code for the given location");
        }

        locationRepository.save(location);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Location added successfully.");
        response.put("location", location);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> refreshLocation(@PathVariable Long id) {
        logger.info("PUT /locations/{} called for refresh", id);

        var locationOpt = locationRepository.findById(id);
        if (locationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found");
        }

        var location = locationOpt.get();
        try {
            siteCodeResolver.populateLocationData(location, null);
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        locationRepository.save(location);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Location data refreshed successfully.");
        response.put("location", location);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        logger.info("DELETE /locations/{} called", id);
        
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found");
        }

        locationRepository.deleteById(id);
        return ResponseEntity.ok("Location deleted successfully.");
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getLocationStats(@PathVariable Long id) {
        var locationOpt = locationRepository.findById(id);
        if (locationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found");
        }

        var snapshots = snapshotRepository.findByLocationId(id);
        
        Double avgPm25 = snapshots.stream().mapToDouble(AirQualitySnapshot::getPm25).average().orElse(0.0);
        Double avgPm10 = snapshots.stream().mapToDouble(AirQualitySnapshot::getPm10).average().orElse(0.0);
        Double avgNo2 = snapshots.stream().mapToDouble(AirQualitySnapshot::getNo2).average().orElse(0.0);
        Double avgSo2 = snapshots.stream().mapToDouble(AirQualitySnapshot::getSo2).average().orElse(0.0);
        Double avgO3 = snapshots.stream().mapToDouble(AirQualitySnapshot::getO3).average().orElse(0.0);
        Double avgCo = snapshots.stream().mapToDouble(AirQualitySnapshot::getCo).average().orElse(0.0);

        Map<String,Object> response = Map.of(
            "locationId", id,
            "averagePm25", avgPm25,
            "averagePm10", avgPm10,
            "averageNo2", avgNo2,
            "averageSo2", avgSo2,
            "averageO3", avgO3,
            "averageCo", avgCo
        );

        return ResponseEntity.ok(response);
    }
}