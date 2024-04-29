package com.mxfz.weatherservice.service;

import org.springframework.stereotype.Service;

@Service
public interface SerializationService {

    String serializeToJSON(Object javaObject);

    String serializeToXML(Object javaObject);

    <T> T deserializeFromJSON(String jsonString, Class<T> c);

    <T> T deserializeFromXML(String xmlString, Class<T> c);

    String writeValueAsString(Object request);
}
