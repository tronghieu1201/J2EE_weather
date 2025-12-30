package com.weather.forecast.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu thống kê performance của model sau mỗi lần training.
 * Giúp theo dõi model cải thiện hay xấu đi qua thời gian.
 */
@Entity
@Table(name = "model_metrics")
public class ModelMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName; // max_temp, min_temp, rain_prob

    @Column(name = "model_version", length = 50)
    private String modelVersion; // v1.0, v1.1, v2.0...

    // === Performance Metrics ===
    @Column(nullable = false)
    private Double rmse; // Root Mean Square Error

    @Column(nullable = false)
    private Double mae; // Mean Absolute Error

    @Column(name = "r2_score")
    private Double r2Score; // R² score (0-1)

    // === Training Info ===
    @Column(name = "train_samples")
    private Integer trainSamples;

    @Column(name = "test_samples")
    private Integer testSamples;

    @Column(name = "training_duration_sec")
    private Integer trainingDurationSec;

    // === Hyperparameters (JSON format) ===
    @Column(columnDefinition = "TEXT")
    private String hyperparameters;

    // === Timestamps ===
    @Column(name = "trained_at", nullable = false)
    private LocalDateTime trainedAt;

    @Column(name = "trained_by", length = 100)
    private String trainedBy; // Người thực hiện training

    @Column(length = 500)
    private String notes; // Ghi chú

    // === Constructors ===
    public ModelMetrics() {
        this.trainedAt = LocalDateTime.now();
    }

    public ModelMetrics(String modelName, Double rmse, Double mae) {
        this();
        this.modelName = modelName;
        this.rmse = rmse;
        this.mae = mae;
    }

    public ModelMetrics(String modelName, String modelVersion, Double rmse, Double mae,
            Integer trainSamples, Integer testSamples) {
        this();
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.rmse = rmse;
        this.mae = mae;
        this.trainSamples = trainSamples;
        this.testSamples = testSamples;
    }

    // === Getters and Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Double getRmse() {
        return rmse;
    }

    public void setRmse(Double rmse) {
        this.rmse = rmse;
    }

    public Double getMae() {
        return mae;
    }

    public void setMae(Double mae) {
        this.mae = mae;
    }

    public Double getR2Score() {
        return r2Score;
    }

    public void setR2Score(Double r2Score) {
        this.r2Score = r2Score;
    }

    public Integer getTrainSamples() {
        return trainSamples;
    }

    public void setTrainSamples(Integer trainSamples) {
        this.trainSamples = trainSamples;
    }

    public Integer getTestSamples() {
        return testSamples;
    }

    public void setTestSamples(Integer testSamples) {
        this.testSamples = testSamples;
    }

    public Integer getTrainingDurationSec() {
        return trainingDurationSec;
    }

    public void setTrainingDurationSec(Integer trainingDurationSec) {
        this.trainingDurationSec = trainingDurationSec;
    }

    public String getHyperparameters() {
        return hyperparameters;
    }

    public void setHyperparameters(String hyperparameters) {
        this.hyperparameters = hyperparameters;
    }

    public LocalDateTime getTrainedAt() {
        return trainedAt;
    }

    public void setTrainedAt(LocalDateTime trainedAt) {
        this.trainedAt = trainedAt;
    }

    public String getTrainedBy() {
        return trainedBy;
    }

    public void setTrainedBy(String trainedBy) {
        this.trainedBy = trainedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "ModelMetrics{" +
                "modelName='" + modelName + '\'' +
                ", version='" + modelVersion + '\'' +
                ", rmse=" + rmse +
                ", mae=" + mae +
                ", trainSamples=" + trainSamples +
                ", trainedAt=" + trainedAt +
                '}';
    }
}
