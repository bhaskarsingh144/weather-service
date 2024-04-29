package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.weather.WeatherInfo;

import java.util.List;

public interface WeatherService {

    WeatherInfo fetchWeather(String pincode, String date);

    List<WeatherInfo> findAll();
}