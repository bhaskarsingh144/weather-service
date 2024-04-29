package com.mxfz.weatherservice.model.weather;

import lombok.Data;

@Data
public class Weather {

    private int id;
    private String main;
    private String description;
    private String icon;
}