package com.weather.forecast.repository;

import com.weather.forecast.model.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {

    /**
     * Tìm dự đoán theo tỉnh và ngày
     */
    Optional<PredictionResult> findByProvinceAndPredictionDate(String province, LocalDate predictionDate);

    /**
     * Lấy các dự đoán chưa verify (để cập nhật actual data)
     */
    List<PredictionResult> findByIsVerifiedFalseAndPredictionDateBefore(LocalDate date);

    /**
     * Lấy dự đoán gần nhất của một tỉnh
     */
    List<PredictionResult> findTop7ByProvinceOrderByPredictionDateDesc(String province);

    /**
     * Lấy tất cả dự đoán đã verify để tính accuracy
     */
    List<PredictionResult> findByIsVerifiedTrue();

    /**
     * Tính trung bình MAE của max temp theo tỉnh
     */
    @Query("SELECT AVG(p.maeMaxTemp) FROM PredictionResult p WHERE p.province = ?1 AND p.isVerified = true")
    Double getAverageMaeMaxTempByProvince(String province);

    /**
     * Tính trung bình MAE tổng thể
     */
    @Query("SELECT AVG(p.maeMaxTemp) FROM PredictionResult p WHERE p.isVerified = true")
    Double getOverallAverageMaeMaxTemp();
}
