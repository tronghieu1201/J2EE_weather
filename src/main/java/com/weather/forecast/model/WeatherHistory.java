package com.weather.forecast.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entity cho bảng weather_history - lưu trữ dữ liệu thời tiết lịch sử để huấn
 * luyện XGBoost.
 * Cấu trúc này phù hợp với việc thu thập dữ liệu daily để dự đoán.
 */
@Entity
@Table(name = "weather_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "province", "record_date" })
})
public class WeatherHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "record_time")
    private LocalTime recordTime;

    // === Features cho XGBoost ===

    @Column(name = "temp_max")
    private Double tempMax;

    @Column(name = "temp_min")
    private Double tempMin;

    @Column(name = "temp_current")
    private Double tempCurrent;

    private Double humidity;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "precipitation")
    private Double precipitation;

    @Column(name = "precipitation_probability")
    private Double precipitationProbability;

    private Double pressure;

    @Column(name = "cloud_cover")
    private Double cloudCover;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    // === Constructors ===

    public WeatherHistory() {
        this.recordedAt = LocalDateTime.now();
    }

    // === Getters and Setters ===

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public LocalTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalTime recordTime) {
        this.recordTime = recordTime;
    }

    public Double getTempMax() {
        return tempMax;
    }

    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }

    public Double getTempMin() {
        return tempMin;
    }

    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    public Double getTempCurrent() {
        return tempCurrent;
    }

    public void setTempCurrent(Double tempCurrent) {
        this.tempCurrent = tempCurrent;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }

    public Double getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(Double precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(Double cloudCover) {
        this.cloudCover = cloudCover;
    }

    public Integer getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(Integer weatherCode) {
        this.weatherCode = weatherCode;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public String toString() {
        return "WeatherHistory{" +
                "id=" + id +
                ", province='" + province + '\'' +
                ", recordDate=" + recordDate +
                ", tempMax=" + tempMax +
                ", tempMin=" + tempMin +
                ", precipitationProbability=" + precipitationProbability +
                ", weatherCode=" + weatherCode +
                '}';
    }
}
