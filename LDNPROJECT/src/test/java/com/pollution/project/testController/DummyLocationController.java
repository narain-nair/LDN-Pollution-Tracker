package com.pollution.project.testController;

import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location;
import com.pollution.project.testRepository.DummyLocationRepository;
import com.pollution.project.testRepository.DummySiteCodeResolver;
import com.pollution.project.testRepository.DummySnapshotRepository;

import java.util.*;

public class DummyLocationController {
    private final Map<Long, Location> locationStorage = new HashMap<>();
    private final DummySiteCodeResolver siteCodeResolver;
    private final DummyLocationRepository locationRepository;
    private final Map<Long, List<AirQualitySnapshot>> snapshotStorage = new HashMap<>();
    private long counter = 1;
    private static final Logger logger = LoggerFactory.getLogger(DummyLocationController.class);

    public DummyLocationController(DummySiteCodeResolver siteCodeResolver, DummyLocationRepository locationRepository) {
        this.siteCodeResolver = siteCodeResolver;
        this.locationRepository = locationRepository;
    }

    public ResponseEntity<?> addLocation(Location location) {
        if (location.getName() == null || location.getName().isEmpty()) {
            logger.warn("Attempted to add location with null or empty name");
            return ResponseEntity.status(400).body("Location name cannot be null or empty");
        }
        if (location.getLatitude() < -90 || location.getLatitude() > 90 ||
            location.getLongitude() < -180 || location.getLongitude() > 180) {
            logger.warn("Attempted to add location with invalid latitude or longitude");
            return ResponseEntity.status(400).body("Invalid latitude or longitude");
        }    

        if (location.getId() == null) location.setId(counter++);
        siteCodeResolver.populateLocationData(location, location.getName());
        locationStorage.put(location.getId(), location);
        locationRepository.save(location); // optional if you want dummy repo tracking
    
        Map<String, Object> response = Map.of("message", "Location added successfully.", "location", location);
        return ResponseEntity.status(201).body(response);
    }   

    public ResponseEntity<?> getLocationData(Long id) {
        Location loc = locationStorage.get(id);
        if (loc == null) {
            logger.warn("Location with id {} not found", id);
            return ResponseEntity.status(404).body("Location not found");
        }
    
        siteCodeResolver.populateLocationData(loc, loc.getName());
    
        if (loc.getAirQualityData() == null) {
            logger.warn("No air quality data found for location id {}", id);
            return ResponseEntity.status(404).body(Map.of("error", "No air quality data found", "siteCode", loc.getSiteCode()));
        }
    
        return ResponseEntity.ok(Map.of("location", loc, "airQualityData", loc.getAirQualityData(), "message", "Location data retrieved successfully."));
    }

    public ResponseEntity<?> deleteLocation(Long id) {
        if (!locationStorage.containsKey(id)) {
            logger.warn("Attempted to delete non-existent location with id {}", id);
            return ResponseEntity.status(404).body("Location not found");
        }
        locationStorage.remove(id);
        return ResponseEntity.ok("Location deleted successfully.");
    }

    public ResponseEntity<?> getLocationStats(Long id) {
        List<AirQualitySnapshot> snaps = snapshotStorage.getOrDefault(id, List.of());
    
        Map<String, Object> response = Map.of(
            "locationId", id,
            "averagePm25", snaps.stream().mapToDouble(AirQualitySnapshot::getPm25).average().orElse(0.0),
            "averagePm10", snaps.stream().mapToDouble(AirQualitySnapshot::getPm10).average().orElse(0.0),
            "averageNo2", snaps.stream().mapToDouble(AirQualitySnapshot::getNo2).average().orElse(0.0),
            "averageSo2", snaps.stream().mapToDouble(AirQualitySnapshot::getSo2).average().orElse(0.0),
            "averageO3", snaps.stream().mapToDouble(AirQualitySnapshot::getO3).average().orElse(0.0),
            "averageCo", snaps.stream().mapToDouble(AirQualitySnapshot::getCo).average().orElse(0.0)
        );
        return ResponseEntity.ok(response);
    }

    public void addSnapshot(Long locationId, AirQualitySnapshot snapshot) {
        snapshotStorage.computeIfAbsent(locationId, k -> new ArrayList<>()).add(snapshot);
    }

    public ResponseEntity<?> getSiteByCoords(double lat, double lng) {
        Location tempLoc = new Location("temp", lat, lng);
        siteCodeResolver.populateLocationData(tempLoc, null);
    
        if (tempLoc.getAirQualityData() == null) {
            logger.warn("No air quality data found for coordinates ({}, {})", lat, lng);
            return ResponseEntity.status(404).body("No data found for the given coordinates");
        }
    
        return ResponseEntity.ok(Map.of("siteCode", tempLoc.getSiteCode(), "airQualityData", tempLoc.getAirQualityData()));
    }

    public ResponseEntity<?> refreshLocation(Long id) {
        Location loc = locationStorage.get(id);
        if (loc == null) {
            logger.warn("Attempted to refresh non-existent location with id {}", id);
            return ResponseEntity.status(404).body("Location not found");
        }
    
        siteCodeResolver.populateLocationData(loc, null);
    
        if (loc.getAirQualityData() == null) {
            logger.warn("No air quality data found for location id {}", id);
            return ResponseEntity.status(404).body(Map.of("error", "No air quality data found for this location"));
        }
    
        locationStorage.put(id, loc); // refresh in-memory
    
        return ResponseEntity.ok(Map.of("message", "Location data refreshed successfully.", "location", loc));
    }

    public void deleteAllLocations() {
        locationStorage.clear();
        snapshotStorage.clear();
        counter = 1;
    }
}
