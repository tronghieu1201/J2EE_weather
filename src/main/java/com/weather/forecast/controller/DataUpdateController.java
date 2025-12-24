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
 * Truy cập: /admin/data-update?token=YOUR_SECRET_KEY
 */
@Controller
@RequestMapping("/admin")
public class DataUpdateController {

    private final DataUpdateService dataUpdateService;
    private final WeatherHistoryRepository weatherHistoryRepository;

    // Key cố định cho admin
    private final String adminSecretKey = "j2weather.%s@angtesas/ang/a222/TH@2";

    @Autowired
    public DataUpdateController(DataUpdateService dataUpdateService,
            WeatherHistoryRepository weatherHistoryRepository) {
        this.dataUpdateService = dataUpdateService;
        this.weatherHistoryRepository = weatherHistoryRepository;
    }

    @GetMapping("/data-update")
    public String showUpdatePage(@RequestParam(name = "token", required = false) String token, Model model) {
        // Nếu không có token, hiển thị trang đăng nhập
        if (token == null || token.isEmpty()) {
            return "admin-login"; // Trang yêu cầu nhập token
        }

        // Kiểm tra token xác thực - nếu sai thì vào trang trắng
        if (!token.equals(adminSecretKey)) {
            return "blank-page"; // Trang trắng khi nhập sai key
        }

        // Token hợp lệ - tiếp tục hiển thị trang admin
        model.addAttribute("token", token); // Truyền token để dùng trong form POST

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
            @RequestParam(name = "token", required = false) String token,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra token xác thực - nếu sai thì vào trang trắng
        if (token == null || !token.equals(adminSecretKey)) {
            return "blank-page"; // Trang trắng khi token không hợp lệ
        }

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

        // Redirect với token để giữ phiên đăng nhập
        return "redirect:/admin/data-update?token=" + token;
    }
}