package com.weather.forecast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.api.OpenMeteoAPI;
import com.weather.forecast.model.WeatherLog;
import com.weather.forecast.repository.WeatherLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DataUpdateService {

    private final OpenMeteoAPI openMeteoAPI;
    private final WeatherLogRepository weatherLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataUpdateService(OpenMeteoAPI openMeteoAPI, WeatherLogRepository weatherLogRepository, ObjectMapper objectMapper) {
        this.openMeteoAPI = openMeteoAPI;
        this.weatherLogRepository = weatherLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches current weather for a city and saves it to the database.
     * @param city The city name.
     * @throws IOException if API calls or JSON parsing fails.
     * @throws InterruptedException if API calls are interrupted.
     */
    public void collectAndSaveCurrentWeather(String city) throws IOException, InterruptedException {
        // 1. Get coordinates
        String geoJson = openMeteoAPI.getCoordinatesForCity(city);
        JsonNode geoResults = objectMapper.readTree(geoJson).path("results");
        if (!geoResults.isArray() || geoResults.size() == 0) {
            throw new IOException("Could not find coordinates for city: " + city);
        }
        double lat = geoResults.get(0).path("latitude").asDouble();
        double lon = geoResults.get(0).path("longitude").asDouble();

        // 2. Get current weather
        // Using the comprehensive API call now
        String weatherJson = openMeteoAPI.getWeatherForecast(lat, lon);
        JsonNode currentWeather = objectMapper.readTree(weatherJson).path("current");

        // 3. Create WeatherLog entity
        WeatherLog log = new WeatherLog();
        log.setCity(city);
        log.setTemperature(currentWeather.path("temperature_2m").asDouble());
        log.setWindSpeed(currentWeather.path("wind_speed_10m").asDouble());
        
        // OpenMeteo API now provides humidity
        log.setHumidity(currentWeather.path("relative_humidity_2m").asInt(0)); 

        int weatherCode = currentWeather.path("weather_code").asInt();
        log.setConditions(mapWeatherCodeToCondition(weatherCode));

        LocalDateTime updateTime = LocalDateTime.parse(currentWeather.path("time").asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        log.setUpdateTime(updateTime);
        log.setSavedDate(LocalDateTime.now()); // The moment we save it

        // 4. Save to database
        weatherLogRepository.save(log);
    }
    
    /**
     * Maps WMO weather interpretation codes to a human-readable string.
     * @param code The WMO code from the API.
     * @return A descriptive string.
     */
    private String mapWeatherCodeToCondition(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: return "Fog";
            case 48: return "Depositing rime fog";
            case 51: return "Drizzle: Light";
            case 53: return "Drizzle: Moderate";
            case 55: return "Drizzle: Dense intensity";
            case 56: return "Freezing Drizzle: Light";
            case 57: return "Freezing Drizzle: Dense";
            case 61: return "Rain: Slight";
            case 63: return "Rain: Moderate";
            case 65: return "Rain: Heavy intensity";
            case 66: return "Freezing Rain: Light";
            case 67: return "Freezing Rain: Heavy";
            case 71: return "Snow fall: Slight";
            case 73: return "Snow fall: Moderate";
            case 75: return "Snow fall: Heavy intensity";
            case 77: return "Snow grains";
            case 80: return "Rain showers: Slight";
            case 81: return "Rain showers: Moderate";
            case 82: return "Rain showers: Violent";
            case 85: return "Snow showers: Slight";
            case 86: return "Snow showers: Heavy";
            case 95: return "Thunderstorm: Slight or moderate";
            case 96: return "Thunderstorm with slight hail";
            case 99: return "Thunderstorm with heavy hail";
            default: return "Unknown";
        }
    }
}
