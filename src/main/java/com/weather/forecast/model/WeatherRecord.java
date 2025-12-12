package com.weather.forecast.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JPA Entity representing a single weather data record in the 'weather_history' table.
 */
@Entity
@Table(name = "weather_history")
public class WeatherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String province;
    private double lat;
    private double lon;

    @Column(name = "\"date\"") // Use quoted identifier for reserved keyword
    private LocalDate date;

    @Column(name = "\"time\"") // Use quoted identifier for reserved keyword
    private LocalTime time;

    private double temperature;
    private double humidity;

    @Column(name = "wind_speed")
    private double windSpeed;

    private double precipitation;
    private double pressure;

    @Column(name = "cloud_cover")
    private double cloudCover;

    /**
     * Default constructor for JPA.
     */
    public WeatherRecord() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

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

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(double cloudCover) {
        this.cloudCover = cloudCover;
    }
}
