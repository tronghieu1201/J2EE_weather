package com.weather.forecast.util;

import java.util.Map;

public class WeatherCodeMapper {

    private static final Map<Integer, String> CODE_TO_DESCRIPTION_MAP;

    // Static block to initialize the map
    static {
        CODE_TO_DESCRIPTION_MAP = Map.ofEntries(
            Map.entry(0, "Trời quang"),
            Map.entry(1, "Trời quang"),
            Map.entry(2, "Ít mây"),
            Map.entry(3, "Nhiều mây"),
            Map.entry(45, "Sương mù"),
            Map.entry(48, "Sương mù"),
            Map.entry(51, "Mưa phùn nhẹ"),
            Map.entry(53, "Mưa phùn"),
            Map.entry(55, "Mưa phùn dày"),
            Map.entry(56, "Mưa phùn đông"),
            Map.entry(57, "Mưa phùn đông dày"),
            Map.entry(61, "Mưa nhỏ"),
            Map.entry(63, "Mưa vừa"),
            Map.entry(65, "Mưa to"),
            Map.entry(66, "Mưa đông nhẹ"),
            Map.entry(67, "Mưa đông to"),
            Map.entry(71, "Tuyết rơi nhẹ"),
            Map.entry(73, "Tuyết rơi vừa"),
            Map.entry(75, "Tuyết rơi dày"),
            Map.entry(77, "Hạt tuyết"),
            Map.entry(80, "Mưa rào nhẹ"),
            Map.entry(81, "Mưa rào vừa"),
            Map.entry(82, "Mưa rào to"),
            Map.entry(85, "Tuyết rào nhẹ"),
            Map.entry(86, "Tuyết rào to"),
            Map.entry(95, "Dông"),
            Map.entry(96, "Dông có mưa đá"),
            Map.entry(99, "Dông có mưa đá to")
        );
    }
    public static String getDescription(int code) {
        return CODE_TO_DESCRIPTION_MAP.getOrDefault(code, "Không xác định");
    }

    /**
     * Maps a rain probability (0-1) to a simplified WMO weather code for icon display.
     * This is a heuristic mapping for predicted rain probability.
     * @param rainProbability The predicted rain probability (0.0 to 1.0).
     * @return A WMO weather code.
     */
    public static int mapRainProbToWeatherCode(double rainProbability) {
        if (rainProbability > 0.6) {
            return 65; // Heavy rain
        } else if (rainProbability > 0.3) {
            return 61; // Light rain
        } else if (rainProbability > 0.1) {
            return 2;  // Partly cloudy (chance of some precipitation)
        } else {
            return 0;  // Clear sky
        }
    }
}
