package com.pollution.project.testController;

import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location;
import com.pollution.project.testRepository.DummyLocationRepository;
import com.pollution.project.testRepository.DummySiteCodeResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map; 

import static org.junit.jupiter.api.Assertions.*;

public class LocationControllerTest {
    private DummyLocationController controller;
    private DummyLocationRepository locationRepo;
    private DummySiteCodeResolver siteCodeResolver;

    @BeforeEach
    void setup() {
        locationRepo = new DummyLocationRepository();
        siteCodeResolver = new DummySiteCodeResolver();

        controller = new DummyLocationController(siteCodeResolver, locationRepo);
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
    void testAddLocation_InvalidLongitude() {
        Location loc = new Location("Test", 50.0, 200.0); // longitude > 180
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
        Map<String,Object> stats = (Map<String,Object>) response.getBody();
        assertEquals(0.0, stats.get("averagePm25"));
        assertEquals(0.0, stats.get("averagePm10"));
    }

    @SuppressWarnings("null")
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

    @Test
    void testGetLocationStats_NotFound() {
        ResponseEntity<?> response = controller.getLocationStats(999L);
        assertEquals(404, response.getStatusCode().value());
    }

    @SuppressWarnings("null")
    @Test
    void testGetSiteByCoords_Valid() {
        ResponseEntity<?> response = controller.getSiteByCoords(51.5, 0.1);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("DUMMY", body.get("siteCode"));
        assertNotNull(body.get("airQualityData"));
    }

    @Test
    void testGetSiteByCoords_NoData() {
        siteCodeResolver.setReturnNullSiteCode(true);
        ResponseEntity<?> response = controller.getSiteByCoords(51.5, 0.1);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testRefreshLocation_Valid() {
        Location loc = new Location("Test", 51.5, 0.1);
        controller.addLocation(loc);

        ResponseEntity<?> response = controller.refreshLocation(loc.getId());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testRefreshLocation_NotFound() {
        ResponseEntity<?> response = controller.refreshLocation(999L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testRefreshLocation_NoAQData() {
        siteCodeResolver.setReturnNullSiteCode(true);
        Location loc = new Location("Test", 51.5, 0.1);
        controller.addLocation(loc);

        ResponseEntity<?> response = controller.refreshLocation(loc.getId());
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteAllLocations() {
        Location loc1 = new Location("Loc1", 51.5, 0.1);
        Location loc2 = new Location("Loc2", 52.5, 0.2);
        controller.addLocation(loc1);
        controller.addLocation(loc2);

        controller.deleteAllLocations();

        ResponseEntity<?> response1 = controller.getLocationData(loc1.getId());
        ResponseEntity<?> response2 = controller.getLocationData(loc2.getId());
        assertEquals(404, response1.getStatusCode().value());
        assertEquals(404, response2.getStatusCode().value());
    }
}