package com.mxfz.weatherservice.service;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

public interface RestService {

    <T> T callApi(URI endpoint, HttpMethod method, HttpHeaders requestHeaders, Map<String, ?> requestParams,
                  Object requestBody, ParameterizedTypeReference<T> responseType);

}
