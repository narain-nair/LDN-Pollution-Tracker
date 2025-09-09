package com.pollution.project.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class AirQualitySnapshot {
    @Id @GeneratedValue
    private Long id;

    private LocalDateTime timestamp;

    private Double pm25;
    private Double pm10;
    private Double no2;
    private Double so2;
    private Double o3;
    private Double co;

    @ManyToOne
    private Location location; 
}