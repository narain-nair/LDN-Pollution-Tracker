package com.pollution.project.repository;

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