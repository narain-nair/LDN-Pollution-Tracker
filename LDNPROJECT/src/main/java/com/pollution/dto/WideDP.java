package com.pollution.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WideDP {
    @JsonProperty("pm25")
    private Double pm25;
    @JsonProperty("pm10")
    private Double pm10;
    @JsonProperty("no2")
    private Double no2;
    @JsonProperty("so2")
    private Double so2;
    @JsonProperty("o3")
    private Double o3;
    @JsonProperty("co")
    private Double co;
    @JsonProperty("DateTime")
    private String timestamp;

    @SuppressWarnings("unused")
    private void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm25() {
        return pm25;
    }

    @SuppressWarnings("unused")
    private void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getPm10() {
        return pm10;
    }
    @SuppressWarnings("unused")
    private void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getNo2() {
        return no2;
    }

    @SuppressWarnings("unused")
    private void setSo2(Double so2) {
        this.so2 = so2;
    }

    public Double getSo2() {
        return so2;
    }

    @SuppressWarnings("unused")
    private void setO3(Double o3) {
        this.o3 = o3;
    }

    public Double getO3() {
        return o3;
    }

    @SuppressWarnings("unused")
    private void setCo(Double co) {
        this.co = co;
    }

    public Double getCo() {
        return co;
    }

    @SuppressWarnings("unused")
    private void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
