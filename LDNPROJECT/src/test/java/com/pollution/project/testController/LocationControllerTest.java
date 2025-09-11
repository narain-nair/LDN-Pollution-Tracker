package com.pollution.project.testController;

import com.pollution.project.entity.AirQualityData;
import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location;
import com.pollution.project.repository.AirQualitySnapshotRepository;
import com.pollution.project.repository.LocationRepository;
import com.pollution.project.service.SiteCodeResolver;

import com.pollution.project.testRepository.DummyLocationRepository;
import com.pollution.project.testRepository.DummySiteCodeResolver;
import com.pollution.project.testRepository.DummySnapshotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; 
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class LocationControllerTest {
    private DummyLocationController controller;
    private DummyLocationRepository locationRepo;
    private DummySnapshotRepository snapshotRepo;
    private DummySiteCodeResolver siteCodeResolver;

    @BeforeEach
    void setup() {
        locationRepo = new DummyLocationRepository();
        snapshotRepo = new DummySnapshotRepository();
        siteCodeResolver = new DummySiteCodeResolver();

        controller = new DummyLocationController(siteCodeResolver, locationRepo, snapshotRepo);
    }

    @Test
    void testAddLocation_Valid() {
        Location loc = new Location("Test Location", 51.5, 0.1);
        ResponseEntity<?> response = controller.addLocation(loc);

        assertEquals(201, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Location saved = (Location) body.get("location");
        assertNotNull(saved.getId());
        assertEquals("DUMMY", saved.getSiteCode());
        assertNotNull(saved.getAirQualityData());
    }

    @Test
    void testAddLocation_NullName() {
        Location loc = new Location(null, 51.5, 0.1);
        ResponseEntity<?> response = controller.addLocation(loc);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testAddLocation_EmptyName() {
        Location loc = new Location("", 51.5, 0.1);
        ResponseEntity<?> response = controller.addLocation(loc);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testAddLocation_InvalidCoordinates() {
        Location loc = new Location("Test", 100.0, 0.0); // latitude > 90
        ResponseEntity<?> response = controller.addLocation(loc);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testGetLocationData_Valid() {
        Location loc = new Location("Test Location", 51.5, 0.1);
        controller.addLocation(loc);

        ResponseEntity<?> response = controller.getLocationData(loc.getId());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetLocationData_NotFound() {
        ResponseEntity<?> response = controller.getLocationData(999L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetLocationData_NoAQData() {
        siteCodeResolver.setReturnNullSiteCode(true);
        Location loc = new Location("Test", 51.5, 0.1);
        controller.addLocation(loc);
    
        ResponseEntity<?> response = controller.getLocationData(loc.getId());
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteLocation_Valid() {
        Location loc = new Location("Test", 51.5, 0.1);
        controller.addLocation(loc);

        ResponseEntity<?> response = controller.deleteLocation(loc.getId());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test  
    void testDeleteLocation_NotFound() {
        ResponseEntity<?> response = controller.deleteLocation(999L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test  
    void testGetLocation_NoSnapshots() {
        Location loc = new Location("Test", 51.5, 0.1);
        controller.addLocation(loc);

        ResponseEntity<?> response = controller.getLocationStats(loc.getId());
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        List<AirQualitySnapshot> snapshots = (List<AirQualitySnapshot>) body.get("snapshots");
        assertTrue(snapshots.isEmpty());
    }

    @Test
    void testGetLocationStats_WithSnapshots() {
        Location loc = new Location("Test", 51.5, 0.1);
        controller.addLocation(loc);
    
        AirQualitySnapshot s1 = new AirQualitySnapshot();
        s1.setPm25(5.0); s1.setPm10(10.0); s1.setNo2(15.0); s1.setSo2(20.0); s1.setO3(25.0); s1.setCo(30.0);
        controller.addSnapshot(loc.getId(), s1);
    
        ResponseEntity<?> response = controller.getLocationStats(loc.getId());
        Map<String,Object> stats = (Map<String,Object>) response.getBody();
        assertEquals(5.0, stats.get("averagePm25"));
        assertEquals(10.0, stats.get("averagePm10"));
    }
}