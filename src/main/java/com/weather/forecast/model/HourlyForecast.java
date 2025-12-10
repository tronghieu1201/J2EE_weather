package com.weather.forecast.model;

import java.time.LocalTime;

/**
 * POJO for the detailed 3-hourly forecast for a specific day.
 */
public class HourlyForecast {
    private LocalTime time;
    private double temperature;
    private double rain;
    // Add other relevant fields like humidity, wind speed etc.

    public HourlyForecast(LocalTime time, double temperature, double rain) {
        this.time = time;
        this.temperature = temperature;
        this.rain = rain;
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

    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

    @Override
    public String toString() {
        return "HourlyForecast{" +
                "time=" + time +
                ", temperature=" + temperature +
                ", rain=" + rain +
                '}';
    }
}
