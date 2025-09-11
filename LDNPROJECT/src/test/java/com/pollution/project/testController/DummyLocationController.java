package com.pollution.project.testController;

import com.pollution.project.entity.AirQualityData;
import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location;
import com.pollution.dto.LocationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pollution.project.testRepository.DummyLocationRepository;
import com.pollution.project.testRepository.DummySiteCodeResolver;
import com.pollution.project.testRepository.DummySnapshotRepository;

import java.util.*;
import java.time.LocalDateTime;

public class DummyLocationController {
    private final Map<Long, Location> locationStorage = new HashMap<>();
    private final DummySiteCodeResolver siteCodeResolver;
    private final DummyLocationRepository locationRepository;
    private final DummySnapshotRepository snapshotRepository;
    private final Map<Long, List<AirQualitySnapshot>> snapshotStorage = new HashMap<>();
    private long counter = 1;

    public DummyLocationController(DummySiteCodeResolver siteCodeResolver, DummyLocationRepository locationRepository, DummySnapshotRepository snapshotRepository) {
        this.siteCodeResolver = siteCodeResolver;
        this.locationRepository = locationRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public ResponseEntity<?> addLocation(Location location) {
        if (location.getId() == null) location.setId(counter++);
        siteCodeResolver.populateLocationData(location, location.getName());
        locationStorage.put(location.getId(), location);
        locationRepository.save(location); // optional if you want dummy repo tracking
    
        Map<String, Object> response = Map.of("message", "Location added successfully.", "location", location);
        return ResponseEntity.status(201).body(response);
    }   

    public ResponseEntity<?> getLocationData(Long id) {
        Location loc = locationStorage.get(id);
        if (loc == null) return ResponseEntity.status(404).body("Location not found");
    
        siteCodeResolver.populateLocationData(loc, loc.getName());
    
        if (loc.getAirQualityData() == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "No air quality data found", "siteCode", loc.getSiteCode()));
        }
    
        return ResponseEntity.ok(Map.of("location", loc, "airQualityData", loc.getAirQualityData(), "message", "Location data retrieved successfully."));
    }

    public ResponseEntity<?> deleteLocation(Long id) {
        if (!locationStorage.containsKey(id)) return ResponseEntity.status(404).body("Location not found");
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
}
