package com.mxfz.weatherservice.model.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Geometry {

    @JsonProperty("bounds")
    private Bounds bounds;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("viewport")
    private Viewport viewport;
}