package com.mxfz.weatherservice.repository;

import com.mxfz.weatherservice.model.weather.WeatherInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<WeatherInfo, String> {

    WeatherInfo findByPincodeAndDate(String pincode, String date);
}