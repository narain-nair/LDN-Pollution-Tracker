package com.pollution.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pollution.project.entity.Location;
import com.pollution.project.repository.LocationRepository;
import com.pollution.project.service.SiteCodeResolver;

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
        return ResponseEntity.ok(location);
    }

    @GetMapping
    public ResponseEntity<?> getSiteByCoords(@RequestParam double lat, @RequestParam double lng) {
        Location tempLoc = new Location("temp", lat, lng);
        siteCodeResolver.populateLocationData(tempLoc, null);

        if (tempLoc.getAirQualityData() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the given coordinates");
        }

        return ResponseEntity.ok(tempLoc);
    }
}