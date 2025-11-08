package com.mxfz.weatherservice.repository;

import com.mxfz.weatherservice.model.PincodeLocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PincodeRepository extends MongoRepository<PincodeLocation, String> {

    PincodeLocation findByPincode(String pincode);
}