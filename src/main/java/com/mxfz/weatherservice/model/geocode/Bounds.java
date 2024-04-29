package com.mxfz.weatherservice.model.geocode;

import lombok.Data;

@Data
public class Bounds {

    private Location northeast;
    private Location southwest;
}