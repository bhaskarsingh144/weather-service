package com.mxfz.weatherservice.model.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Result {

    @JsonProperty("address_components")
    private List<AddressComponent> addressComponents;

    @JsonProperty("formatted_address")
    private String formattedAddress;

    @JsonProperty("geometry")
    private Geometry geometry;

    @JsonProperty("place_id")
    private String placeId;

    @JsonProperty("postcode_localities")
    private List<String> postcodeLocalities;

    @JsonProperty("types")
    private List<String> types;
}