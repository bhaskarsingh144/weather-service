package com.mxfz.weatherservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SerializationServiceImpl implements SerializationService {

    private final ObjectMapper objectMapper;
    private final ObjectMapper xmlMapper;

    public SerializationServiceImpl(ObjectMapper objectMapper, ObjectMapper xmlMapper) {
        this.objectMapper = objectMapper;
        this.xmlMapper = xmlMapper;
    }

    @Override
    public String serializeToJSON(Object javaObject) {
        try {
            return objectMapper.writeValueAsString(javaObject);
        } catch (JsonProcessingException e) {
            log.error("Error converting Object to JSON" + e.getMessage());
            return "";
        }
    }

    @Override
    public String serializeToXML(Object javaObject) {
        try {
            return xmlMapper.writeValueAsString(javaObject);
        } catch (JsonProcessingException e) {
            log.error("Error converting Object to XML" + e.getMessage());
            return "";
        }
    }

    @Override
    public <T> T deserializeFromJSON(String jsonString, Class<T> c) {
        try {
            return objectMapper.readValue(jsonString, c);
        } catch (JsonProcessingException e) {
            log.error("Error in deserializing JSON:" + e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T deserializeFromXML(String xmlString, Class<T> c) {
        try {
            return xmlMapper.readValue(xmlString, c);
        } catch (JsonProcessingException e) {
            log.error("Error in deserializing XML:" + e.getMessage());
            return null;
        }
    }

    @Override
    public String writeValueAsString(Object request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Error in deserializing Object:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}