package com.pollution.project.controller;

import com.pollution.model.Location;
import com.pollution.repository.LocationRepository;

import java.util.*;

class DummyLocationRepository implements LocationRepository {
    private Map<Long, Location> storage = new HashMap<>();
    private long counter = 1;

    @Override
    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Location save(Location location) {
        if (location.getId() == null) location.setId(counter++);
        storage.put(location.getId(), location);
        return location;
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}