package com.pollution.project.controller;

import com.pollution.dto.AirQualityData;
import com.pollution.model.AirQualitySnapshot;
import com.pollution.model.Location;
import com.pollution.repository.AirQualitySnapshotRepository;
import com.pollution.repository.LocationRepository;
import com.pollution.service.SiteCodeResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocationControllerTest {

    private LocationController controller;
    private DummyLocationRepository locationRepo;
    private DummySnapshotRepository snapshotRepo;
    private DummySiteCodeResolver siteCodeResolver;

    @BeforeEach
    void setup() {
        locationRepo = new DummyLocationRepository();
        snapshotRepo = new DummySnapshotRepository();
        siteCodeResolver = new DummySiteCodeResolver();
        controller = new LocationController(siteCodeResolver, locationRepo, snapshotRepo);
    }

    @Test
    void testGetLocationData_Valid() {
        Location loc = new Location("Dummy Location", 51.0, 0.0);
        loc.setId(1L);
        locationRepo.save(loc);

        ResponseEntity<?> response = controller.getLocationData(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertEquals(loc, body.get("location"));
        assertNotNull(body.get("airQualityData"));
    }

    @Test
    void testAddLocation_CreatesLocation() {
        var request = new LocationRequest();
        request.setSiteName("New Site");
        request.setLat(51.0);
        request.setLng(0.0);

        ResponseEntity<?> response = controller.addLocation(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<?,?> body = (Map<?,?>) response.getBody();
        Location loc = (Location) body.get("location");
        assertEquals("DUMMY", loc.getSiteCode());
        assertEquals("New Site", loc.getName());
    }

    @Test
    void testDeleteLocation() {
        Location loc = new Location("To Delete", 51.0, 0.0);
        loc.setId(1L);
        locationRepo.save(loc);

        ResponseEntity<?> response = controller.deleteLocation(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Location deleted successfully.", response.getBody());
        assertFalse(locationRepo.existsById(1L));
    }

    @Test
    void testGetLocationStats() {
        Location loc = new Location("Stats Loc", 51.0, 0.0);
        loc.setId(1L);
        locationRepo.save(loc);

        AirQualitySnapshot snap1 = new AirQualitySnapshot();
        snap1.setPm25(2.0); snap1.setPm10(3.0); snap1.setNo2(4.0);
        snap1.setSo2(5.0); snap1.setO3(6.0); snap1.setCo(7.0);
        snapshotRepo.addSnapshot(1L, snap1);

        AirQualitySnapshot snap2 = new AirQualitySnapshot();
        snap2.setPm25(4.0); snap2.setPm10(5.0); snap2.setNo2(6.0);
        snap2.setSo2(7.0); snap2.setO3(8.0); snap2.setCo(9.0);
        snapshotRepo.addSnapshot(1L, snap2);

        ResponseEntity<?> response = controller.getLocationStats(1L);
        Map<?,?> body = (Map<?,?>) response.getBody();

        assertEquals(3.0, body.get("averagePm25"));
        assertEquals(4.0, body.get("averagePm10"));
        assertEquals(5.0, body.get("averageNo2"));
        assertEquals(6.0, body.get("averageSo2"));
        assertEquals(7.0, body.get("averageO3"));
        assertEquals(8.0, body.get("averageCo"));
    }
}