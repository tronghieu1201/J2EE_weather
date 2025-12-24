package com.weather.forecast.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service chạy các tác vụ tự động theo lịch.
 * - Thu thập dữ liệu thời tiết hàng ngày
 * - Cập nhật trạng thái cảnh báo
 */
@Service
@EnableScheduling
public class ScheduledTasks {

    private final DataUpdateService dataUpdateService;

    private LocalDateTime lastDataCollectionTime;
    private String lastDataCollectionStatus;
    private int lastCollectionRecords;

    @Autowired
    public ScheduledTasks(DataUpdateService dataUpdateService) {
        this.dataUpdateService = dataUpdateService;
        this.lastDataCollectionStatus = "Chưa chạy";
        this.lastCollectionRecords = 0;
    }

    /**
     * Thu thập dữ liệu thời tiết hàng ngày vào lúc 6:00 sáng.
     * Cron: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void collectDailyWeatherData() {
        System.out.println("=== [SCHEDULER] Bắt đầu thu thập dữ liệu tự động: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ===");

        try {
            int successCount = dataUpdateService.collectAllProvincesTodayData();

            lastDataCollectionTime = LocalDateTime.now();
            lastDataCollectionStatus = "Thành công";
            lastCollectionRecords = successCount;

            System.out.println("=== [SCHEDULER] Thu thập thành công " + successCount + " tỉnh/thành ===");
        } catch (Exception e) {
            lastDataCollectionTime = LocalDateTime.now();
            lastDataCollectionStatus = "Lỗi: " + e.getMessage();
            lastCollectionRecords = 0;

            System.err.println("=== [SCHEDULER] Lỗi thu thập dữ liệu: " + e.getMessage() + " ===");
            e.printStackTrace();
        }
    }

    /**
     * Chạy thử thu thập dữ liệu (dùng để test).
     * Có thể gọi từ controller để kích hoạt thủ công.
     */
    public void triggerManualCollection() {
        collectDailyWeatherData();
    }

    // === Getters cho Dashboard ===

    public LocalDateTime getLastDataCollectionTime() {
        return lastDataCollectionTime;
    }

    public String getLastDataCollectionStatus() {
        return lastDataCollectionStatus;
    }

    public int getLastCollectionRecords() {
        return lastCollectionRecords;
    }

    public String getNextScheduledRun() {
        // Tính thời gian chạy tiếp theo (6:00 sáng ngày mai)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun;

        if (now.getHour() < 6) {
            nextRun = now.withHour(6).withMinute(0).withSecond(0);
        } else {
            nextRun = now.plusDays(1).withHour(6).withMinute(0).withSecond(0);
        }

        return nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public boolean isSchedulerEnabled() {
        return true; // Luôn enabled khi bean được tạo
    }
}
