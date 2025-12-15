package com.weather.forecast.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.HourlyForecast;
import com.weather.forecast.model.dto.ComprehensiveWeatherReport;
import com.weather.forecast.model.dto.ProvinceCurrentWeather; // Import the new DTO
import com.weather.forecast.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;
    private final Map<String, List<String>> groupedCities;
    private final List<String> allProvinces;
    private final List<String> prominentProvinces; // Declare prominentProvinces
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public WeatherController(WeatherService weatherService, ObjectMapper objectMapper) {
        this.weatherService = weatherService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.groupedCities = initGroupedCities();
        this.allProvinces = new ArrayList<>();
        this.groupedCities.values().forEach(allProvinces::addAll);
        this.prominentProvinces = List.of( // Initialize prominentProvinces
                "Hồ Chí Minh", "Bình Định", "Ninh Thuận", "An Giang", "Kiên Giang", "Đà Nẵng",
                "Bình Thuận", "Khánh Hòa", "Cần Thơ", "Lâm Đồng", "Quảng Ninh", "Lào Cai"
        );
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "city", required = false) String city, Model model) {
        model.addAttribute("groupedCities", groupedCities);
        if (city == null || city.isEmpty()) {
            city = "Hà Nội";
        }
        logger.info("Searching for city: {}", city);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<DailyForecast> sevenDayForecast = weatherService.get7DayForecastFromReport(comprehensiveReport);
        List<ProvinceCurrentWeather> prominentProvincesWeather = weatherService.getCurrentWeatherForProminentProvinces(prominentProvinces); // Get prominent provinces weather

        model.addAttribute("comprehensiveReport", comprehensiveReport);
        model.addAttribute("sevenDayForecast", sevenDayForecast);
        model.addAttribute("city", city);
        model.addAttribute("prominentProvincesWeather", prominentProvincesWeather); // Add to model
        return "index";
    }

    @PostMapping("/")
    public String postWeatherForecast(@RequestParam("city") String city, Model model) {
        model.addAttribute("groupedCities", groupedCities);
        logger.info("User searching for city (POST): {}", city);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<DailyForecast> sevenDayForecast = weatherService.get7DayForecastFromReport(comprehensiveReport);

        model.addAttribute("comprehensiveReport", comprehensiveReport);
        model.addAttribute("sevenDayForecast", sevenDayForecast);
        model.addAttribute("city", city);
        return "index";
    }

    @GetMapping("/provinces")
    public String showProvinces(Model model) {
        model.addAttribute("provinces", allProvinces);
        return "provinces";
    }

    @GetMapping("/chart")
    public String showChart(@RequestParam(name = "city", required = false) String city, Model model) {
        if (city == null || city.isEmpty()) {
            city = "Hà Nội"; // Default city
        }
        model.addAttribute("city", city);
        model.addAttribute("groupedCities", groupedCities); // For the navbar dropdown
        return "chart";
    }

    @GetMapping("/api/weather-forecast")
    @ResponseBody
    public Map<String, Object> getWeatherDataForChart(@RequestParam("city") String city) {
        logger.info("API request for chart data for city: {}", city);
        ComprehensiveWeatherReport report = weatherService.getWeatherReport(city);
        List<DailyForecast> sevenDayForecast = weatherService.get7DayForecastFromReport(report);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("sevenDayForecast", sevenDayForecast);
        
        return response;
    }
    @GetMapping("/perpetual-calendar")
    public String showPerpetualCalendar() {
        return "perpetual_calendar";
    }

    @PostMapping("/api/lunar-month-dates")
    @ResponseBody
    public List<Map<String, Object>> getLunarMonthDates(@RequestBody Map<String, Integer> payload) {
        int month = payload.get("month");
        int year = payload.get("year");
        logger.info("Fetching lunar dates for month: {}/{}", month, year);
        List<Map<String, Object>> lunarDates = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            try {
                // Add a delay to avoid rate limiting
                Thread.sleep(200); 

                Map<String, Integer> dateMap = Map.of("day", day, "month", month, "year", year);
                String requestBody = objectMapper.writeValueAsString(dateMap);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://open.oapi.vn/date/convert-to-lunar"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Map<String, Object> apiResponse = objectMapper.readValue(response.body(), Map.class);
                    Map<String, Object> lunarData = (Map<String, Object>) apiResponse.get("data");
                    if (lunarData != null) {
                        lunarDates.add(lunarData);
                    } else {
                        lunarDates.add(Collections.singletonMap("error", "No data for day " + day));
                    }
                } else {
                    logger.error("Lunar API request for {}/{}/{} failed with status: {}", day, month, year, response.statusCode());
                    lunarDates.add(Collections.singletonMap("error", "API failed for day " + day + " status: " + response.statusCode()));
                }
            } catch (Exception e) {
                logger.error("Error calling lunar API for {}/{}/{}: {}", day, month, year, e.getMessage());
                // If the loop is interrupted (e.g., by Thread.sleep), handle it.
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                lunarDates.add(Collections.singletonMap("error", "Exception for day " + day + ": " + e.getMessage()));
            }
        }
        return lunarDates;
    }

    @GetMapping("/weather-news")
    public String showWeatherNews() {
        return "weather_news";
    }

    @GetMapping("/get-weather-fragment")
    public String getWeatherFragment(@RequestParam(name = "city", required = false) String city, Model model) {
        if (city == null || city.isEmpty()) {
            city = "Hà Nội";
        }
        logger.info("Fetching weather fragment for city: {}", city);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<DailyForecast> sevenDayForecast = weatherService.get7DayForecastFromReport(comprehensiveReport);

        model.addAttribute("comprehensiveReport", comprehensiveReport);
        model.addAttribute("sevenDayForecast", sevenDayForecast);
        model.addAttribute("city", city);
        // Ensure prominentProvincesWeather is also added to the fragment's model
        List<ProvinceCurrentWeather> prominentProvincesWeather = weatherService.getCurrentWeatherForProminentProvinces(prominentProvinces);
        model.addAttribute("prominentProvincesWeather", prominentProvincesWeather);

        return "index :: weatherContent";
    }

    @GetMapping("/hourly-details")
    public String showHourlyDetails(
            @RequestParam("city") String city,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        logger.info("Fetching hourly details for city: {} on date: {}", city, date);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<HourlyForecast> hourlyForecastForDay = new ArrayList<>();

        if (comprehensiveReport != null && comprehensiveReport.getHourly() != null) {
            hourlyForecastForDay = filterHourlyForecastByDate(comprehensiveReport.getHourly(), date);
        }

        model.addAttribute("city", city);
        model.addAttribute("date", date);
        model.addAttribute("hourlyForecast", hourlyForecastForDay);

        return "daily_hourly_detail";
    }

    private List<HourlyForecast> filterHourlyForecastByDate(ComprehensiveWeatherReport.HourlyData fullHourlyData, LocalDate targetDate) {
        List<HourlyForecast> filteredForecasts = new ArrayList<>();

        if (fullHourlyData == null || fullHourlyData.getTime() == null || fullHourlyData.getTime().isEmpty()) {
            return filteredForecasts;
        }

        for (int i = 0; i < fullHourlyData.getTime().size(); i++) {
            String timestampString = fullHourlyData.getTime().get(i);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(timestampString, formatter);
            LocalDate entryDate = localDateTime.toLocalDate();

            if (entryDate.isEqual(targetDate)) {
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
                    logger.warn("Inconsistent hourly data at index {}", i);
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

