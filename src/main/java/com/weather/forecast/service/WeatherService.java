package com.weather.forecast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.ai.ForecastModel;
import com.weather.forecast.api.OpenMeteoAPI;
import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.HourlyForecast; // Assuming this model will be used
import com.weather.forecast.model.dto.ComprehensiveWeatherReport; // New DTO
import com.weather.forecast.model.dto.ProvinceCurrentWeather;
import com.weather.forecast.repository.WeatherRepository; // Assuming this is still used for something else

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Core business logic for the weather forecast application.
 * This service now supports both AI-based 7-day predictions and fetching comprehensive live data.
 */
@Service
public class WeatherService {

    private final OpenMeteoAPI openMeteoAPI;
    private final WeatherRepository weatherRepository; // Potentially still needed or removed
    private final ForecastModel dailyMaxTempForecastModel;
    private final ForecastModel dailyMinTempForecastModel;
    private final ForecastModel dailyRainProbForecastModel;
    private final ForecastModel hourlyForecastModel; // This will remain as a single model for hourly

    private final ObjectMapper objectMapper;

    @Autowired
    public WeatherService(OpenMeteoAPI openMeteoAPI,
                          WeatherRepository weatherRepository,
                          @Qualifier("dailyMaxTempForecastModel") ForecastModel dailyMaxTempForecastModel,
                          @Qualifier("dailyMinTempForecastModel") ForecastModel dailyMinTempForecastModel,
                          @Qualifier("dailyRainProbForecastModel") ForecastModel dailyRainProbForecastModel,
                          @Qualifier("hourlyForecastModel") ForecastModel hourlyForecastModel,
                          ObjectMapper objectMapper) { // Inject ObjectMapper
        this.openMeteoAPI = openMeteoAPI;
        this.weatherRepository = weatherRepository;
        this.dailyMaxTempForecastModel = dailyMaxTempForecastModel;
        this.dailyMinTempForecastModel = dailyMinTempForecastModel;
        this.dailyRainProbForecastModel = dailyRainProbForecastModel;
        this.hourlyForecastModel = hourlyForecastModel;
        this.objectMapper = objectMapper;
    }

    /**
     * Gets a comprehensive weather report for a given city (live data from Open-Meteo API).
     * This is used for current weather and potentially for features for the AI model.
     * @param city The name of the city.
     * @return A ComprehensiveWeatherReport object.
     */
    public ComprehensiveWeatherReport getWeatherReport(String city) {
        try {
            // 1. Get Coordinates
            String geoJson = openMeteoAPI.getCoordinatesForCity(city);
            JsonNode rootNode = objectMapper.readTree(geoJson);
            JsonNode resultsNode = rootNode.path("results");

            if (!resultsNode.isArray() || resultsNode.size() == 0) {
                System.err.println("Could not find coordinates for city: " + city);
                return new ComprehensiveWeatherReport(); // Return empty but non-null report
            }

            JsonNode firstResult = resultsNode.get(0);
            double lat = firstResult.path("latitude").asDouble();
            double lon = firstResult.path("longitude").asDouble();

            // 2. Get Comprehensive Weather Data
            String weatherJson = openMeteoAPI.getWeatherForecast(lat, lon);

            // 3. Deserialize into DTO
            return objectMapper.readValue(weatherJson, ComprehensiveWeatherReport.class);

        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to get weather report for " + city + ": " + e.getMessage());
            e.printStackTrace();
            return new ComprehensiveWeatherReport(); // Return empty but non-null report on failure
        }
    }


    /**
     * Extracts the 7-day weather forecast directly from the comprehensive API report.
     * This method no longer uses XGBoost models.
     * @param comprehensiveReport The full weather report containing the daily forecast data.
     * @return A list of DailyForecast objects.
     */
    public List<DailyForecast> get7DayForecastFromReport(ComprehensiveWeatherReport comprehensiveReport) {
        // If the report is null or doesn't have the necessary data, return an empty list.
        if (comprehensiveReport == null || comprehensiveReport.getDaily() == null || comprehensiveReport.getDaily().getTime().isEmpty()) {
            System.err.println("Comprehensive report is missing daily data. Cannot extract 7-day forecast.");
            return Collections.emptyList();
        }

        System.out.println("Extracting 7-day forecast directly from API report.");
        List<DailyForecast> forecastResults = new ArrayList<>();
        
        try {
            ComprehensiveWeatherReport.DailyData dailyData = comprehensiveReport.getDaily();
            List<String> dates = dailyData.getTime();
            List<Double> maxTemps = dailyData.getTemperatureMax();
            List<Double> minTemps = dailyData.getTemperatureMin();
            List<Integer> rainProbs = dailyData.getPrecipitationProbabilityMax();
            List<Integer> weatherCodes = dailyData.getWeatherCode();

            // Ensure all lists have the same size to avoid IndexOutOfBoundsException
            int forecastDays = dates.size();
            if (maxTemps.size() != forecastDays || minTemps.size() != forecastDays || rainProbs.size() != forecastDays || weatherCodes.size() != forecastDays) {
                System.err.println("Inconsistent daily data sizes in API report. Cannot proceed.");
                return Collections.emptyList();
            }

            for (int i = 0; i < forecastDays; i++) {
                LocalDate date = LocalDate.parse(dates.get(i));
                double maxTemp = maxTemps.get(i);
                double minTemp = minTemps.get(i);
                // Convert precipitation probability from percentage to a 0-1 float value
                double rainProb = rainProbs.get(i) / 100.0;
                int weatherCode = weatherCodes.get(i);

                DailyForecast day = new DailyForecast(date, maxTemp, minTemp, rainProb, weatherCode);
                forecastResults.add(day);
            }
            return forecastResults;

        } catch (Exception e) {
            System.err.println("Error extracting 7-day forecast from report: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return empty list on any parsing or processing error
        }
    }


    /**
     * This method is now effectively deprecated and kept for reference or internal use if needed.
     * The main logic is in get7DayXGBoostForecast(ComprehensiveWeatherReport).
     * @param lat The latitude.
     * @param lon The longitude.
     * @return A list of DailyForecast objects.
     */
    private List<DailyForecast> get7DayXGBoostForecast(double lat, double lon) {
        System.out.println("Executing coordinate-based 7-day forecast. Consider using the report-based method for efficiency.");
        try {
            String weatherJson = openMeteoAPI.getWeatherForecast(lat, lon);
            ComprehensiveWeatherReport comprehensiveReport = objectMapper.readValue(weatherJson, ComprehensiveWeatherReport.class);
            return get7DayForecastFromReport(comprehensiveReport);
        } catch (Exception e) {
            System.err.println("Error getting 7-day XGBoost forecast by coords: " + e.getMessage());
            e.printStackTrace();
            return getPlaceholderForecast(); // Fallback on API/other errors
        }
    }


    /**
     * Parses the latitude and longitude from the geocoding API response.
     * @param jsonResponse The JSON string from the geocoding API.
     * @return A double array containing [latitude, longitude], or null if not found.
     * @throws IOException if JSON parsing fails.
     */
    private double[] parseCoordinates(String jsonResponse) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode resultsNode = rootNode.path("results");
        if (resultsNode.isArray() && resultsNode.size() > 0) {
            JsonNode firstResult = resultsNode.get(0);
            double lat = firstResult.path("latitude").asDouble();
            double lon = firstResult.path("longitude").asDouble();
            return new double[]{lat, lon};
        }
        return null;
    }

    /**
     * Extracts historical daily data from the ComprehensiveWeatherReport DTO for feature engineering.
     * This simulates fetching "historical" data from the daily part of the forecast API.
     * For a true historical model, you would query an archive API or a database of past weather.
     * @param report ComprehensiveWeatherReport DTO.
     * @param numDays The number of historical days to extract.
     * @return List of DailyForecast objects.
     */
    private List<DailyForecast> extractHistoricalDailyData(ComprehensiveWeatherReport report, int numDays) {
        List<DailyForecast> historicalData = new ArrayList<>();
        if (report.getDaily() == null || report.getDaily().getTime().isEmpty()) {
            return historicalData;
        }

        ComprehensiveWeatherReport.DailyData dailyData = report.getDaily();
        List<String> dates = dailyData.getTime();
        List<Double> maxTemps = dailyData.getTemperatureMax();
        List<Double> minTemps = dailyData.getTemperatureMin();
        List<Integer> rainProbs = dailyData.getPrecipitationProbabilityMax();
        List<Integer> weatherCodes = dailyData.getWeatherCode();

        // Ensure all lists have the same size to avoid IndexOutOfBoundsException
        int dataSize = dates.size();
        if (maxTemps.size() != dataSize || minTemps.size() != dataSize || rainProbs.size() != dataSize || weatherCodes.size() != dataSize) {
            System.err.println("Inconsistent daily data sizes in API report for historical data. Cannot proceed.");
            return Collections.emptyList();
        }

        // The API returns daily forecast from today onwards.
        // We need 'past' data for XGBoost features.
        // For simplicity, we'll use the *first N days* of the returned daily forecast
        // as our "historical" features for predicting the next 7 days.
        // In a real application, this historical data would come from a separate source (e.g., database, archive API).
        for (int i = 0; i < Math.min(numDays, dataSize); i++) {
            LocalDate date = LocalDate.parse(dates.get(i));
            double max = maxTemps.get(i);
            double min = minTemps.get(i);
            double rainProb = rainProbs.get(i) / 100.0; // Convert to probability [0,1]
            int weatherCode = weatherCodes.get(i); // Get the weather code

            historicalData.add(new DailyForecast(date, max, min, rainProb, weatherCode));
        }
        return historicalData;
    }


    /**
     * Generates a list of placeholder forecasts.
     * @return A list of 7 DailyForecast objects with dummy data.
     */
    private List<DailyForecast> getPlaceholderForecast() {
        System.out.println("Note: AI model logic is not fully implemented or failed. Returning dummy forecast data.");
        // Provide a default weather code (e.g., 0 for clear sky) for placeholder data
        return List.of(
            new DailyForecast(LocalDate.now().plusDays(1), 25.0, 15.0, 0.2, 0),
            new DailyForecast(LocalDate.now().plusDays(2), 26.0, 16.0, 0.3, 1),
            new DailyForecast(LocalDate.now().plusDays(3), 27.0, 17.0, 0.1, 2),
            new DailyForecast(LocalDate.now().plusDays(4), 28.0, 18.0, 0.4, 3),
            new DailyForecast(LocalDate.now().plusDays(5), 29.0, 19.0, 0.25, 45),
            new DailyForecast(LocalDate.now().plusDays(6), 30.0, 20.0, 0.15, 51),
            new DailyForecast(LocalDate.now().plusDays(7), 31.0, 21.0, 0.05, 61)
        );
    }


    /**
     * Gets the detailed 3-hourly forecast for a specific day. (Placeholder for future development)
     * @param province The province name.
     * @param date The specific date.
     * @return A list of HourlyForecast objects.
     */
    public List<HourlyForecast> getHourlyForecast(String province, LocalDate date) {
        System.out.println("Executing hourly forecast logic for " + province + " on " + date);
        return Collections.emptyList(); // Placeholder
    }

    /**
     * Fetches current weather data for a list of prominent provinces.
     *
     * @param prominentProvinces A list of province names for which to fetch current weather.
     * @return A list of ProvinceCurrentWeather DTOs containing the current temperature and weather code for each province.
     */
    public List<ProvinceCurrentWeather> getCurrentWeatherForProminentProvinces(List<String> prominentProvinces) {
        List<ProvinceCurrentWeather> provinceWeatherList = new ArrayList<>();
        for (String province : prominentProvinces) {
            try {
                ComprehensiveWeatherReport report = getWeatherReport(province);
                // Assuming current weather is available and contains temperature and weather code
                Optional.ofNullable(report.getCurrent())
                        .ifPresent(currentWeather -> provinceWeatherList.add(new ProvinceCurrentWeather(
                                province,
                                currentWeather.getTemperature(),
                                currentWeather.getWeatherCode()
                        )));
            } catch (Exception e) {
                System.err.println("Failed to fetch current weather for prominent province " + province + ": " + e.getMessage());
                // Optionally add a placeholder or skip this province
            }
        }
        return provinceWeatherList;
    }
}

