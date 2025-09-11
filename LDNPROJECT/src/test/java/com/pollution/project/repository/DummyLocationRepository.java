package com.pollution.project.repository;

import com.pollution.project.entity.Location;
import com.pollution.project.repository.LocationRepository;

import java.util.*;

class DummyLocationRepository {
    private Map<Long, Location> storage = new HashMap<>();
    private long counter = 1;

    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Location save(Location location) {
        if (location.getId() == null) location.setId(counter++);
        storage.put(location.getId(), location);
        return location;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    public Collection<Location> findAll() {
        return storage.values();
    }
}