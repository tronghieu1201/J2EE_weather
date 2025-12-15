package com.weather.forecast.repository;

import com.weather.forecast.model.WeatherHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng weather_history - cung cấp các phương thức truy vấn dữ
 * liệu lịch sử.
 */
@Repository
public interface WeatherHistoryRepository extends JpaRepository<WeatherHistory, Long> {

    /**
     * Tìm tất cả bản ghi của một tỉnh, sắp xếp theo ngày giảm dần.
     */
    List<WeatherHistory> findByProvinceOrderByRecordDateDesc(String province);

    /**
     * Tìm bản ghi của một tỉnh trong một ngày cụ thể.
     */
    Optional<WeatherHistory> findByProvinceAndRecordDate(String province, LocalDate recordDate);

    /**
     * Tìm N bản ghi gần nhất của một tỉnh (dùng cho features của XGBoost).
     */
    List<WeatherHistory> findTop7ByProvinceOrderByRecordDateDesc(String province);

    /**
     * Tìm các bản ghi trong khoảng thời gian.
     */
    List<WeatherHistory> findByProvinceAndRecordDateBetweenOrderByRecordDateAsc(
            String province, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy tất cả bản ghi để huấn luyện model.
     */
    @Query("SELECT w FROM WeatherHistory w ORDER BY w.province, w.recordDate")
    List<WeatherHistory> findAllForTraining();

    /**
     * Đếm số lượng bản ghi theo tỉnh.
     */
    long countByProvince(String province);

    /**
     * Kiểm tra xem đã có bản ghi cho ngày này chưa.
     */
    boolean existsByProvinceAndRecordDate(String province, LocalDate recordDate);

    /**
     * Lấy danh sách các tỉnh đã có dữ liệu.
     */
    @Query("SELECT DISTINCT w.province FROM WeatherHistory w ORDER BY w.province")
    List<String> findDistinctProvinces();
}
