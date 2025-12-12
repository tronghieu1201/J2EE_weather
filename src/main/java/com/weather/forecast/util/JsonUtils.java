package com.weather.forecast.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

/**
 * Utility class for JSON operations.
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Configure to ignore unknown properties in the JSON input
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Converts a JSON string to a Java object.
     *
     * @param jsonString The JSON string.
     * @param clazz      The class of the target object.
     * @param <T>        The type of the target object.
     * @return An object of type T.
     * @throws IOException if parsing fails.
     */
    public static <T> T fromJson(String jsonString, Class<T> clazz) throws IOException {
        return objectMapper.readValue(jsonString, clazz);
    }

    /**
     * Converts a Java object to its JSON string representation.
     *
     * @param object The object to convert.
     * @return JSON string.
     * @throws IOException if conversion fails.
     */
    public static String toJson(Object object) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
