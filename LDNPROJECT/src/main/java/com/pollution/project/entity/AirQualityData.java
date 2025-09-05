package com.pollution.project.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Embeddable;

@Embeddable
public class AirQualityData {
    private double pm25;
    private double pm10;
    private double no2;
    private double so2;
    private double o3;
    private double co;
    private LocalDateTime timestamp;

    // Constructors, getters, and setters
    public AirQualityData() {}
    public AirQualityData(double pm25, double pm10, double no2, double so2, double o3, double co, LocalDateTime timestamp) {
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.no2 = no2;
        this.so2 = so2;
        this.o3 = o3;
        this.co = co;
        this.timestamp = timestamp;
    }

    public double getPm25() {
        return pm25;
    }

    public void setPm25(double pm25) {
        this.pm25 = pm25;
    }

    public double getPm10() {
        return pm10;
    }

    public void setPm10(double pm10) {
        this.pm10 = pm10;
    }

    public double getNo2() {
        return no2;
    }

    public void setNo2(double no2) {
        this.no2 = no2;
    }

    public double getSo2() {
        return so2;
    }

    public void setSo2(double so2) {
        this.so2 = so2;
    }

    public double getO3() {
        return o3;
    }

    public void setO3(double o3) {
        this.o3 = o3;
    }

    public double getCo() {
        return co;
    }

    public void setCo(double co) {
        this.co = co;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AirQualityData{" +
                "pm25=" + pm25 +
                ", pm10=" + pm10 +
                ", no2=" + no2 +
                ", so2=" + so2 +
                ", o3=" + o3 +
                ", co=" + co +
                ", timestamp=" + timestamp +
                '}';
    }
}