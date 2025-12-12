package com.weather.forecast.repository;

import com.weather.forecast.model.WeatherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link WeatherRecord} entity.
 */
@Repository
public interface WeatherRepository extends JpaRepository<WeatherRecord, Long> {

    /**
     * Finds a list of weather records for a given province since a specific date.
     * @param province The province to search for.
     * @param date The start date.
     * @return A list of weather records ordered by date and time descending.
     */
    List<WeatherRecord> findByProvinceAndDateAfterOrderByDateDescTimeDesc(String province, LocalDate date);
}
