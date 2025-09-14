package com.pollution.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MonitoringSiteResponse {
    @JsonProperty("Sites")
    private SiteWrapper sitesWrapper;

    public MonitoringSiteResponse() {}

    public MonitoringSiteResponse(SiteWrapper sitesWrapper) {
        this.sitesWrapper = sitesWrapper;
    }

    public MonitoringSite[] getMonitoringSites() {
        return sitesWrapper != null ? sitesWrapper.getMonitoringSites() : new MonitoringSite[0];
    }

    public void setMonitoringSites(MonitoringSite[] monitoringSites) {
        if (this.sitesWrapper == null) {
            this.sitesWrapper = new SiteWrapper();
        }
        this.sitesWrapper.setMonitoringSites(monitoringSites);
    }
}

class SiteWrapper {
    @JsonProperty("Site")
    private MonitoringSite[] monitoringSites;

    public MonitoringSite[] getMonitoringSites() {
        return monitoringSites;
    }

    public void setMonitoringSites(MonitoringSite[] monitoringSites) {
        this.monitoringSites = monitoringSites;
    }
}