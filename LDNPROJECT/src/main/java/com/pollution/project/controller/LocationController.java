package com.pollution.project.controller;

import java.util.HashMap;
import java.util.Map;

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

import com.pollution.dto.LocationRequest;
import com.pollution.project.entity.Location;
import com.pollution.project.repository.LocationRepository;
import com.pollution.project.service.SiteCodeResolver;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/locations")
public class LocationController{
    private final SiteCodeResolver siteCodeResolver;
    private final LocationRepository locationRepository;

    public LocationController (SiteCodeResolver siteCodeResolver, LocationRepository locationRepository) {
        this.siteCodeResolver = siteCodeResolver;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationData(@PathVariable Long id) {
        var locationOpt = locationRepository.findById(id);
        if (locationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found");
        }

        var location = locationOpt.get();
        siteCodeResolver.populateLocationData(location, location.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("location", location);
        response.put("airQualityData", location.getAirQualityData());
        response.put("message", "Location data retrieved successfully.");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getSiteByCoords(@RequestParam double lat, @RequestParam double lng) {
        Location tempLoc = new Location("temp", lat, lng);
        siteCodeResolver.populateLocationData(tempLoc, null);

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
        Location location = new Location(request.getSiteName(), request.getLat(), request.getLng());

        siteCodeResolver.populateLocationData(location, request.getSiteName());
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
        var locationOpt = locationRepository.findById(id);
        if (locationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found");
        }

        var location = locationOpt.get();
        siteCodeResolver.populateLocationData(location, null);

        locationRepository.save(location);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Location data refreshed successfully.");
        response.put("location", location);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found");
        }

        locationRepository.deleteById(id);
        return ResponseEntity.ok("Location deleted successfully.");
    }
}