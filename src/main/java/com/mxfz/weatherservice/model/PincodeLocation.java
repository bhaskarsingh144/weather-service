package com.mxfz.weatherservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pincodes")
@Entity
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
