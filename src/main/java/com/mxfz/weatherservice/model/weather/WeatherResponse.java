package com.mxfz.weatherservice.model.weather;

import lombok.Data;

import java.util.List;

@Data
public
class WeatherResponse {

    private double lat;
    private double lon;
    private String timezone;
    private int timezoneOffset;
    private List<WeatherData> data;
}



