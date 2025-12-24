package com.weather.forecast.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity lưu cảnh báo thời tiết (bão, lũ, nắng nóng, etc.)
 * Admin có thể thêm/sửa/xóa cảnh báo để hiển thị cho user.
 */
@Entity
@Table(name = "weather_alerts")
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // STORM, FLOOD, HEAT, COLD, RAIN, OTHER

    @Column(name = "severity", nullable = false, length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "affected_provinces", length = 500)
    private String affectedProvinces; // Comma-separated list

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // === Constructors ===

    public WeatherAlert() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    public WeatherAlert(String title, String description, String alertType, String severity) {
        this();
        this.title = title;
        this.description = description;
        this.alertType = alertType;
        this.severity = severity;
    }

    // === Getters and Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAffectedProvinces() {
        return affectedProvinces;
    }

    public void setAffectedProvinces(String affectedProvinces) {
        this.affectedProvinces = affectedProvinces;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // === Helper Methods ===

    public String getSeverityColor() {
        switch (severity != null ? severity.toUpperCase() : "") {
            case "CRITICAL":
                return "danger";
            case "HIGH":
                return "warning";
            case "MEDIUM":
                return "info";
            case "LOW":
                return "secondary";
            default:
                return "secondary";
        }
    }

    public String getAlertIcon() {
        switch (alertType != null ? alertType.toUpperCase() : "") {
            case "STORM":
                return "bi-cloud-lightning-rain";
            case "FLOOD":
                return "bi-water";
            case "HEAT":
                return "bi-thermometer-sun";
            case "COLD":
                return "bi-snow";
            case "RAIN":
                return "bi-cloud-rain-heavy";
            default:
                return "bi-exclamation-triangle";
        }
    }

    @Override
    public String toString() {
        return "WeatherAlert{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", alertType='" + alertType + '\'' +
                ", severity='" + severity + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
