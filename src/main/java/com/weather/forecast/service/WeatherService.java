package com.weather.forecast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.ai.ForecastModel;
import com.weather.forecast.api.OpenMeteoAPI;
import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.HourlyForecast; // Assuming this model will be used
import com.weather.forecast.model.dto.ComprehensiveWeatherReport; // New DTO
import com.weather.forecast.repository.WeatherRepository; // Assuming this is still used for something else

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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
     * Gets the 7-day weather forecast for a given province using XGBoost models.
     * This method fetches historical data, prepares features, and calls the prediction model.
     * It falls back to placeholder data if prediction fails.
     * @param province The name of the province.
     * @return A list of DailyForecast objects.
     */
    public List<DailyForecast> get7DayXGBoostForecast(String province) {
        System.out.println("Executing 7-day XGBoost forecast logic for " + province);
        try {
            // 1. Get coordinates for the province
            String geoJson = openMeteoAPI.getCoordinatesForCity(province);
            double[] coords = parseCoordinates(geoJson);
            if (coords == null) {
                System.err.println("Could not find coordinates for " + province);
                return Collections.emptyList(); // Return empty list on geocoding failure
            }
            // 2. Call the coordinate-based forecast method
            return get7DayXGBoostForecast(coords[0], coords[1]);
        } catch (Exception e) {
            System.err.println("Error getting 7-day XGBoost forecast for province '" + province + "': " + e.getMessage());
            e.printStackTrace();
            return getPlaceholderForecast(); // Fallback on geocoding/other errors
        }
    }

    /**
     * Gets the 7-day weather forecast for a given latitude and longitude using XGBoost models.
     * This is the core prediction logic.
     * @param lat The latitude.
     * @param lon The longitude.
     * @return A list of DailyForecast objects.
     */
    public List<DailyForecast> get7DayXGBoostForecast(double lat, double lon) {
        System.out.println("Executing 7-day XGBoost forecast for coordinates: Lat=" + lat + ", Lon=" + lon);
        final int PAST_DAYS_FOR_FEATURES = 3;

        try {
            // 1. Fetch historical data for features
            // This now uses the comprehensive API, extracting daily data
            String weatherJson = openMeteoAPI.getWeatherForecast(lat, lon);
            ComprehensiveWeatherReport comprehensiveReport = objectMapper.readValue(weatherJson, ComprehensiveWeatherReport.class);
            List<DailyForecast> historicalData = extractHistoricalDailyData(comprehensiveReport, PAST_DAYS_FOR_FEATURES);


            if (historicalData.size() < PAST_DAYS_FOR_FEATURES) {
                System.err.println("Not enough historical data to create features. Need " + PAST_DAYS_FOR_FEATURES + " days.");
                return getPlaceholderForecast(); // Fallback if insufficient historical data
            }

            // 2. Prepare features and predict for the next 7 days
            List<DailyForecast> forecastResults = new ArrayList<>();
            List<DailyForecast> currentFeatures = new ArrayList<>(historicalData);

            for (int i = 1; i <= 7; i++) {
                LocalDate predictionDate = LocalDate.now().plusDays(i);

                float[] features = new float[2 + 1 + (PAST_DAYS_FOR_FEATURES * 3)];
                features[0] = (float) lat;
                features[1] = (float) lon;
                features[2] = predictionDate.getDayOfYear();
                int featureIndex = 3;
                for(DailyForecast day : currentFeatures) {
                    features[featureIndex++] = (float) day.getTempMax();
                    features[featureIndex++] = (float) day.getTempMin();
                    features[featureIndex++] = (float) day.getRainProbability();
                }

                try {
                    // 3. Call each model separately
                    float predictedMaxTemp = dailyMaxTempForecastModel.predict(features);
                    float predictedMinTemp = dailyMinTempForecastModel.predict(features);
                    float predictedRainProb = dailyRainProbForecastModel.predict(features);

                    DailyForecast predictedDay = new DailyForecast(
                        predictionDate,
                        predictedMaxTemp,
                        predictedMinTemp,
                        predictedRainProb
                    );
                    forecastResults.add(predictedDay);

                    currentFeatures.remove(0);
                    currentFeatures.add(predictedDay);

                } catch (Exception modelException) {
                    System.err.println("MODEL PREDICTION FAILED for day " + i + ": " + modelException.getMessage());
                    System.err.println("Falling back to placeholder data for the rest of the forecast.");
                    return getPlaceholderForecast(); // Return placeholder if any prediction fails
                }
            }
            return forecastResults;

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

        // The API returns daily forecast from today onwards.
        // We need 'past' data for XGBoost features.
        // For simplicity, we'll use the *first N days* of the returned daily forecast
        // as our "historical" features for predicting the next 7 days.
        // In a real application, this historical data would come from a separate source (e.g., database, archive API).
        for (int i = 0; i < Math.min(numDays, report.getDaily().getTime().size()); i++) {
            LocalDate date = LocalDate.parse(report.getDaily().getTime().get(i));
            double max = report.getDaily().getTemperatureMax().get(i);
            double min = report.getDaily().getTemperatureMin().get(i);
            double rainProb = report.getDaily().getPrecipitationProbabilityMax().get(i) / 100.0; // Convert to probability [0,1]

            historicalData.add(new DailyForecast(date, max, min, rainProb));
        }
        return historicalData;
    }


    /**
     * Generates a list of placeholder forecasts.
     * @return A list of 7 DailyForecast objects with dummy data.
     */
    private List<DailyForecast> getPlaceholderForecast() {
        System.out.println("Note: AI model logic is not fully implemented or failed. Returning dummy forecast data.");
        return List.of(
            new DailyForecast(LocalDate.now().plusDays(1), 25.0, 15.0, 0.2),
            new DailyForecast(LocalDate.now().plusDays(2), 26.0, 16.0, 0.3),
            new DailyForecast(LocalDate.now().plusDays(3), 27.0, 17.0, 0.1),
            new DailyForecast(LocalDate.now().plusDays(4), 28.0, 18.0, 0.4),
            new DailyForecast(LocalDate.now().plusDays(5), 29.0, 19.0, 0.25),
            new DailyForecast(LocalDate.now().plusDays(6), 30.0, 20.0, 0.15),
            new DailyForecast(LocalDate.now().plusDays(7), 31.0, 21.0, 0.05)
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
}

