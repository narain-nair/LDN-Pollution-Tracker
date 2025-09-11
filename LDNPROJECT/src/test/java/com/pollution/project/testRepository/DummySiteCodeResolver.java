package com.pollution.project.testRepository;

import java.time.LocalDateTime;

import com.pollution.project.entity.AirQualityData;
import com.pollution.project.entity.Location;
import com.pollution.project.service.SiteCodeResolver;

public class DummySiteCodeResolver extends SiteCodeResolver {
    private boolean returnNullSiteCode = false;

    public DummySiteCodeResolver() {
        super();
    }

    public void setReturnNullSiteCode(boolean flag) {
        this.returnNullSiteCode = flag;
    }

    @Override
    public void populateLocationData(Location location, String userInput) {
        if (returnNullSiteCode) {
            location.setSiteCode(null);
            location.setAirQualityData(null);
        } else {
            location.setSiteCode("DUMMY");
            location.setAirQualityData(new AirQualityData(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, LocalDateTime.now()));
        }
    }
}