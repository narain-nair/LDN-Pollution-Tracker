package com.pollution.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.pollution.project.repository.AirQualitySnapshotRepository;
import com.pollution.project.repository.LocationRepository;
import com.pollution.project.service.SiteCodeResolver;

@SpringBootTest
class PollutionProjectApplicationTests {

    @MockBean
    private SiteCodeResolver siteCodeResolver;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private AirQualitySnapshotRepository snapshotRepository;

    @Test
    void contextLoads() {
    }
}