package com.pollution.project.testRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pollution.project.entity.AirQualitySnapshot;

public class DummySnapshotRepository extends AirQualitySnapshot{
    private final Map<Long, List<AirQualitySnapshot>> snapshots = new HashMap<>();

    public List<AirQualitySnapshot> findByLocationId(Long id) {
        return snapshots.getOrDefault(id, Collections.emptyList());
    }

    public void addSnapshot(Long locationId, AirQualitySnapshot snapshot) {
        snapshots.computeIfAbsent(locationId, k -> new ArrayList<>()).add(snapshot);
    }
}