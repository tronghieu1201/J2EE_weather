package com.weather.forecast.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.dto.ComprehensiveWeatherReport;
import com.weather.forecast.model.HourlyForecast; // Import HourlyForecast
import com.weather.forecast.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat; // Import DateTimeFormat

import java.time.LocalDate; // Import LocalDate
import java.time.OffsetDateTime; // Import OffsetDateTime
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

    // New mapping for the root, now displaying weather forecast
    @GetMapping("/")
    public String index(@RequestParam(name = "city", required = false) String city, Model model) {
        model.addAttribute("groupedCities", groupedCities); // Pass groupedCities for mega menu

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
        return "index"; // Return index template
    }

    // Handle POST request from the search bar on index.html (or other pages)
    @PostMapping("/") // Change to root POST mapping
    public String postWeatherForecast(@RequestParam("city") String city, Model model) {
        // This will now directly populate the index page, not redirect
        model.addAttribute("groupedCities", groupedCities); // Pass groupedCities for mega menu

        logger.info("User searching for city (POST): {}", city);

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
        return "index"; // Return index template
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

    @GetMapping("/get-weather-fragment")
    public String getWeatherFragment(@RequestParam(name = "city", required = false) String city, Model model) {
        if (city == null || city.isEmpty()) {
            city = "Hà Nội"; // Default city
        }
        logger.info("Fetching weather fragment for city: {}", city);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<DailyForecast> sevenDayForecast = weatherService.get7DayXGBoostForecast(city);

        model.addAttribute("comprehensiveReport", comprehensiveReport);
        model.addAttribute("sevenDayForecast", sevenDayForecast);
        model.addAttribute("city", city);

        return "index :: weatherContent"; // Return the weatherContent fragment from index.html
    }

    @GetMapping("/hourly-details")
    public String showHourlyDetails(
            @RequestParam("city") String city,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        logger.info("Fetching hourly details for city: {} on date: {}", city, date);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<HourlyForecast> hourlyForecastForDay = new ArrayList<>(); // Declare and initialize here

        if (comprehensiveReport != null && comprehensiveReport.getHourly() != null) {
            List<String> hourlyTimes = comprehensiveReport.getHourly().getTime();
            logger.debug("Hourly data time array size for {}: {}", city, hourlyTimes != null ? hourlyTimes.size() : 0);
            if (hourlyTimes != null && !hourlyTimes.isEmpty()) {
                logger.debug("Last hourly timestamp for {}: {}", city, hourlyTimes.get(hourlyTimes.size() - 1));
            }
            hourlyForecastForDay = filterHourlyForecastByDate(comprehensiveReport.getHourly(), date);
        }

        model.addAttribute("city", city);
        model.addAttribute("date", date);
        model.addAttribute("hourlyForecast", hourlyForecastForDay); // hourlyForecast is now a List

        return "daily_hourly_detail";
    }

    // Helper method to filter hourly data for a specific date and convert to List<HourlyForecast>
    private List<HourlyForecast> filterHourlyForecastByDate(ComprehensiveWeatherReport.HourlyData fullHourlyData, LocalDate targetDate) {
        List<HourlyForecast> filteredForecasts = new ArrayList<>();

        if (fullHourlyData == null || fullHourlyData.getTime() == null || fullHourlyData.getTime().isEmpty()) {
            return filteredForecasts;
        }

        for (int i = 0; i < fullHourlyData.getTime().size(); i++) {
            String timestampString = fullHourlyData.getTime().get(i);
            // Log the timestamp string for easier debugging
            // Parsing timestamp: " + timestampString); // Removed for less verbose logging

            // Define the formatter to handle the expected format "yyyy-MM-dd'T'HH:mm"
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

            // Parse as LocalDateTime
            java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(timestampString, formatter);
            LocalDate entryDate = localDateTime.toLocalDate();
            if (entryDate.isEqual(targetDate)) {
                // Ensure indices are valid before accessing
                if (i < fullHourlyData.getTemperature2m().size() &&
                    i < fullHourlyData.getWeatherCode().size() &&
                    i < fullHourlyData.getPrecipitationProbability().size() &&
                    i < fullHourlyData.getWindSpeed10m().size()) {

                    HourlyForecast hourlyForecast = new HourlyForecast();
                    hourlyForecast.setTime(localDateTime.toLocalTime());
                    hourlyForecast.setTemperature(fullHourlyData.getTemperature2m().get(i));
                    hourlyForecast.setWeatherCode(fullHourlyData.getWeatherCode().get(i));
                    hourlyForecast.setPrecipitationProbability(fullHourlyData.getPrecipitationProbability().get(i));
                    hourlyForecast.setWindSpeed(fullHourlyData.getWindSpeed10m().get(i));
                    filteredForecasts.add(hourlyForecast);
                } else {
                    logger.warn("Inconsistent hourly data at index {} for date {}. Sizes: time={}, temp={}, code={}, precip={}, wind={}",
                            i, targetDate, fullHourlyData.getTime().size(), fullHourlyData.getTemperature2m().size(),
                            fullHourlyData.getWeatherCode().size(), fullHourlyData.getPrecipitationProbability().size(),
                            fullHourlyData.getWindSpeed10m().size());
                }
            }
        }
        return filteredForecasts;
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

