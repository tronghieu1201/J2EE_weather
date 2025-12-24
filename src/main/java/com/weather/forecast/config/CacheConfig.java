package com.weather.forecast.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Cấu hình cache cho ứng dụng.
 * Cache thời tiết sẽ tự động xoá sau 5 phút để đảm bảo dữ liệu luôn mới.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new ConcurrentMapCache("weatherReports"),
                new ConcurrentMapCache("coordinates"),
                new ConcurrentMapCache("prominentProvincesWeather")));
        return cacheManager;
    }

    /**
     * Tự động xoá cache mỗi 5 phút để cập nhật dữ liệu thời tiết mới.
     */
    @CacheEvict(value = { "weatherReports", "coordinates", "prominentProvincesWeather" }, allEntries = true)
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000 ms
    public void evictAllCaches() {
        System.out.println("♻️ Cache cleared - Weather data will be refreshed");
    }
}
