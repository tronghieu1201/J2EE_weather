package com.weather.forecast.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity lưu kết quả dự đoán XGBoost để so sánh với thực tế.
 * Giúp đánh giá accuracy của model theo thời gian.
 */
@Entity
@Table(name = "prediction_results", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "province", "prediction_date" })
})
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    // === Giá trị dự đoán từ XGBoost ===
    @Column(name = "predicted_max_temp")
    private Double predictedMaxTemp;

    @Column(name = "predicted_min_temp")
    private Double predictedMinTemp;

    @Column(name = "predicted_rain_prob")
    private Double predictedRainProb;

    @Column(name = "predicted_weather_code")
    private Integer predictedWeatherCode;

    // === Giá trị thực tế (cập nhật sau khi có data) ===
    @Column(name = "actual_max_temp")
    private Double actualMaxTemp;

    @Column(name = "actual_min_temp")
    private Double actualMinTemp;

    @Column(name = "actual_rain_prob")
    private Double actualRainProb;

    @Column(name = "actual_weather_code")
    private Integer actualWeatherCode;

    // === Metrics tính toán ===
    @Column(name = "mae_max_temp")
    private Double maeMaxTemp; // |predicted - actual|

    @Column(name = "mae_min_temp")
    private Double maeMinTemp;

    // === Timestamps ===
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    // === Constructors ===
    public PredictionResult() {
        this.createdAt = LocalDateTime.now();
        this.isVerified = false;
    }

    public PredictionResult(String province, LocalDate predictionDate,
            Double predictedMaxTemp, Double predictedMinTemp, Double predictedRainProb) {
        this();
        this.province = province;
        this.predictionDate = predictionDate;
        this.predictedMaxTemp = predictedMaxTemp;
        this.predictedMinTemp = predictedMinTemp;
        this.predictedRainProb = predictedRainProb;
    }

    // === Helper method để verify với actual data ===
    public void verifyWithActual(Double actualMax, Double actualMin, Double actualRain, Integer actualCode) {
        this.actualMaxTemp = actualMax;
        this.actualMinTemp = actualMin;
        this.actualRainProb = actualRain;
        this.actualWeatherCode = actualCode;

        // Tính MAE
        if (predictedMaxTemp != null && actualMax != null) {
            this.maeMaxTemp = Math.abs(predictedMaxTemp - actualMax);
        }
        if (predictedMinTemp != null && actualMin != null) {
            this.maeMinTemp = Math.abs(predictedMinTemp - actualMin);
        }

        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
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

    public LocalDate getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(LocalDate predictionDate) {
        this.predictionDate = predictionDate;
    }

    public Double getPredictedMaxTemp() {
        return predictedMaxTemp;
    }

    public void setPredictedMaxTemp(Double predictedMaxTemp) {
        this.predictedMaxTemp = predictedMaxTemp;
    }

    public Double getPredictedMinTemp() {
        return predictedMinTemp;
    }

    public void setPredictedMinTemp(Double predictedMinTemp) {
        this.predictedMinTemp = predictedMinTemp;
    }

    public Double getPredictedRainProb() {
        return predictedRainProb;
    }

    public void setPredictedRainProb(Double predictedRainProb) {
        this.predictedRainProb = predictedRainProb;
    }

    public Integer getPredictedWeatherCode() {
        return predictedWeatherCode;
    }

    public void setPredictedWeatherCode(Integer predictedWeatherCode) {
        this.predictedWeatherCode = predictedWeatherCode;
    }

    public Double getActualMaxTemp() {
        return actualMaxTemp;
    }

    public void setActualMaxTemp(Double actualMaxTemp) {
        this.actualMaxTemp = actualMaxTemp;
    }

    public Double getActualMinTemp() {
        return actualMinTemp;
    }

    public void setActualMinTemp(Double actualMinTemp) {
        this.actualMinTemp = actualMinTemp;
    }

    public Double getActualRainProb() {
        return actualRainProb;
    }

    public void setActualRainProb(Double actualRainProb) {
        this.actualRainProb = actualRainProb;
    }

    public Integer getActualWeatherCode() {
        return actualWeatherCode;
    }

    public void setActualWeatherCode(Integer actualWeatherCode) {
        this.actualWeatherCode = actualWeatherCode;
    }

    public Double getMaeMaxTemp() {
        return maeMaxTemp;
    }

    public void setMaeMaxTemp(Double maeMaxTemp) {
        this.maeMaxTemp = maeMaxTemp;
    }

    public Double getMaeMinTemp() {
        return maeMinTemp;
    }

    public void setMaeMinTemp(Double maeMinTemp) {
        this.maeMinTemp = maeMinTemp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    @Override
    public String toString() {
        return "PredictionResult{" +
                "province='" + province + '\'' +
                ", predictionDate=" + predictionDate +
                ", predictedMax=" + predictedMaxTemp +
                ", actualMax=" + actualMaxTemp +
                ", maeMax=" + maeMaxTemp +
                ", isVerified=" + isVerified +
                '}';
    }
}
