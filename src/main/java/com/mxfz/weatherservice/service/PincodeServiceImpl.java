package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.PincodeLocation;
import com.mxfz.weatherservice.repository.PincodeRepository;
import org.springframework.stereotype.Service;

@Service
public class PincodeServiceImpl implements PincodeService {

    private final PincodeRepository pincodeRepository;

    public PincodeServiceImpl(PincodeRepository pincodeRepository) {
        this.pincodeRepository = pincodeRepository;
    }


    @Override
    public PincodeLocation findByPincode(String pincode) {
        return pincodeRepository.findByPincode(pincode);
    }

    @Override
    public PincodeLocation createPincode(PincodeLocation pincodeLocation) {
        return pincodeRepository.save(pincodeLocation);
    }

    @Override
    public PincodeLocation updatePincode(PincodeLocation pincodeLocation) {
        PincodeLocation existingPincode = pincodeRepository.findById(pincodeLocation.getId()).orElse(null);
        if (existingPincode == null) {
            throw new IllegalArgumentException("PincodeLocation not found: " + pincodeLocation.getId());
        }
        return pincodeRepository.save(pincodeLocation);
    }

    @Override
    public void deletePincode(String pincodeLocation) {
        PincodeLocation existingPincode = pincodeRepository.findByPincode(pincodeLocation);
        if (existingPincode != null) {
            pincodeRepository.delete(existingPincode);
        }
    }
}
