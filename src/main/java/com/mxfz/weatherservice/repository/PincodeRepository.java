package com.mxfz.weatherservice.repository;

import com.mxfz.weatherservice.model.PincodeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PincodeRepository extends JpaRepository<PincodeLocation, String> {

    PincodeLocation findByPincode(String pincode);
}