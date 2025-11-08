package com.mxfz.weatherservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pincode_locations")
public class PincodeLocation {

    @Id
    private String id = UUID.randomUUID().toString();
    private String pincode;
    private double latitude;
    private double longitude;
    private String address;

    public PincodeLocation(String pincode, double latitude, double longitude, String address) {
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
}
