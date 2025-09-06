package com.pollution.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pollution.project.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Location findByName(String name);
    boolean existsByName(String name);

    List<Location> findAllByUsers_Id(Long userId);
    List<Location> findAllByOrderByNameAsc();
    
    @Query("SELECT l FROM Location l WHERE l.latitude BETWEEN :minLat AND :maxLat AND l.longitude BETWEEN :minLong AND :maxLong")
    List<Location> findInLatLongRange(@Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
                                    @Param("minLong") Double minLong, @Param("maxLong") Double maxLong);
}
