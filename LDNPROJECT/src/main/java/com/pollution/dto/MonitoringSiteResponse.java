package com.pollution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MonitoringSiteResponse {
    @JsonProperty("MonitoringSite")
    private MonitoringSite[] monitoringSites; // array of sites

    public MonitoringSite[] getMonitoringSites() {
        return monitoringSites;
    }

    public void setMonitoringSites(MonitoringSite[] monitoringSites) {
        this.monitoringSites = monitoringSites;
    }
}