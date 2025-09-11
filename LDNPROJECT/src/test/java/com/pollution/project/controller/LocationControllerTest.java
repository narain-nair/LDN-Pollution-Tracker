package com.pollution.project.controller;

import com.pollution.project.entity.AirQualityData;
import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location;
import com.pollution.project.repository.AirQualitySnapshotRepository;
import com.pollution.project.repository.LocationRepository;
import com.pollution.project.service.SiteCodeResolver;
import com.pollution.project.controller.LocationController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


public class LocationControllerTest {
    
}