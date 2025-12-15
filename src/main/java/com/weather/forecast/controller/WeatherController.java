package com.weather.forecast.controller;

import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.HourlyForecast;
import com.weather.forecast.model.LunarDayInfo;
import com.weather.forecast.model.dto.ComprehensiveWeatherReport;
import com.weather.forecast.model.dto.ProvinceCurrentWeather; // Import the new DTO
import com.weather.forecast.service.WeatherService;
import com.weather.forecast.util.LunarConverterUtil;
import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
        this.groupedCities = initGroupedCities();
        this.allProvinces = new ArrayList<>();
        this.groupedCities.values().forEach(allProvinces::addAll);
        this.prominentProvinces = List.of( // Initialize prominentProvinces
                "Hồ Chí Minh", "Bình Định", "Ninh Thuận", "An Giang", "Kiên Giang", "Đà Nẵng",
                "Bình Thuận", "Khánh Hòa", "Cần Thơ", "Lâm Đồng", "Quảng Ninh", "Lào Cai");
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "city", required = false) String city, Model model) {
        model.addAttribute("groupedCities", groupedCities);
        if (city == null || city.isEmpty()) {
            city = "Hà Nội";
        }
        logger.info("Searching for city: {}", city);

        ComprehensiveWeatherReport comprehensiveReport = weatherService.getWeatherReport(city);
        List<DailyForecast> sevenDayForecast = weatherService.get7DayForecast(city);
        List<ProvinceCurrentWeather> prominentProvincesWeather = weatherService
                .getCurrentWeatherForProminentProvinces(prominentProvinces); // Get prominent provinces weather

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
        List<DailyForecast> sevenDayForecast = weatherService.get7DayForecast(city);
        List<ProvinceCurrentWeather> prominentProvincesWeather = weatherService
                .getCurrentWeatherForProminentProvinces(prominentProvinces);

        model.addAttribute("comprehensiveReport", comprehensiveReport);
        model.addAttribute("sevenDayForecast", sevenDayForecast);
        model.addAttribute("city", city);
        model.addAttribute("prominentProvincesWeather", prominentProvincesWeather);
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
    public String showPerpetualCalendar(@RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            Model model) {

        LocalDate today = LocalDate.now();
        int currentMonth = (month != null) ? month : today.getMonthValue();
        int currentYear = (year != null) ? year : today.getYear();

        List<LunarDayInfo> calendarDays = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Tính ngày đầu tháng là thứ mấy (để vẽ lịch)
        int firstDayOfMonth = yearMonth.atDay(1).getDayOfWeek().getValue() % 7;
        // Lưu ý: Java trả về 7 là CN, nếu bạn muốn CN là 0 thì dùng logic % 7 như trên
        // là đúng

        for (int day = 1; day <= daysInMonth; day++) {
            // --- SỬA ĐOẠN NÀY: Dùng thư viện Lunar-Java ---
            Solar solar = new Solar(currentYear, currentMonth, day);
            Lunar lunar = solar.getLunar();

            LunarDayInfo dayInfo = new LunarDayInfo();

            // Set thông tin Dương lịch
            dayInfo.setSolarDay(day);
            dayInfo.setSolarMonth(currentMonth);
            dayInfo.setSolarYear(currentYear);

            // Set thông tin Âm lịch (Dùng thư viện mới)
            dayInfo.setLunarDay(lunar.getDay());
            dayInfo.setLunarMonth(lunar.getMonth());
            dayInfo.setLunarYear(lunar.getYear());

            // Set tên chữ (Ví dụ: Mùng Một, Tháng Giêng...)
            dayInfo.setLunarDayName(LunarConverterUtil.getVietnameseLunarDay(lunar.getDayInChinese()));
            dayInfo.setLunarMonthName(LunarConverterUtil.getVietnameseLunarMonth(lunar.getMonthInChinese()));
            dayInfo.setLunarYearName(LunarConverterUtil.getVietnameseYearName(lunar.getYearGan(), lunar.getYearZhi())); // Ví
                                                                                                                        // dụ:
                                                                                                                        // Năm
                                                                                                                        // Giáp
                                                                                                                        // Tý

            // Set Giờ Hoàng Đạo
            // Thư viện này trả về danh sách giờ hoàng đạo dạng object, ta cần lấy tên chi
            // giờ ra
            List<String> hoangDaoList = new ArrayList<>();
            // Lấy danh sách giờ hoàng đạo trong ngày
            for (com.nlf.calendar.LunarTime time : lunar.getTimes()) {
                // Logic: Nếu là giờ cát (hoàng đạo) thì thêm vào list
                if ("吉".equals(time.getTianShenLuck())) { // Chữ Hán "Cát"
                    // time.getGanZhi() trả về Can Chi giờ (ví dụ Giáp Tý)
                    // Chúng ta chỉ cần lấy Chi (Tý, Sửu...)
                    hoangDaoList.add(time.getZhi());
                }
            }
            // Nếu bạn muốn hiển thị tiếng Việt rõ ràng, cần 1 hàm map từ Hán -> Việt
            // Nhưng tạm thời để code chạy được, ta lưu list này:
            dayInfo.setHoangdao(LunarConverterUtil.getVietnameseHoangDao(hoangDaoList));

            calendarDays.add(dayInfo);
        }

        LocalDate prevMonthDate = yearMonth.minusMonths(1).atDay(1);
        LocalDate nextMonthDate = yearMonth.plusMonths(1).atDay(1);

        model.addAttribute("groupedCities", groupedCities);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("year", currentYear);
        model.addAttribute("month", currentMonth);
        model.addAttribute("firstDayOfMonth", firstDayOfMonth);
        model.addAttribute("prevYear", prevMonthDate.getYear());
        model.addAttribute("prevMonth", prevMonthDate.getMonthValue());
        model.addAttribute("nextYear", nextMonthDate.getYear());
        model.addAttribute("nextMonth", nextMonthDate.getMonthValue());

        return "perpetual_calendar";
    }

    @PostMapping("/perpetual-calendar/convert-solar-to-lunar")
    public String convertSolarToLunar(@RequestParam("solar_day") int day,
            @RequestParam("solar_month") int month,
            @RequestParam("solar_year") int year,
            RedirectAttributes redirectAttributes) {
        try {
            Solar solar = new Solar(year, month, day);
            Lunar lunar = solar.getLunar();
            String result = String.format("Ngày %d/%d/%d Dương lịch là ngày %d/%d/%d Âm lịch (%s)",
                    day, month, year, lunar.getDay(), lunar.getMonth(), lunar.getYear(),
                    LunarConverterUtil.getVietnameseYearName(lunar.getYearGan(), lunar.getYearZhi()));
            redirectAttributes.addFlashAttribute("solarToLunarResult", result);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("solarToLunarResult", "Ngày không hợp lệ.");
        }
        return "redirect:/perpetual-calendar";
    }

    @PostMapping("/perpetual-calendar/convert-lunar-to-solar")
    public String convertLunarToSolar(@RequestParam("lunar_day") int day,
            @RequestParam("lunar_month") int month,
            @RequestParam("lunar_year") int year,
            RedirectAttributes redirectAttributes) {
        try {
            // Note: The lunar-java library might expect a boolean for isLeapMonth if
            // handling leap months is needed.
            // For simplicity, we are not handling leap months here.
            Lunar lunar = new Lunar(year, month, day);
            Solar solar = lunar.getSolar();
            String result = String.format("Ngày %d/%d/%d Âm lịch là ngày %d/%d/%d Dương lịch",
                    day, month, year, solar.getDay(), solar.getMonth(), solar.getYear());
            redirectAttributes.addFlashAttribute("lunarToSolarResult", result);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("lunarToSolarResult", "Ngày không hợp lệ.");
        }
        return "redirect:/perpetual-calendar";
    }

    @GetMapping("/weather-news")
    public String showWeatherNews() {
        return "weather_news";
    }

    @GetMapping("/weather-news/acid-rain")
    public String showAcidRainArticle() {
        return "acid_rain";
    }

    @GetMapping("/weather-news/dead-sea")
    public String showDeadSeaArticle() {
        return "dead_sea";
    }

    @GetMapping("/weather-news/tropical-storm")
    public String showTropicalStormArticle() {
        return "tropical_storm";
    }

    @GetMapping("/weather-news/westerly-winds")
    public String showWesterlyWindsArticle() {
        return "westerly_winds";
    }

    @GetMapping("/weather-news/white-sea")
    public String showWhiteSeaArticle() {
        return "white_sea";
    }

    @GetMapping("/weather-news/earthquake")
    public String showEarthquakeArticle() {
        return "earthquake";
    }

    @GetMapping("/weather-news/temperature-amplitude")
    public String showTemperatureAmplitudeArticle() {
        return "temperature_amplitude";
    }

    @GetMapping("/weather-news/northern-seasons")
    public String showNorthernSeasonsArticle() {
        return "northern_seasons";
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
        List<ProvinceCurrentWeather> prominentProvincesWeather = weatherService
                .getCurrentWeatherForProminentProvinces(prominentProvinces);
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

    private List<HourlyForecast> filterHourlyForecastByDate(ComprehensiveWeatherReport.HourlyData fullHourlyData,
            LocalDate targetDate) {
        List<HourlyForecast> filteredForecasts = new ArrayList<>();

        if (fullHourlyData == null || fullHourlyData.getTime() == null || fullHourlyData.getTime().isEmpty()) {
            return filteredForecasts;
        }

        for (int i = 0; i < fullHourlyData.getTime().size(); i++) {
            String timestampString = fullHourlyData.getTime().get(i);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm");
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
        cities.put("Đông Bắc Bộ", List.of("Hà Giang", "Cao Bằng", "Bắc Kạn", "Lạng Sơn", "Tuyên Quang", "Thái Nguyên",
                "Phú Thọ", "Bắc Giang", "Quảng Ninh"));
        cities.put("Tây Bắc Bộ", List.of("Hòa Bình", "Sơn La", "Điện Biên", "Lai Châu", "Lào Cai", "Yên Bái"));
        cities.put("Đồng Bằng Sông Hồng", List.of("Bắc Ninh", "Hà Nam", "Hà Nội", "Hải Dương", "Hải Phòng", "Hưng Yên",
                "Nam Định", "Ninh Bình", "Thái Bình", "Vĩnh Phúc"));
        cities.put("Bắc Trung Bộ",
                List.of("Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Bình", "Quảng Trị", "Thừa Thiên Huế"));
        cities.put("Nam Trung Bộ", List.of("Đà Nẵng", "Quảng Nam", "Quảng Ngãi", "Bình Định", "Phú Yên", "Khánh Hòa",
                "Ninh Thuận", "Bình Thuận"));
        cities.put("Tây Nguyên", List.of("Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng"));
        cities.put("Đông Nam Bộ",
                List.of("Bình Phước", "Bình Dương", "Đồng Nai", "Tây Ninh", "Bà Rịa - Vũng Tàu", "Hồ Chí Minh"));
        cities.put("Đồng Bằng Sông Cửu Long", List.of("An Giang", "Bến Tre", "Bạc Liêu", "Cà Mau", "Cần Thơ",
                "Đồng Tháp", "Hậu Giang", "Kiên Giang", "Long An", "Sóc Trăng", "Tiền Giang", "Trà Vinh", "Vĩnh Long"));
        return cities;
    }
}
