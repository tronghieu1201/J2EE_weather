package com.weather.forecast.repository;

import com.weather.forecast.model.ModelMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelMetricsRepository extends JpaRepository<ModelMetrics, Long> {

    /**
     * Lấy metrics mới nhất của một model
     */
    Optional<ModelMetrics> findTopByModelNameOrderByTrainedAtDesc(String modelName);

    /**
     * Lấy lịch sử training của một model
     */
    List<ModelMetrics> findByModelNameOrderByTrainedAtDesc(String modelName);

    /**
     * Lấy metrics của version cụ thể
     */
    Optional<ModelMetrics> findByModelNameAndModelVersion(String modelName, String modelVersion);

    /**
     * Lấy tất cả metrics mới nhất (mỗi model 1 record)
     */
    @Query("SELECT m FROM ModelMetrics m WHERE m.trainedAt = " +
            "(SELECT MAX(m2.trainedAt) FROM ModelMetrics m2 WHERE m2.modelName = m.modelName)")
    List<ModelMetrics> findLatestMetricsForAllModels();

    /**
     * Lấy model có RMSE thấp nhất
     */
    Optional<ModelMetrics> findTopByModelNameOrderByRmseAsc(String modelName);
}
