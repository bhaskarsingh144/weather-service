package com.mxfz.weatherservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSONUtil {

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Converts a Java object to JSON string
     *
     * @param object the object to convert
     * @return JSON string representation of the object
     */
    public static String convertObjectToJSON(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting Object to JSON: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Converts a JSON string to a Java object of the specified class
     *
     * @param jsonString the JSON string to convert
     * @param clazz      the target class
     * @param <T>        the type of the object to return
     * @return the converted object, or null if conversion fails
     */
    public static <T> T convertJSONToObject(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to Object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converts a Java object to a pretty-printed JSON string
     *
     * @param object the object to convert
     * @return pretty-printed JSON string representation of the object
     */
    public static String convertObjectToPrettyJSON(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting Object to Pretty JSON: {}", e.getMessage());
            return "";
        }
    }
}

