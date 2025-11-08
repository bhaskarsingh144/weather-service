package com.mxfz.weatherservice.model.weather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "weather_info")
public class WeatherInfo {

    @Id
    private String id = UUID.randomUUID().toString();

    private String pincode;
    private String date;

    private Double latitude;
    private Double longitude;

    private Long sunrise;
    private Long sunset;

    private Double temp;    // Temperature (Kelvin)
    private Double feelsLike; // Feels-like temperature (Kelvin)

    private Integer pressure;  // Atmospheric pressure (hPa)
    private Integer humidity;  // Relative humidity (%)
    private Double dewPoint;   // Dew point temperature (Kelvin)

    private Integer clouds;  // Cloud cover percentage (0-100)
    private Integer visibility; // Visibility in meters
    private Double windSpeed;  // Wind speed (m/s)
    private Integer windDeg;   // Wind direction in degrees (0-359), optional

    private String weatherDescription;  // Textual description of the weather conditions

    public WeatherInfo(String pincode, Double latitude, Double longitude, String date, Long sunrise, Long sunset,
                       Double temp, Double feelsLike, Integer pressure, Integer humidity, Double dewPoint,
                       Integer clouds, Integer visibility, Double windSpeed, Integer windDeg, String weatherDescription) {
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.pressure = pressure;
        this.humidity = humidity;
        this.dewPoint = dewPoint;
        this.clouds = clouds;
        this.visibility = visibility;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
        this.weatherDescription = weatherDescription;
    }
}
