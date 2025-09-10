package com.pollution.project.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.pollution.project.entity.Location;
import com.pollution.project.entity.AirQualityData;
import com.pollution.project.repository.LocationRepository;
import com.pollution.project.repository.AirQualitySnapshotRepository;
import com.pollution.project.service.SiteCodeResolver;

