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
}