package com.pollution.dto;

public class SuggestionDTO {
    private String name;
    private String siteCode;

    public SuggestionDTO() { }

    public SuggestionDTO(String name, String siteCode) {
        this.name = name;
        this.siteCode = siteCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }
}
