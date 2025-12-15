package com.weather.forecast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.api.OpenMeteoAPI;
import com.weather.forecast.model.WeatherHistory;
import com.weather.forecast.repository.WeatherHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Service để cập nhật dữ liệu thời tiết lịch sử cho việc huấn luyện XGBoost.
 */
@Service
public class DataUpdateService {

    private final OpenMeteoAPI openMeteoAPI;
    private final WeatherHistoryRepository weatherHistoryRepository;
    private final ObjectMapper objectMapper;

    // Số ngày lịch sử cần thu thập
    private static final int HISTORICAL_DAYS = 30;

    // Danh sách 63 tỉnh thành Việt Nam
    private static final List<String> ALL_PROVINCES = Arrays.asList(
            "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
            "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
            "Bình Thuận", "Cà Mau", "Cần Thơ", "Cao Bằng", "Đà Nẵng",
            "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp",
            "Gia Lai", "Hà Giang", "Hà Nam", "Hà Nội", "Hà Tĩnh",
            "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình", "Hưng Yên",
            "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng",
            "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An",
            "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình",
            "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng",
            "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa",
            "Thừa Thiên Huế", "Tiền Giang", "Hồ Chí Minh", "Trà Vinh", "Tuyên Quang",
            "Vĩnh Long", "Vĩnh Phúc", "Yên Bái");

    @Autowired
    public DataUpdateService(OpenMeteoAPI openMeteoAPI,
            WeatherHistoryRepository weatherHistoryRepository,
            ObjectMapper objectMapper) {
        this.openMeteoAPI = openMeteoAPI;
        this.weatherHistoryRepository = weatherHistoryRepository;
        this.objectMapper = objectMapper;
    }

    public List<String> getAllProvinces() {
        return ALL_PROVINCES;
    }

    /**
     * Thu thập dữ liệu thời tiết LỊCH SỬ (30 ngày) cho một tỉnh từ Open-Meteo
     * Archive API.
     */
    @Transactional
    public int collectHistoricalWeather(String province) throws IOException, InterruptedException {
        System.out.println("Collecting 30-day historical data for: " + province);

        // 1. Lấy tọa độ
        String geoJson = openMeteoAPI.getCoordinatesForCity(province);
        JsonNode geoResults = objectMapper.readTree(geoJson).path("results");
        if (!geoResults.isArray() || geoResults.size() == 0) {
            throw new IOException("Could not find coordinates for province: " + province);
        }

        double lat = geoResults.get(0).path("latitude").asDouble();
        double lon = geoResults.get(0).path("longitude").asDouble();

        // 2. Tính khoảng thời gian (30 ngày trước đến hôm qua - Archive API không có dữ
        // liệu hôm nay)
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(HISTORICAL_DAYS - 1);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String startStr = startDate.format(formatter);
        String endStr = endDate.format(formatter);

        System.out.println("  Fetching data from " + startStr + " to " + endStr);

        // 3. Gọi Archive API
        String historicalJson = openMeteoAPI.getHistoricalWeather(lat, lon, startStr, endStr);
        JsonNode root = objectMapper.readTree(historicalJson);
        JsonNode dailyData = root.path("daily");

        if (!dailyData.has("time") || !dailyData.path("time").isArray()) {
            throw new IOException("Invalid historical data format for " + province);
        }

        // 4. Parse và lưu từng ngày
        JsonNode times = dailyData.path("time");
        JsonNode tempMaxArr = dailyData.path("temperature_2m_max");
        JsonNode tempMinArr = dailyData.path("temperature_2m_min");
        JsonNode precipArr = dailyData.path("precipitation_sum");
        JsonNode weatherCodeArr = dailyData.path("weather_code");
        JsonNode windSpeedArr = dailyData.path("wind_speed_10m_max");

        int savedCount = 0;

        for (int i = 0; i < times.size(); i++) {
            LocalDate recordDate = LocalDate.parse(times.get(i).asText());

            // Kiểm tra xem đã có dữ liệu chưa
            if (weatherHistoryRepository.existsByProvinceAndRecordDate(province, recordDate)) {
                continue; // Skip nếu đã có
            }

            WeatherHistory history = new WeatherHistory();
            history.setProvince(province);
            history.setLatitude(lat);
            history.setLongitude(lon);
            history.setRecordDate(recordDate);
            history.setRecordTime(LocalTime.NOON); // Dữ liệu daily, set giờ trưa

            // Set values từ API
            if (tempMaxArr.has(i) && !tempMaxArr.get(i).isNull()) {
                history.setTempMax(tempMaxArr.get(i).asDouble());
            }
            if (tempMinArr.has(i) && !tempMinArr.get(i).isNull()) {
                history.setTempMin(tempMinArr.get(i).asDouble());
            }
            if (precipArr.has(i) && !precipArr.get(i).isNull()) {
                history.setPrecipitation(precipArr.get(i).asDouble());
                // Tính xác suất mưa từ lượng mưa (đơn giản: có mưa > 0.1mm = 100%, không = 0%)
                double precip = precipArr.get(i).asDouble();
                history.setPrecipitationProbability(precip > 0.1 ? 1.0 : 0.0);
            }
            if (weatherCodeArr.has(i) && !weatherCodeArr.get(i).isNull()) {
                history.setWeatherCode(weatherCodeArr.get(i).asInt());
            }
            if (windSpeedArr.has(i) && !windSpeedArr.get(i).isNull()) {
                history.setWindSpeed(windSpeedArr.get(i).asDouble());
            }

            // Tính nhiệt độ hiện tại (trung bình của max và min)
            if (history.getTempMax() != null && history.getTempMin() != null) {
                history.setTempCurrent((history.getTempMax() + history.getTempMin()) / 2);
            }

            history.setRecordedAt(LocalDateTime.now());

            weatherHistoryRepository.save(history);
            savedCount++;
        }

        System.out.println("  Saved " + savedCount + " new records for " + province);
        return savedCount;
    }

    /**
     * Thu thập dữ liệu thời tiết HIỆN TẠI (ngày hôm nay) cho một tỉnh.
     */
    @Transactional
    public void collectTodayWeather(String province) throws IOException, InterruptedException {
        System.out.println("Collecting today's data for: " + province);

        // 1. Lấy tọa độ
        String geoJson = openMeteoAPI.getCoordinatesForCity(province);
        JsonNode geoResults = objectMapper.readTree(geoJson).path("results");
        if (!geoResults.isArray() || geoResults.size() == 0) {
            throw new IOException("Could not find coordinates for province: " + province);
        }

        double lat = geoResults.get(0).path("latitude").asDouble();
        double lon = geoResults.get(0).path("longitude").asDouble();

        // 2. Lấy dữ liệu forecast (có dữ liệu hôm nay)
        String weatherJson = openMeteoAPI.getWeatherForecast(lat, lon);
        JsonNode root = objectMapper.readTree(weatherJson);
        JsonNode currentWeather = root.path("current");
        JsonNode dailyData = root.path("daily");

        LocalDate today = LocalDate.now();

        // 3. Kiểm tra xem đã có dữ liệu cho ngày hôm nay chưa
        if (weatherHistoryRepository.existsByProvinceAndRecordDate(province, today)) {
            System.out.println("  Data for " + province + " on " + today + " already exists. Skipping.");
            return;
        }

        // 4. Tạo entity WeatherHistory
        WeatherHistory history = new WeatherHistory();
        history.setProvince(province);
        history.setLatitude(lat);
        history.setLongitude(lon);
        history.setRecordDate(today);
        history.setRecordTime(LocalTime.now());

        // Current weather data
        history.setTempCurrent(currentWeather.path("temperature_2m").asDouble());
        history.setHumidity(currentWeather.path("relative_humidity_2m").asDouble());
        history.setWindSpeed(currentWeather.path("wind_speed_10m").asDouble());
        history.setPressure(currentWeather.path("surface_pressure").asDouble());
        history.setWeatherCode(currentWeather.path("weather_code").asInt());

        // Daily data (today's forecast - index 0)
        if (dailyData.has("temperature_2m_max") && dailyData.path("temperature_2m_max").isArray()) {
            history.setTempMax(dailyData.path("temperature_2m_max").get(0).asDouble());
        }
        if (dailyData.has("temperature_2m_min") && dailyData.path("temperature_2m_min").isArray()) {
            history.setTempMin(dailyData.path("temperature_2m_min").get(0).asDouble());
        }
        if (dailyData.has("precipitation_probability_max")
                && dailyData.path("precipitation_probability_max").isArray()) {
            history.setPrecipitationProbability(
                    dailyData.path("precipitation_probability_max").get(0).asDouble() / 100.0);
        }
        if (dailyData.has("precipitation_sum") && dailyData.path("precipitation_sum").isArray()) {
            history.setPrecipitation(dailyData.path("precipitation_sum").get(0).asDouble());
        }

        history.setRecordedAt(LocalDateTime.now());

        // 5. Lưu vào database
        weatherHistoryRepository.save(history);
        System.out.println("  Saved today's data for " + province);
    }

    /**
     * Thu thập dữ liệu LỊCH SỬ 30 NGÀY cho tất cả 63 tỉnh thành.
     * 
     * @return Tổng số bản ghi đã lưu.
     */
    public int collectAllProvincesHistoricalData() {
        int totalSaved = 0;
        int successCount = 0;
        int failCount = 0;

        System.out.println("\n=== COLLECTING 30-DAY HISTORICAL DATA FOR ALL 63 PROVINCES ===\n");

        for (String province : ALL_PROVINCES) {
            try {
                int saved = collectHistoricalWeather(province);
                totalSaved += saved;
                successCount++;
                // Delay để tránh rate limit
                Thread.sleep(200);
            } catch (Exception e) {
                System.err.println("Failed to collect historical data for " + province + ": " + e.getMessage());
                failCount++;
            }
        }

        System.out.println("\n=== COLLECTION COMPLETE ===");
        System.out.println("Provinces: " + successCount + " success, " + failCount + " failed");
        System.out.println("Total new records saved: " + totalSaved);

        return totalSaved;
    }

    /**
     * Thu thập dữ liệu CHỈ NGÀY HÔM NAY cho tất cả 63 tỉnh thành.
     */
    public int collectAllProvincesTodayData() {
        int successCount = 0;
        int failCount = 0;

        for (String province : ALL_PROVINCES) {
            try {
                collectTodayWeather(province);
                successCount++;
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Failed to collect today's data for " + province + ": " + e.getMessage());
                failCount++;
            }
        }

        System.out.println("Today collection: " + successCount + " success, " + failCount + " failed");
        return successCount;
    }

    /**
     * Lấy tổng số bản ghi trong database.
     */
    public long getTotalRecords() {
        return weatherHistoryRepository.count();
    }

    /**
     * Lấy danh sách tất cả bản ghi để hiển thị.
     */
    public List<WeatherHistory> getAllRecords() {
        return weatherHistoryRepository.findAll();
    }
}
