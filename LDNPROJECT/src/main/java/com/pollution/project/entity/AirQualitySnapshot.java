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

    public AirQualitySnapshot() {}

    public AirQualitySnapshot(LocalDateTime timestamp, Double pm25, Double pm10, Double no2, Double so2, Double o3, Double co, Location location) {
        this.timestamp = timestamp;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.no2 = no2;
        this.so2 = so2;
        this.o3 = o3;
        this.co = co;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getSo2() {
        return so2;
    }

    public void setSo2(Double so2) {
        this.so2 = so2;
    }

    public Double getO3() {
        return o3;
    }

    public void setO3(Double o3) {
        this.o3 = o3;
    }

    public Double getCo() {
        return co;
    }

    public void setCo(Double co) {
        this.co = co;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}