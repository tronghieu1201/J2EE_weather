package com.weather.forecast.model;

import java.time.LocalDate;

/**
 * POJO for the 7-day forecast summary.
 */
public class DailyForecast {
    private LocalDate date;
    private double tempMax;
    private double tempMin;
    private double rainProbability;
    private int weatherCode;

    public DailyForecast(LocalDate date, double tempMax, double tempMin, double rainProbability, int weatherCode) {
        this.date = date;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.rainProbability = rainProbability;
        this.weatherCode = weatherCode;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public double getRainProbability() {
        return rainProbability;
    }

    public void setRainProbability(double rainProbability) {
        this.rainProbability = rainProbability;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    @Override
    public String toString() {
        return "DailyForecast{" +
                "date=" + date +
                ", tempMax=" + tempMax +
                ", tempMin=" + tempMin +
                ", rainProbability=" + rainProbability +
                ", weatherCode=" + weatherCode +
                '}';
    }
}
