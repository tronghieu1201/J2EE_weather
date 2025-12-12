package com.weather.forecast.controller;

import com.weather.forecast.model.WeatherLog;
import com.weather.forecast.repository.WeatherLogRepository;
import com.weather.forecast.service.DataUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Arrays; // Import Arrays

@Controller
@RequestMapping("/admin")
public class DataUpdateController {

    private final DataUpdateService dataUpdateService;
    private final WeatherLogRepository weatherLogRepository;

    // List of cities to update. In a real app, this might come from a config file or database.
    private static final List<String> CITIES_TO_UPDATE = Arrays.asList(
            "An Giang", "Vung Tau", "Bac Giang", "Bac Kan", "Bac Lieu", "Bac Ninh",
            "Ben Tre", "Qui Nhon", "Binh Duong", "Binh Phuoc", "Binh Thuan", "Ca Mau",
            "Can Tho", "Cao Bang", "Da Nang", "Buon Ma Thuot", "Gia Nghia", "Dien Bien", "Bien Hoa",
            "Dong Thap", "Pleiku", "Ha Giang", "Ha Nam", "Ha Noi", "Ha Tinh", "Hai Duong",
            "Haiphong", "Hau Giang", "Hoa Binh", "Hung Yen", "Khanh Hoa", "Kien Giang",
            "Kon Tum", "Lai Chau", "Da Lat", "Lang Son", "Lao Cai", "Long An", "Nam Dinh",
            "Vinh", "Ninh Binh", "Ninh Thuan", "Phu Tho", "Phu Yen", "Quang Binh", "Tam Ky",
            "Quang Ngai", "Quang Ninh", "Quang Tri", "Soc Trang", "Son La", "Tay Ninh", "Thai Binh",
            "Thai Nguyen", "Thanh Hoa", "Hue", "Tien Giang", "Tra Vinh", "Tuyen Quang",
            "Vinh Long", "Vinh Phuc", "Yen Bai", "Ho Chi Minh City"
    );

    @Autowired
    public DataUpdateController(DataUpdateService dataUpdateService, WeatherLogRepository weatherLogRepository) {
        this.dataUpdateService = dataUpdateService;
        this.weatherLogRepository = weatherLogRepository;
    }

    @GetMapping("/data-update")
    public String showUpdatePage(Model model) {
        List<WeatherLog> logs = weatherLogRepository.findAllByOrderByUpdateTimeDesc();
        model.addAttribute("weatherLogs", logs);
        model.addAttribute("cities", CITIES_TO_UPDATE);
        return "update-data"; // This corresponds to 'update-data.html'
    }

    @PostMapping("/data-update")
    public String triggerUpdate(RedirectAttributes redirectAttributes) {
        System.out.println("Triggering data update for all cities...");
        int successCount = 0;
        int failCount = 0;

        for (String city : CITIES_TO_UPDATE) {
            try {
                dataUpdateService.collectAndSaveCurrentWeather(city);
                successCount++;
            } catch (Exception e) {
                System.err.println("Failed to update data for city '" + city + "': " + e.getMessage());
                failCount++;
            }
        }

        if (failCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Data update finished with " + failCount + " error(s). Check server logs for details.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Successfully updated weather data for " + successCount + " cities.");
        }

        return "redirect:/admin/data-update";
    }
}
