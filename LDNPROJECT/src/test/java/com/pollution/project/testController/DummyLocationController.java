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
        this.siteCodeResolver = new DummySiteCodeResolver();
        this.locationRepository = new DummyLocationRepository();
        this.snapshotRepository = new DummySnapshotRepository();
    }
}
