package com.weather.forecast.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * A DTO for deserializing the comprehensive forecast response from Open-Meteo.
 * This class and its nested classes map directly to the JSON structure.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComprehensiveWeatherReport {

    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("longitude")
    private double longitude;
    @JsonProperty("timezone")
    private String timezone;
    @JsonProperty("current")
    private CurrentWeather current = new CurrentWeather(); // Initialize to prevent null
    @JsonProperty("hourly")
    private HourlyData hourly = new HourlyData();       // Initialize to prevent null
    @JsonProperty("daily")
    private DailyData daily = new DailyData();          // Initialize to prevent null

    //<editor-fold desc="Getters and Setters">
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public CurrentWeather getCurrent() { return current; }
    public void setCurrent(CurrentWeather current) { this.current = current; }
    public HourlyData getHourly() { return hourly; }
    public void setHourly(HourlyData hourly) { this.hourly = hourly; }
    public DailyData getDaily() { return daily; }
    public void setDaily(DailyData daily) { this.daily = daily; }
    //</editor-fold>

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {
        @JsonProperty("time") private String time = "";
        @JsonProperty("temperature_2m") private double temperature = 0.0;
        @JsonProperty("relative_humidity_2m") private int humidity = 0;
        @JsonProperty("apparent_temperature") private double apparentTemperature = 0.0;
        @JsonProperty("is_day") private int isDay = 0;
        @JsonProperty("weather_code") private int weatherCode = 0;
        @JsonProperty("surface_pressure") private double surfacePressure = 0.0;
        @JsonProperty("wind_speed_10m") private double windSpeed = 0.0;
        @JsonProperty("wind_direction_10m") private int windDirection = 0;

        //<editor-fold desc="Getters and Setters">
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
        public double getApparentTemperature() { return apparentTemperature; }
        public void setApparentTemperature(double apparentTemperature) { this.apparentTemperature = apparentTemperature; }
        public int getIsDay() { return isDay; }
        public void setIsDay(int isDay) { this.isDay = isDay; }
        public int getWeatherCode() { return weatherCode; }
        public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }
        public double getSurfacePressure() { return surfacePressure; }
        public void setSurfacePressure(double surfacePressure) { this.surfacePressure = surfacePressure; }
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        public int getWindDirection() { return windDirection; }
        public void setWindDirection(int windDirection) { this.windDirection = windDirection; }
        //</editor-fold>
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HourlyData {
        @JsonProperty("time") private List<String> time = new ArrayList<>();
        @JsonProperty("temperature_2m") private List<Double> temperature2m = new ArrayList<>(); // Added
        @JsonProperty("weather_code") private List<Integer> weatherCode = new ArrayList<>();    // Added
        @JsonProperty("precipitation_probability") private List<Integer> precipitationProbability = new ArrayList<>(); // Added
        @JsonProperty("wind_speed_10m") private List<Double> windSpeed10m = new ArrayList<>(); // Added
        @JsonProperty("uv_index") private List<Double> uvIndex = new ArrayList<>();
        @JsonProperty("visibility") private List<Double> visibility = new ArrayList<>();

        //<editor-fold desc="Getters and Setters">
        public List<String> getTime() { return time; }
        public void setTime(List<String> time) { this.time = time; }
        public List<Double> getTemperature2m() { return temperature2m; } // Added
        public void setTemperature2m(List<Double> temperature2m) { this.temperature2m = temperature2m; } // Added
        public List<Integer> getWeatherCode() { return weatherCode; } // Added
        public void setWeatherCode(List<Integer> weatherCode) { this.weatherCode = weatherCode; } // Added
        public List<Integer> getPrecipitationProbability() { return precipitationProbability; } // Added
        public void setPrecipitationProbability(List<Integer> precipitationProbability) { this.precipitationProbability = precipitationProbability; } // Added
        public List<Double> getWindSpeed10m() { return windSpeed10m; } // Added
        public void setWindSpeed10m(List<Double> windSpeed10m) { this.windSpeed10m = windSpeed10m; } // Added
        public List<Double> getUvIndex() { return uvIndex; }
        public void setUvIndex(List<Double> uvIndex) { this.uvIndex = uvIndex; }
        public List<Double> getVisibility() { return visibility; }
        public void setVisibility(List<Double> visibility) { this.visibility = visibility; }
        //</editor-fold>
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyData {
        @JsonProperty("time") private List<String> time = new ArrayList<>();
        @JsonProperty("weather_code") private List<Integer> weatherCode = new ArrayList<>();
        @JsonProperty("temperature_2m_max") private List<Double> temperatureMax = new ArrayList<>();
        @JsonProperty("temperature_2m_min") private List<Double> temperatureMin = new ArrayList<>();
        @JsonProperty("apparent_temperature_max") private List<Double> apparentTemperatureMax = new ArrayList<>();
        @JsonProperty("apparent_temperature_min") private List<Double> apparentTemperatureMin = new ArrayList<>();
        @JsonProperty("uv_index_max") private List<Double> uvIndexMax = new ArrayList<>();
        @JsonProperty("precipitation_probability_max") private List<Integer> precipitationProbabilityMax = new ArrayList<>();

        //<editor-fold desc="Getters and Setters">
        public List<String> getTime() { return time; }
        public void setTime(List<String> time) { this.time = time; }
        public List<Integer> getWeatherCode() { return weatherCode; }
        public void setWeatherCode(List<Integer> weatherCode) { this.weatherCode = weatherCode; }
        public List<Double> getTemperatureMax() { return temperatureMax; }
        public void setTemperatureMax(List<Double> temperatureMax) { this.temperatureMax = temperatureMax; }
        public List<Double> getTemperatureMin() { return temperatureMin; }
        public void setTemperatureMin(List<Double> temperatureMin) { this.temperatureMin = temperatureMin; }
        public List<Double> getApparentTemperatureMax() { return apparentTemperatureMax; }
        public void setApparentTemperatureMax(List<Double> apparentTemperatureMax) { this.apparentTemperatureMax = apparentTemperatureMax; }
        public List<Double> getApparentTemperatureMin() { return apparentTemperatureMin; }
        public void setApparentTemperatureMin(List<Double> apparentTemperatureMin) { this.apparentTemperatureMin = apparentTemperatureMin; }
        public List<Double> getUvIndexMax() { return uvIndexMax; }
        public void setUvIndexMax(List<Double> uvIndexMax) { this.uvIndexMax = uvIndexMax; }
        public List<Integer> getPrecipitationProbabilityMax() { return precipitationProbabilityMax; }
        public void setPrecipitationProbabilityMax(List<Integer> precipitationProbabilityMax) { this.precipitationProbabilityMax = precipitationProbabilityMax; }
        //</editor-fold>
    }
}
