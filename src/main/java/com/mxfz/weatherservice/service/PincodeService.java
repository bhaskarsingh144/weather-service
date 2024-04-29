package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.PincodeLocation;

public interface PincodeService {

    PincodeLocation findByPincode(String pincode);

    PincodeLocation createPincode(PincodeLocation pincodeLocation);

    PincodeLocation updatePincode(PincodeLocation pincodeLocation);

    void deletePincode(String pincodeLocation);
}
