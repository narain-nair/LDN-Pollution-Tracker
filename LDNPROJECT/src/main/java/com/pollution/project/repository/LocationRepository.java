package com.pollution.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pollution.project.entity.Location;
import com.pollution.project.entity.AirQualityData;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String name);
    boolean existsByName(String name);

    Optional<Location> findBySiteCode(String siteCode);
    boolean existsBySiteCode(String siteCode);

    List<Location> findAllBySiteCode(String siteCode);
    List<Location> findBySiteCodeContainingIgnoreCase(String siteCode);

    List<Location> findAllByUsers_Id(Long userId);
    List<Location> findAllByOrderByNameAsc();
    List<Location> findAllByName(String name);

    @Query("SELECT l.airQualityData FROM Location l WHERE l.id = :id")
    AirQualityData findAirQualityDataByLocationId(@Param("id") Long id);

    @Query("SELECT l FROM Location l WHERE l.latitude BETWEEN :minLat AND :maxLat AND l.longitude BETWEEN :minLong AND :maxLong")
    List<Location> findInLatLongRange(@Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
                                    @Param("minLong") Double minLong, @Param("maxLong") Double maxLong);

    @Query("SELECT l FROM Location l WHERE l.airQualityData.pm25 > :threshold")
    List<Location> findLocationsWithPm25Above(@Param("threshold") Double threshold);
}
