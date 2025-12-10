package com.weather.forecast.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_logs")
public class WeatherLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private Double temperature;
    private Integer humidity; // Added humidity
    private Double windSpeed;
    private String conditions; // e.g., "Clear", "Cloudy", "Rainy"
    private LocalDateTime updateTime; // Time from the API response
    private LocalDateTime savedDate; // When it was saved to our DB

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(LocalDateTime savedDate) {
        this.savedDate = savedDate;
    }

    @Override
    public String toString() {
        return "WeatherLog{" +
                "id=" + id +
                ", city='" + city + "'" +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", conditions='" + conditions + "'" +
                ", updateTime=" + updateTime +
                ", savedDate=" + savedDate +
                '}';
    }
}
