package com.mxfz.weatherservice.model.geocode;

import lombok.Data;

@Data
public class Viewport {

    private Location northeast;
    private Location southwest;
}