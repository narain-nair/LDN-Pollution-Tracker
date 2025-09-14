package com.pollution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MonitoringSiteResponse {
    @JsonProperty("Sites")
    private SiteWrapper sitesWrapper;

    public MonitoringSite[] getMonitoringSites() {
        return sitesWrapper != null ? sitesWrapper.getMonitoringSites() : new MonitoringSite[0];
    }
}

class SiteWrapper {
    @JsonProperty("MonitoringSite")
    private MonitoringSite[] monitoringSites;

    public MonitoringSite[] getMonitoringSites() {
        return monitoringSites;
    }

    public void setMonitoringSites(MonitoringSite[] monitoringSites) {
        this.monitoringSites = monitoringSites;
    }
}