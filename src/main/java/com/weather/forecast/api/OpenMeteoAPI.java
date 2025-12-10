package com.weather.forecast.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

/**
 * Handles all interactions with the Open-Meteo API.
 * This is a Spring-managed service.
 */
@Service
public class OpenMeteoAPI {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String API_BASE_URL = "https://api.open-meteo.com/v1";
    private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search";

    public OpenMeteoAPI(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches the current weather for a given location.
     * @param lat latitude
     * @param lon longitude
     * @return JSON string of the current weather data.
     * @throws IOException if the API call fails.
     * @throws InterruptedException if the API call is interrupted.
     */
    /**
     * Fetches a comprehensive weather forecast (current, hourly, daily) in a single API call.
     * @param lat latitude
     * @param lon longitude
     * @return JSON string of the comprehensive weather data.
     * @throws IOException if the API call fails.
     * @throws InterruptedException if the API call is interrupted.
     */
    public String getWeatherForecast(double lat, double lon) throws IOException, InterruptedException {
        String url = API_BASE_URL + "/forecast?latitude=" + lat + "&longitude=" + lon +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m" +
                "&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,weather_code,visibility,uv_index,is_day" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,uv_index_max,precipitation_sum,precipitation_hours,precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max,wind_direction_10m_dominant" +
                "&timezone=auto";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch weather forecast from Open-Meteo API: " + response.body());
        }
        return response.body();
    }

    /**
     * Converts a city name to coordinates (latitude, longitude).
     * @param cityName The name of the city.
     * @return The JSON response from the geocoding API.
     */
     public String getCoordinatesForCity(String cityName) throws IOException, InterruptedException {
        // URL encode city name to handle spaces and special characters
        String encodedCityName = java.net.URLEncoder.encode(cityName, java.nio.charset.StandardCharsets.UTF_8);
        String url = GEOCODING_API_URL + "?name=" + encodedCityName + "&count=1&language=en&format=json";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

         if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch geocoding data: " + response.body());
        }
        return response.body();
     }
}
