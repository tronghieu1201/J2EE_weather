package com.weather.forecast.service;

import com.weather.forecast.model.WeatherLog;
import com.weather.forecast.model.dto.ComprehensiveWeatherReport;
import com.weather.forecast.repository.WeatherLogRepository;
import com.weather.forecast.util.WeatherCodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service để lưu log thời tiết vào database.
 * Mỗi khi user tra cứu thời tiết, dữ liệu sẽ được log vào bảng weather_logs.
 */
@Service
public class WeatherLogService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherLogService.class);

    private final WeatherLogRepository weatherLogRepository;

    @Autowired
    public WeatherLogService(WeatherLogRepository weatherLogRepository) {
        this.weatherLogRepository = weatherLogRepository;
    }

    /**
     * Lưu weather data vào database (chạy async để không block request chính).
     * 
     * @param city   Tên thành phố/tỉnh
     * @param report Dữ liệu thời tiết từ API
     */
    @Async
    public void logWeatherData(String city, ComprehensiveWeatherReport report) {
        try {
            if (report == null || report.getCurrent() == null) {
                logger.warn("Không thể log weather data cho {}: report hoặc current data null", city);
                return;
            }

            ComprehensiveWeatherReport.CurrentWeather current = report.getCurrent();

            WeatherLog log = new WeatherLog();
            log.setCity(city);
            log.setTemperature(current.getTemperature());
            log.setHumidity(current.getHumidity());
            log.setWindSpeed(current.getWindSpeed());

            // Map weather code to conditions description
            String conditions = WeatherCodeMapper.getDescription(current.getWeatherCode());
            log.setConditions(conditions);

            // Parse update time từ API (format: "2025-12-28T15:00")
            LocalDateTime updateTime = parseApiTime(current.getTime());
            log.setUpdateTime(updateTime);

            // Thời điểm lưu vào DB
            log.setSavedDate(LocalDateTime.now());

            weatherLogRepository.save(log);
            logger.info("✓ Đã lưu weather log cho {}: {}°C, {}", city, current.getTemperature(), conditions);

        } catch (Exception e) {
            logger.error("Lỗi khi lưu weather log cho {}: {}", city, e.getMessage());
        }
    }

    /**
     * Parse thời gian từ API response.
     */
    private LocalDateTime parseApiTime(String timeString) {
        try {
            if (timeString != null && !timeString.isEmpty()) {
                return LocalDateTime.parse(timeString);
            }
        } catch (Exception e) {
            logger.warn("Không thể parse time: {}", timeString);
        }
        return LocalDateTime.now();
    }

    /**
     * Lấy số lượng logs đã lưu.
     */
    public long getLogCount() {
        return weatherLogRepository.count();
    }
}
