package com.weather.forecast.repository;

import com.weather.forecast.model.WeatherLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherLogRepository extends JpaRepository<WeatherLog, Long> {
    List<WeatherLog> findAllByOrderByUpdateTimeDesc();
}
