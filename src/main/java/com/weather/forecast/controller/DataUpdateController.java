package com.weather.forecast.controller;

import com.weather.forecast.model.WeatherHistory;
import com.weather.forecast.repository.WeatherHistoryRepository;
import com.weather.forecast.service.DataUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller để quản lý việc cập nhật dữ liệu thời tiết lịch sử.
 * Truy cập: /admin/data-update
 */
@Controller
@RequestMapping("/admin")
public class DataUpdateController {

    private final DataUpdateService dataUpdateService;
    private final WeatherHistoryRepository weatherHistoryRepository;

    @Autowired
    public DataUpdateController(DataUpdateService dataUpdateService,
            WeatherHistoryRepository weatherHistoryRepository) {
        this.dataUpdateService = dataUpdateService;
        this.weatherHistoryRepository = weatherHistoryRepository;
    }

    @GetMapping("/data-update")
    public String showUpdatePage(Model model) {
        List<WeatherHistory> records = weatherHistoryRepository.findAll();
        long totalRecords = weatherHistoryRepository.count();
        List<String> provinces = dataUpdateService.getAllProvinces();
        List<String> savedProvinces = weatherHistoryRepository.findDistinctProvinces();

        model.addAttribute("weatherRecords", records);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("provinces", provinces);
        model.addAttribute("savedProvinces", savedProvinces);

        return "update-data";
    }

    /**
     * Thu thập dữ liệu LỊCH SỬ 30 NGÀY cho tất cả 63 tỉnh.
     * Sử dụng Open-Meteo Archive API.
     */
    @PostMapping("/data-update")
    public String triggerHistoricalUpdate(
            @RequestParam(name = "type", defaultValue = "historical") String type,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== Data Update Request: " + type + " ===");

        try {
            if ("today".equals(type)) {
                // Thu thập chỉ ngày hôm nay
                int successCount = dataUpdateService.collectAllProvincesTodayData();
                long totalRecords = dataUpdateService.getTotalRecords();

                redirectAttributes.addFlashAttribute("successMessage",
                        "✓ Thu thập thành công dữ liệu NGÀY HÔM NAY cho " + successCount + " tỉnh/thành. " +
                                "Tổng số bản ghi: " + totalRecords);
            } else {
                // Thu thập 30 ngày lịch sử
                int savedRecords = dataUpdateService.collectAllProvincesHistoricalData();
                long totalRecords = dataUpdateService.getTotalRecords();

                redirectAttributes.addFlashAttribute("successMessage",
                        "✓ Thu thập thành công " + savedRecords + " bản ghi LỊCH SỬ 30 NGÀY. " +
                                "Tổng số bản ghi trong database: " + totalRecords);
            }
        } catch (Exception e) {
            System.err.println("Error during data collection: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/data-update";
    }
}