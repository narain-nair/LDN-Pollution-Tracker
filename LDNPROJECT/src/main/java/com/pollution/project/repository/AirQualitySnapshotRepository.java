package com.pollution.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pollution.project.entity.AirQualitySnapshot;

public interface AirQualitySnapshotRepository extends JpaRepository<AirQualitySnapshot, Long> {
    List<AirQualitySnapshot> findByLocationId(Long locationId);
    List<AirQualitySnapshot> findByLocationIdOrderByTimestampDesc(Long locationId);
}