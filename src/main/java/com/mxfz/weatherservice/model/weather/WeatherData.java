package com.mxfz.weatherservice.model.weather;

import lombok.Data;

import java.util.List;

@Data
public class WeatherData {

    private long dt;
    private long sunrise;
    private long sunset;
    private double temp;
    private double feelsLike;
    private int pressure;
    private int humidity;
    private double dewPoint;
    private double uvi;
    private int clouds;
    private int visibility;
    private double windSpeed;
    private int windDeg;
    private List<Weather> weather;
}