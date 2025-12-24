package com.weather.forecast.repository;

import com.weather.forecast.model.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository cho bảng weather_alerts - quản lý cảnh báo thời tiết.
 */
@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    /**
     * Tìm tất cả cảnh báo đang hoạt động.
     */
    List<WeatherAlert> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Tìm cảnh báo theo loại.
     */
    List<WeatherAlert> findByAlertTypeOrderByCreatedAtDesc(String alertType);

    /**
     * Tìm cảnh báo theo mức độ nghiêm trọng.
     */
    List<WeatherAlert> findBySeverityOrderByCreatedAtDesc(String severity);

    /**
     * Tìm cảnh báo đang có hiệu lực (trong khoảng thời gian).
     */
    @Query("SELECT a FROM WeatherAlert a WHERE a.isActive = true " +
            "AND (a.startDate IS NULL OR a.startDate <= :today) " +
            "AND (a.endDate IS NULL OR a.endDate >= :today) " +
            "ORDER BY CASE a.severity " +
            "WHEN 'CRITICAL' THEN 1 " +
            "WHEN 'HIGH' THEN 2 " +
            "WHEN 'MEDIUM' THEN 3 " +
            "WHEN 'LOW' THEN 4 " +
            "ELSE 5 END")
    List<WeatherAlert> findActiveAlertsForToday(LocalDate today);

    /**
     * Đếm số cảnh báo đang hoạt động.
     */
    long countByIsActiveTrue();

    /**
     * Tìm cảnh báo ảnh hưởng đến một tỉnh cụ thể.
     */
    @Query("SELECT a FROM WeatherAlert a WHERE a.isActive = true " +
            "AND a.affectedProvinces LIKE %:province% " +
            "ORDER BY a.createdAt DESC")
    List<WeatherAlert> findByAffectedProvince(String province);
}
