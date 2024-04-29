package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.PincodeLocation;

public interface GeoCodingService {
    PincodeLocation fetchLocationDetails(String pincode);
}
