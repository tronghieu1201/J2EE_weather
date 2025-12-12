package com.weather.forecast.model;

import java.time.LocalTime;

/**
 * POJO for a single hourly forecast entry for a specific day.
 */
public class HourlyForecast {
    private LocalTime time;
    private double temperature;
    private int weatherCode;
    private int precipitationProbability;
    private double windSpeed;

    // No-arg constructor for Spring/Jackson
    public HourlyForecast() {
    }

    public HourlyForecast(LocalTime time, double temperature, int weatherCode, int precipitationProbability, double windSpeed) {
        this.time = time;
        this.temperature = temperature;
        this.weatherCode = weatherCode;
        this.precipitationProbability = precipitationProbability;
        this.windSpeed = windSpeed;
    }

    // Getters and Setters
    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    public int getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(int precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    @Override
    public String toString() {
        return "HourlyForecast{" +
                "time=" + time +
                ", temperature=" + temperature +
                ", weatherCode=" + weatherCode +
                ", precipitationProbability=" + precipitationProbability +
                ", windSpeed=" + windSpeed +
                '}';
    }
}