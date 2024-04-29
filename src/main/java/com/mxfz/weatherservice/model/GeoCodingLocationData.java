package com.mxfz.weatherservice.model;

import lombok.Data;

import java.util.Map;

@Data
public class GeoCodingLocationData {

    private String name;
    private Map<String, String> localNames;
    private double lat;
    private double lon;
    private String country;
    private String state;

    public GeoCodingLocationData(String name, Map<String, String> localNames, double lat, double lon,
                                 String country, String state) {
        this.name = name;
        this.localNames = localNames;
        this.lat = lat;
        this.lon = lon;
        this.country = country;
        this.state = state;
    }
}