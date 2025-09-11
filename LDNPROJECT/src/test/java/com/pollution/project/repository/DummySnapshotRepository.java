package com.pollution.project.repository;

import com.pollution.project.entity.AirQualitySnapshot;

import java.util.*;

class DummySnapshotRepository {
    private Map<Long, List<AirQualitySnapshot>> snapshots = new HashMap<>();

    public List<AirQualitySnapshot> findByLocationId(Long id) {
        return snapshots.getOrDefault(id, Collections.emptyList());
    }

    public void addSnapshot(Long locationId, AirQualitySnapshot snapshot) {
        snapshots.computeIfAbsent(locationId, k -> new ArrayList<>()).add(snapshot);
    }
}