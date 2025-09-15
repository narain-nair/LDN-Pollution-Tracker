package com.pollution.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pollution.dto.MonitoringSite;
import com.pollution.project.entity.Location;
import com.pollution.project.repository.LocationRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DBInitialiser {
    private final LocationRepository locationRepository;
    private final SiteCodeResolver siteCodeResolver;

    @Autowired
    public DBInitialiser(LocationRepository locationRepository, SiteCodeResolver siteCodeResolver) {
        this.locationRepository = locationRepository;
        this.siteCodeResolver = siteCodeResolver;
    }

    @PostConstruct
    public void seedLocations() {
        MonitoringSite[] sites = siteCodeResolver.fetchMonitoringSites().getMonitoringSites();
        for (MonitoringSite site : sites) {
            if (site.getLatitude() != null && site.getLongitude() != null) {
                Location loc = new Location();
                loc.setName(site.getSiteName());
                loc.setLatitude(Double.parseDouble(site.getLatitude()));
                loc.setLongitude(Double.parseDouble(site.getLongitude()));
                loc.setSiteCode(site.getSiteCode());
                locationRepository.save(loc);
            }
        }
    }
}
