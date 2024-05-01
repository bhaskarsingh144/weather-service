package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.PincodeLocation;
import com.mxfz.weatherservice.model.geocode.AddressComponent;
import com.mxfz.weatherservice.model.geocode.GeocodeResponse;
import com.mxfz.weatherservice.model.geocode.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
class GeoCodingServiceImpl implements GeoCodingService {

    private final PincodeService pincodeService;
    private final RestService restService;
    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    public GeoCodingServiceImpl(PincodeService pincodeService, RestService restService) {
        this.pincodeService = pincodeService;
        this.restService = restService;
    }

    @Override
    public PincodeLocation fetchLocationDetails(String pincode) {
        PincodeLocation savedPincodeInfo = pincodeService.findByPincode(pincode);
        if (savedPincodeInfo != null) {
            return savedPincodeInfo;
        }
        GeocodeResponse res = callGoogleGeoCodingAPI(pincode);
        PincodeLocation pincodeInfo = buildPincodeInfoFromGeoCodeResponse(res);
        return pincodeService.createPincode(pincodeInfo);
    }

    private GeocodeResponse callGoogleGeoCodingAPI(String pincode) {
        String uriString = "https://maps.googleapis.com/maps/api/geocode/json";
        URI uri = URI.create(uriString);
        byte[] bytes = Base64.getDecoder().decode(googleMapsApiKey);
        String key = new String(bytes, StandardCharsets.UTF_8);
        Map<String, String> params = Map.of("address", pincode, "key", key);
        return restService.callApi(uri, HttpMethod.GET, HttpHeaders.EMPTY, params, null,
                ParameterizedTypeReference.forType(GeocodeResponse.class));
    }

    private PincodeLocation buildPincodeInfoFromGeoCodeResponse(GeocodeResponse response) {

        Result firstResult = response.getResults().get(0);
        // Assuming the first address component with "postal_code" type contains the pincode
        String pincode = null;
        for (AddressComponent component : firstResult.getAddressComponents()) {
            if (component.getTypes().contains("postal_code")) {
                pincode = component.getLongName();
                break;
            }
        }
        if (pincode == null) {
            return null;
        }
        double latitude = firstResult.getGeometry().getLocation().getLat();
        double longitude = firstResult.getGeometry().getLocation().getLng();
        String address = firstResult.getFormattedAddress();
        return new PincodeLocation(pincode, latitude, longitude, address);
    }
}
