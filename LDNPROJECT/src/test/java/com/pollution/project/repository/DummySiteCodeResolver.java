package com.pollution.project.controller;

import com.pollution.model.AirQualityData;
import com.pollution.model.Location;
import com.pollution.service.SiteCodeResolver;

import java.time.LocalDateTime;

class DummySiteCodeResolver extends SiteCodeResolver {

    public DummySiteCodeResolver() {
        super(null);
    }

    @Override
    public void populateLocationData(Location location, String userInput) {
        location.setSiteCode("DUMMY");
        location.setAirQualityData(new AirQualityData(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, LocalDateTime.now()));
    }
}