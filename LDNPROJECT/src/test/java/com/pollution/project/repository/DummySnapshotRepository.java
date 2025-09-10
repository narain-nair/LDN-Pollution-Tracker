package com.pollution.project.controller;

import com.pollution.model.AirQualitySnapshot;
import com.pollution.repository.AirQualitySnapshotRepository;

import java.util.*;

class DummySnapshotRepository implements AirQualitySnapshotRepository {
    private Map<Long, List<AirQualitySnapshot>> snapshots = new HashMap<>();

    @Override
    public List<AirQualitySnapshot> findByLocationId(Long id) {
        return snapshots.getOrDefault(id, Collections.emptyList());
    }

    public void addSnapshot(Long locationId, AirQualitySnapshot snapshot) {
        snapshots.computeIfAbsent(locationId, k -> new ArrayList<>()).add(snapshot);
    }
}