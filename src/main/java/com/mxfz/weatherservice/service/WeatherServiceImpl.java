package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.PincodeLocation;
import com.mxfz.weatherservice.model.weather.WeatherData;
import com.mxfz.weatherservice.model.weather.WeatherInfo;
import com.mxfz.weatherservice.model.weather.WeatherResponse;
import com.mxfz.weatherservice.repository.WeatherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final RestService restService;
    private final GeoCodingService geoCodingService;

    @Value("${open.weather.api.key}")
    private String openWeatherApiKey;

    public WeatherServiceImpl(RestService restService, WeatherRepository weatherRepository,
                              GeoCodingService geoCodingService) {
        this.restService = restService;
        this.weatherRepository = weatherRepository;
        this.geoCodingService = geoCodingService;
    }

    @Override
    public WeatherInfo fetchWeather(String pincode, String date) {
        WeatherInfo savedWeatherData = weatherRepository.findByPincodeAndDate(pincode, date);

        if (savedWeatherData != null) {
            log.info("fetching weather info from db");
            return savedWeatherData;
        }
        log.info("Calling GoogleGeoCodingAPI for latitude/longitude info");
        PincodeLocation location = geoCodingService.fetchLocationDetails(pincode);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        long dt = convertToUnixTime(date);
        WeatherResponse res = callOpenWeatherAPI(dt, latitude, longitude);
        WeatherInfo weatherData = mapWeatherData(res, pincode);
        return weatherRepository.save(weatherData);
    }

    @Override
    public List<WeatherInfo> findAll() {
        return weatherRepository.findAll();
    }

    private WeatherResponse callOpenWeatherAPI(long dt, double latitude, double longitude) {
        log.info("Calling OpenWeatherAPI");
        String uriString = "https://api.openweathermap.org/data/3.0/onecall/timemachine";
        URI uri = URI.create(uriString);
        byte[] bytes = Base64.getDecoder().decode(openWeatherApiKey);
        String key = new String(bytes, StandardCharsets.UTF_8);

        Map<String, ?> params = Map.of(
                "lat", latitude,
                "lon", longitude,
                "dt", dt,
                "appid", key
        );
        return restService.callApi(
                uri,
                HttpMethod.GET,
                HttpHeaders.EMPTY,
                params,
                null,
                ParameterizedTypeReference.forType(WeatherResponse.class)
        );
    }

    private long convertToUnixTime(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = sdf.parse(dateString);
            return date.getTime() / 1000;
        } catch (ParseException e) {
            log.error("Error parsing date string: {}", dateString, e);
            return -1;
        }
    }

    public WeatherInfo mapWeatherData(WeatherResponse weatherResponse, String pincode) {
        if (weatherResponse.getData() == null || weatherResponse.getData().isEmpty()) {
            return null;
        }

        List<WeatherData> weatherDataList = weatherResponse.getData();
        long dt = weatherDataList.get(0).getDt();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(dt * 1000L));

        WeatherData firstData = weatherDataList.get(0);
        Long sunrise = firstData.getSunrise();
        Long sunset = firstData.getSunset();
        Double temp = firstData.getTemp();
        Double feelsLike = firstData.getFeelsLike();
        Integer pressure = firstData.getPressure();
        Integer humidity = firstData.getHumidity();
        Double dewPoint = firstData.getDewPoint();
        Integer clouds = firstData.getClouds();
        Integer visibility = firstData.getVisibility();
        Double windSpeed = firstData.getWindSpeed();
        Integer windDeg = firstData.getWindDeg();

        String weatherDescription = null;
        if (firstData.getWeather() != null && !firstData.getWeather().isEmpty()) {
            weatherDescription = firstData.getWeather().get(0).getDescription();
        }

        return new WeatherInfo(pincode, weatherResponse.getLat(), weatherResponse.getLon(), date, sunrise, sunset, temp,
                feelsLike, pressure, humidity, dewPoint, clouds, visibility, windSpeed, windDeg, weatherDescription);
    }

}
