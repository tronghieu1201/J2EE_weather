package com.weather.forecast.controller;

import com.weather.forecast.model.WeatherAlert;
import com.weather.forecast.repository.WeatherAlertRepository;
import com.weather.forecast.repository.WeatherHistoryRepository;
import com.weather.forecast.service.AdminService;
import com.weather.forecast.service.ScheduledTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller cho Admin Dashboard.
 * Quản lý: thống kê, accuracy tracking, scheduler, cảnh báo thời tiết.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminService adminService;
    private final ScheduledTasks scheduledTasks;
    private final WeatherAlertRepository weatherAlertRepository;
    private final WeatherHistoryRepository weatherHistoryRepository;

    @Value("${admin.secret.key}")
    private String adminSecretKey;

    @Autowired
    public AdminDashboardController(AdminService adminService,
            ScheduledTasks scheduledTasks,
            WeatherAlertRepository weatherAlertRepository,
            WeatherHistoryRepository weatherHistoryRepository) {
        this.adminService = adminService;
        this.scheduledTasks = scheduledTasks;
        this.weatherAlertRepository = weatherAlertRepository;
        this.weatherHistoryRepository = weatherHistoryRepository;
    }

    /**
     * Kiểm tra token xác thực.
     */
    private boolean isValidToken(String token) {
        return token != null && token.equals(adminSecretKey);
    }

    // ==================== DASHBOARD ====================

    /**
     * Trang Dashboard chính.
     */
    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(name = "token", required = false) String token, Model model) {
        if (!isValidToken(token)) {
            model.addAttribute("error", "Bạn cần token hợp lệ để truy cập trang này!");
            return "admin-login";
        }

        model.addAttribute("token", token);

        // Dashboard Stats
        Map<String, Object> stats = adminService.getDashboardStats();
        model.addAttribute("stats", stats);

        // Accuracy Metrics
        Map<String, Object> accuracy = adminService.getAccuracyMetrics();
        model.addAttribute("accuracy", accuracy);

        // System Status
        Map<String, Object> systemStatus = adminService.getSystemStatus();
        model.addAttribute("systemStatus", systemStatus);

        // Scheduler Info
        model.addAttribute("lastCollectionTime", scheduledTasks.getLastDataCollectionTime());
        model.addAttribute("lastCollectionStatus", scheduledTasks.getLastDataCollectionStatus());
        model.addAttribute("nextScheduledRun", scheduledTasks.getNextScheduledRun());
        model.addAttribute("schedulerEnabled", scheduledTasks.isSchedulerEnabled());

        // Top Provinces
        List<Map<String, Object>> topProvinces = adminService.getTopProvinces(5);
        model.addAttribute("topProvinces", topProvinces);

        // Active Alerts
        List<WeatherAlert> activeAlerts = weatherAlertRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        model.addAttribute("activeAlerts", activeAlerts);
        model.addAttribute("alertCount", activeAlerts.size());

        return "admin-dashboard";
    }

    // ==================== ALERTS MANAGEMENT ====================

    /**
     * Trang quản lý cảnh báo.
     */
    @GetMapping("/alerts")
    public String showAlerts(@RequestParam(name = "token", required = false) String token, Model model) {
        if (!isValidToken(token)) {
            model.addAttribute("error", "Bạn cần token hợp lệ để truy cập trang này!");
            return "admin-login";
        }

        model.addAttribute("token", token);
        model.addAttribute("alerts", weatherAlertRepository.findAll());
        model.addAttribute("provinces", weatherHistoryRepository.findDistinctProvinces());

        return "admin-alerts";
    }

    /**
     * Tạo cảnh báo mới.
     */
    @PostMapping("/alerts/create")
    public String createAlert(@RequestParam(name = "token") String token,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String alertType,
            @RequestParam String severity,
            @RequestParam(required = false) String affectedProvinces,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            RedirectAttributes redirectAttributes) {
        if (!isValidToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token không hợp lệ!");
            return "redirect:/admin/alerts";
        }

        try {
            WeatherAlert alert = new WeatherAlert(title, description, alertType, severity);
            alert.setAffectedProvinces(affectedProvinces);

            if (startDate != null && !startDate.isEmpty()) {
                alert.setStartDate(LocalDate.parse(startDate));
            }
            if (endDate != null && !endDate.isEmpty()) {
                alert.setEndDate(LocalDate.parse(endDate));
            }

            weatherAlertRepository.save(alert);
            redirectAttributes.addFlashAttribute("successMessage", "✓ Đã tạo cảnh báo: " + title);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/alerts?token=" + token;
    }

    /**
     * Bật/tắt cảnh báo.
     */
    @PostMapping("/alerts/{id}/toggle")
    public String toggleAlert(@PathVariable Long id,
            @RequestParam(name = "token") String token,
            RedirectAttributes redirectAttributes) {
        if (!isValidToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token không hợp lệ!");
            return "redirect:/admin/alerts";
        }

        try {
            WeatherAlert alert = weatherAlertRepository.findById(id).orElse(null);
            if (alert != null) {
                alert.setIsActive(!alert.getIsActive());
                weatherAlertRepository.save(alert);
                redirectAttributes.addFlashAttribute("successMessage",
                        "✓ Đã " + (alert.getIsActive() ? "kích hoạt" : "tắt") + " cảnh báo: " + alert.getTitle());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/alerts?token=" + token;
    }

    /**
     * Xóa cảnh báo.
     */
    @PostMapping("/alerts/{id}/delete")
    public String deleteAlert(@PathVariable Long id,
            @RequestParam(name = "token") String token,
            RedirectAttributes redirectAttributes) {
        if (!isValidToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token không hợp lệ!");
            return "redirect:/admin/alerts";
        }

        try {
            weatherAlertRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "✓ Đã xóa cảnh báo");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/alerts?token=" + token;
    }

    // ==================== SCHEDULER CONTROL ====================

    /**
     * Kích hoạt thu thập dữ liệu thủ công.
     */
    @PostMapping("/scheduler/run-now")
    public String runSchedulerNow(@RequestParam(name = "token") String token,
            RedirectAttributes redirectAttributes) {
        if (!isValidToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token không hợp lệ!");
            return "redirect:/admin/dashboard";
        }

        try {
            scheduledTasks.triggerManualCollection();
            redirectAttributes.addFlashAttribute("successMessage",
                    "✓ Đã kích hoạt thu thập dữ liệu. Trạng thái: " + scheduledTasks.getLastDataCollectionStatus());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?token=" + token;
    }
}
