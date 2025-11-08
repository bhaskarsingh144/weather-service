package com.mxfz.weatherservice.repository;

import com.mxfz.weatherservice.model.weather.WeatherInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends MongoRepository<WeatherInfo, String> {

    WeatherInfo findByPincodeAndDate(String pincode, String date);
}