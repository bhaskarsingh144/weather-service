package com.mxfz.weatherservice.controller;

import com.mxfz.weatherservice.model.weather.WeatherInfo;
import com.mxfz.weatherservice.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather-for-date-pincode")
    public ResponseEntity<WeatherInfo> getWeatherByPincodeAndDate(@RequestParam String pincode,
                                                                  @RequestParam String date) {
//      Sample date format: 2020-10-15
        WeatherInfo weatherResponse = weatherService.fetchWeather(pincode, date);
        if (weatherResponse == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(weatherResponse);
    }

    @GetMapping("/all-weather")
    public ResponseEntity<List<WeatherInfo>> getAllWeatherInfo() {
        return ResponseEntity.ok(weatherService.findAll());
    }
}
