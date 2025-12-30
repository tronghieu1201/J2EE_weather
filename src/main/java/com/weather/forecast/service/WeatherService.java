package com.weather.forecast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.ai.ForecastModel;
import com.weather.forecast.api.OpenMeteoAPI;
import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.HourlyForecast;
import com.weather.forecast.model.WeatherHistory;
import com.weather.forecast.model.dto.ComprehensiveWeatherReport;
import com.weather.forecast.model.dto.ProvinceCurrentWeather;
import com.weather.forecast.repository.WeatherHistoryRepository;

import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Core business logic for the weather forecast application.
 * Hỗ trợ cả dự đoán XGBoost và lấy dữ liệu trực tiếp từ API.
 */
@Service
public class WeatherService {

    private final OpenMeteoAPI openMeteoAPI;
    private final WeatherHistoryRepository weatherHistoryRepository;
    private final WeatherLogService weatherLogService;
    private final PredictionResultService predictionResultService;
    private final ForecastModel dailyMaxTempForecastModel;
    private final ForecastModel dailyMinTempForecastModel;
    private final ForecastModel dailyRainProbForecastModel;
    private final ForecastModel hourlyForecastModel;
    private final ObjectMapper objectMapper;

    // Số ngày lịch sử dùng làm features cho XGBoost
    private static final int PAST_DAYS_FOR_FEATURES = 3;

    // Flag để bật/tắt XGBoost (có thể set từ config)
    private boolean useXGBoost = true;

    @Autowired
    public WeatherService(OpenMeteoAPI openMeteoAPI,
            WeatherHistoryRepository weatherHistoryRepository,
            WeatherLogService weatherLogService,
            PredictionResultService predictionResultService,
            @Qualifier("dailyMaxTempForecastModel") ForecastModel dailyMaxTempForecastModel,
            @Qualifier("dailyMinTempForecastModel") ForecastModel dailyMinTempForecastModel,
            @Qualifier("dailyRainProbForecastModel") ForecastModel dailyRainProbForecastModel,
            @Qualifier("hourlyForecastModel") ForecastModel hourlyForecastModel,
            ObjectMapper objectMapper) {
        this.openMeteoAPI = openMeteoAPI;
        this.weatherHistoryRepository = weatherHistoryRepository;
        this.weatherLogService = weatherLogService;
        this.predictionResultService = predictionResultService;
        this.dailyMaxTempForecastModel = dailyMaxTempForecastModel;
        this.dailyMinTempForecastModel = dailyMinTempForecastModel;
        this.dailyRainProbForecastModel = dailyRainProbForecastModel;
        this.hourlyForecastModel = hourlyForecastModel;
        this.objectMapper = objectMapper;
    }

    /**
     * Lấy báo cáo thời tiết toàn diện từ API (current, hourly, daily).
     * Kết quả được cache trong 5 phút để tối ưu performance.
     */
    @Cacheable(value = "weatherReports", key = "#city")
    public ComprehensiveWeatherReport getWeatherReport(String city) {
        try {
            String geoJson = openMeteoAPI.getCoordinatesForCity(city);
            JsonNode rootNode = objectMapper.readTree(geoJson);
            JsonNode resultsNode = rootNode.path("results");

            if (!resultsNode.isArray() || resultsNode.size() == 0) {
                System.err.println("Could not find coordinates for city: " + city);
                return new ComprehensiveWeatherReport();
            }

            JsonNode firstResult = resultsNode.get(0);
            double lat = firstResult.path("latitude").asDouble();
            double lon = firstResult.path("longitude").asDouble();

            String weatherJson = openMeteoAPI.getWeatherForecast(lat, lon);
            ComprehensiveWeatherReport report = objectMapper.readValue(weatherJson, ComprehensiveWeatherReport.class);

            // Log weather data vào database (async)
            weatherLogService.logWeatherData(city, report);

            return report;

        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to get weather report for " + city + ": " + e.getMessage());
            return new ComprehensiveWeatherReport();
        }
    }

    /**
     * Dự báo 7 ngày sử dụng XGBoost nếu có đủ dữ liệu lịch sử.
     * Nếu không, fallback về API trực tiếp.
     */
    public List<DailyForecast> get7DayForecast(String city) {
        if (useXGBoost) {
            try {
                List<DailyForecast> xgboostForecast = get7DayForecastWithXGBoost(city);
                if (!xgboostForecast.isEmpty()) {
                    System.out.println("✓ Using XGBoost prediction for " + city);

                    // Lưu kết quả dự đoán vào database (async)
                    predictionResultService.savePredictions(city, xgboostForecast);

                    return xgboostForecast;
                }
            } catch (Exception e) {
                System.err.println("XGBoost prediction failed for " + city + ": " + e.getMessage());
            }
        }

        // Fallback: Lấy từ API trực tiếp
        System.out.println("→ Fallback to API for " + city);
        ComprehensiveWeatherReport report = getWeatherReport(city);
        return get7DayForecastFromReport(report);
    }

    /**
     * Dự báo 7 ngày sử dụng XGBoost models.
     */
    private List<DailyForecast> get7DayForecastWithXGBoost(String city) throws XGBoostError {
        // 1. Lấy dữ liệu lịch sử từ database
        List<WeatherHistory> historyList = weatherHistoryRepository.findTop7ByProvinceOrderByRecordDateDesc(city);

        if (historyList.size() < PAST_DAYS_FOR_FEATURES) {
            System.out.println("Not enough historical data for " + city + " (need " + PAST_DAYS_FOR_FEATURES + ", got "
                    + historyList.size() + ")");
            return Collections.emptyList();
        }

        // 2. Lấy tọa độ
        double lat = historyList.get(0).getLatitude();
        double lon = historyList.get(0).getLongitude();

        // 3. Tạo predictions cho 7 ngày tiếp theo
        List<DailyForecast> predictions = new ArrayList<>();

        // Dự báo hôm nay + 7 ngày tiếp theo = 8 ngày
        for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
            LocalDate predictionDate = LocalDate.now().plusDays(dayOffset);

            // Tạo feature vector
            float[] features = createFeatureVector(lat, lon, predictionDate, historyList);

            // Dự đoán bằng XGBoost
            float predictedMaxTemp = dailyMaxTempForecastModel.predict(features);
            float predictedMinTemp = dailyMinTempForecastModel.predict(features);
            float predictedRainProb = dailyRainProbForecastModel.predict(features);

            // Clamp rain probability to [0, 1]
            predictedRainProb = Math.max(0, Math.min(1, predictedRainProb));

            // Map rain probability to weather code
            int weatherCode = mapRainProbToWeatherCode(predictedRainProb);

            predictions.add(new DailyForecast(
                    predictionDate,
                    predictedMaxTemp,
                    predictedMinTemp,
                    predictedRainProb,
                    weatherCode));
        }

        return predictions;
    }

    /**
     * Tạo feature vector cho XGBoost prediction.
     * Features: [lat, lon, day_of_year, past_day1_max, past_day1_min,
     * past_day1_rain, ...]
     */
    private float[] createFeatureVector(double lat, double lon, LocalDate predictionDate,
            List<WeatherHistory> historyList) {
        // Total features: 3 (lat, lon, day_of_year) + 3 days * 3 values = 12
        float[] features = new float[3 + PAST_DAYS_FOR_FEATURES * 3];

        int idx = 0;
        features[idx++] = (float) lat;
        features[idx++] = (float) lon;
        features[idx++] = predictionDate.getDayOfYear();

        // Add historical features (3 days)
        for (int i = 0; i < PAST_DAYS_FOR_FEATURES && i < historyList.size(); i++) {
            WeatherHistory history = historyList.get(i);
            features[idx++] = history.getTempMax() != null ? history.getTempMax().floatValue() : 25.0f;
            features[idx++] = history.getTempMin() != null ? history.getTempMin().floatValue() : 20.0f;
            features[idx++] = history.getPrecipitationProbability() != null
                    ? history.getPrecipitationProbability().floatValue()
                    : 0.0f;
        }

        return features;
    }

    /**
     * Map xác suất mưa sang weather code.
     */
    private int mapRainProbToWeatherCode(float rainProb) {
        if (rainProb < 0.1)
            return 0; // Clear sky
        if (rainProb < 0.2)
            return 1; // Mainly clear
        if (rainProb < 0.3)
            return 2; // Partly cloudy
        if (rainProb < 0.4)
            return 3; // Overcast
        if (rainProb < 0.5)
            return 61; // Rain: Slight
        if (rainProb < 0.7)
            return 63; // Rain: Moderate
        return 65; // Rain: Heavy
    }

    /**
     * Trích xuất dự báo 7 ngày trực tiếp từ API response.
     */
    public List<DailyForecast> get7DayForecastFromReport(ComprehensiveWeatherReport comprehensiveReport) {
        if (comprehensiveReport == null || comprehensiveReport.getDaily() == null ||
                comprehensiveReport.getDaily().getTime().isEmpty()) {
            System.err.println("Comprehensive report is missing daily data.");
            return Collections.emptyList();
        }

        List<DailyForecast> forecastResults = new ArrayList<>();

        try {
            ComprehensiveWeatherReport.DailyData dailyData = comprehensiveReport.getDaily();
            List<String> dates = dailyData.getTime();
            List<Double> maxTemps = dailyData.getTemperatureMax();
            List<Double> minTemps = dailyData.getTemperatureMin();
            List<Integer> rainProbs = dailyData.getPrecipitationProbabilityMax();
            List<Integer> weatherCodes = dailyData.getWeatherCode();

            int forecastDays = dates.size();
            if (maxTemps.size() != forecastDays || minTemps.size() != forecastDays ||
                    rainProbs.size() != forecastDays || weatherCodes.size() != forecastDays) {
                System.err.println("Inconsistent daily data sizes in API report.");
                return Collections.emptyList();
            }

            for (int i = 0; i < forecastDays; i++) {
                LocalDate date = LocalDate.parse(dates.get(i));
                double maxTemp = maxTemps.get(i);
                double minTemp = minTemps.get(i);
                double rainProb = rainProbs.get(i) / 100.0;
                int weatherCode = weatherCodes.get(i);

                forecastResults.add(new DailyForecast(date, maxTemp, minTemp, rainProb, weatherCode));
            }
            return forecastResults;

        } catch (Exception e) {
            System.err.println("Error extracting 7-day forecast from report: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Lấy thời tiết hiện tại cho các tỉnh nổi bật.
     * Kết quả được cache 5 phút để tránh gọi API lại mỗi lần vào trang chủ.
     */
    @Cacheable(value = "prominentProvincesWeather", key = "'all'")
    public List<ProvinceCurrentWeather> getCurrentWeatherForProminentProvinces(List<String> prominentProvinces) {
        List<ProvinceCurrentWeather> provinceWeatherList = new ArrayList<>();
        for (String province : prominentProvinces) {
            try {
                ComprehensiveWeatherReport report = getWeatherReport(province);
                Optional.ofNullable(report.getCurrent())
                        .ifPresent(currentWeather -> provinceWeatherList.add(new ProvinceCurrentWeather(
                                province,
                                currentWeather.getTemperature(),
                                currentWeather.getWeatherCode())));
            } catch (Exception e) {
                System.err.println("Failed to fetch current weather for " + province + ": " + e.getMessage());
            }
        }
        return provinceWeatherList;
    }

    /**
     * Lấy dự báo theo giờ cho một ngày cụ thể.
     */
    public List<HourlyForecast> getHourlyForecast(String province, LocalDate date) {
        System.out.println("Getting hourly forecast for " + province + " on " + date);
        return Collections.emptyList();
    }

    /**
     * Bật/tắt sử dụng XGBoost.
     */
    public void setUseXGBoost(boolean useXGBoost) {
        this.useXGBoost = useXGBoost;
    }
}
