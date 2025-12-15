package com.weather.forecast.model.dto;

public class ProvinceCurrentWeather {
    private String provinceName;
    private double temperature;
    private int weatherCode;

    public ProvinceCurrentWeather(String provinceName, double temperature, int weatherCode) {
        this.provinceName = provinceName;
        this.temperature = temperature;
        this.weatherCode = weatherCode;
    }

    // Getters and Setters
    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
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
}
