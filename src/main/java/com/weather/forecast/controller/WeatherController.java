package com.weather.forecast.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.dto.ComprehensiveWeatherReport;
import com.weather.forecast.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // Added for collecting all cities

@Controller
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;
    private final Map<String, List<String>> groupedCities;
    private final List<String> allProvinces; // Added to hold all provinces as a flat list

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
        this.groupedCities = initGroupedCities();
        this.allProvinces = new ArrayList<>(); // Initialize allProvinces
        this.groupedCities.values().forEach(allProvinces::addAll); // Populate allProvinces from groupedCities
    }

    // New mapping for the root, displaying only navigation
    @GetMapping("/")
    public String index(Model model) { // Add Model parameter
        model.addAttribute("groupedCities", groupedCities); // Pass groupedCities
        return "index";
    }

    // Existing weather forecast logic, moved to /weather-forecast
    @GetMapping("/weather-forecast")
    public String getWeatherForecast(@RequestParam(name = "city", required = false) String city, Model model) {
        // If no city is specified, use a default
        if (city == null || city.isEmpty()) {
            city = "Hà Nội"; // Default city
        }
        logger.info("Searching for city: {}", city);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<DailyForecast> sevenDayForecast = weatherService.get7DayXGBoostForecast(city);

        if (comprehensiveReport != null) {
            logger.info("Successfully retrieved comprehensive report for {}. Temperature: {}°C", city, comprehensiveReport.getCurrent().getTemperature());
        } else {
            logger.warn("Failed to retrieve comprehensive report for {}", city);
        }
        if (sevenDayForecast != null && !sevenDayForecast.isEmpty()) {
            logger.info("Successfully retrieved 7-day forecast for {}", city);
        } else {
            logger.warn("Failed to retrieve 7-day forecast for {}", city);
        }

        model.addAttribute("comprehensiveReport", comprehensiveReport); // For current weather details
        model.addAttribute("sevenDayForecast", sevenDayForecast);       // For 7-day XGBoost prediction
        model.addAttribute("city", city); // Use the searched city name
        return "weather_forecast_detail"; // Return the new detail template
    }

    // Handle POST request from the search bar on index.html (or other pages)
    @PostMapping("/weather-forecast")
    public String postWeatherForecast(@RequestParam("city") String city) {
        // Redirect to GET /weather-forecast to make the URL cleaner and bookmarkable
        return "redirect:/weather-forecast?city=" + city;
    }

    // New mapping for Tỉnh/Thành phố page
    @GetMapping("/provinces")
    public String showProvinces(Model model) {
        model.addAttribute("provinces", allProvinces); // Pass the flat list of all provinces
        return "provinces";
    }

    // New mapping for Lịch Vạn Niên page
    @GetMapping("/perpetual-calendar")
    public String showPerpetualCalendar() {
        return "perpetual_calendar";
    }

    // New mapping for Tin Thời Tiết page
    @GetMapping("/weather-news")
    public String showWeatherNews() {
        return "weather_news";
    }

    private Map<String, List<String>> initGroupedCities() {
        Map<String, List<String>> cities = new LinkedHashMap<>();
        cities.put("Đông Bắc Bộ", List.of("Hà Giang", "Cao Bằng", "Bắc Kạn", "Lạng Sơn", "Tuyên Quang", "Thái Nguyên", "Phú Thọ", "Bắc Giang", "Quảng Ninh"));
        cities.put("Tây Bắc Bộ", List.of("Hòa Bình", "Sơn La", "Điện Biên", "Lai Châu", "Lào Cai", "Yên Bái"));
        cities.put("Đồng Bằng Sông Hồng", List.of("Bắc Ninh", "Hà Nam", "Hà Nội", "Hải Dương", "Hải Phòng", "Hưng Yên", "Nam Định", "Ninh Bình", "Thái Bình", "Vĩnh Phúc"));
        cities.put("Bắc Trung Bộ", List.of("Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Bình", "Quảng Trị", "Thừa Thiên Huế"));
        cities.put("Nam Trung Bộ", List.of("Đà Nẵng", "Quảng Nam", "Quảng Ngãi", "Bình Định", "Phú Yên", "Khánh Hòa", "Ninh Thuận", "Bình Thuận"));
        cities.put("Tây Nguyên", List.of("Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng"));
        cities.put("Đông Nam Bộ", List.of("Bình Phước", "Bình Dương", "Đồng Nai", "Tây Ninh", "Bà Rịa - Vũng Tàu", "Hồ Chí Minh"));
        cities.put("Đồng Bằng Sông Cửu Long", List.of("An Giang", "Bến Tre", "Bạc Liêu", "Cà Mau", "Cần Thơ", "Đồng Tháp", "Hậu Giang", "Kiên Giang", "Long An", "Sóc Trăng", "Tiền Giang", "Trà Vinh", "Vĩnh Long"));
        return cities;
    }
}

