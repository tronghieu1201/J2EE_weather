package com.weather.forecast.util;

import java.util.Map;

public class WeatherIconMapper {

    private static final Map<Integer, String> a;

    static {
        a = Map.ofEntries(
            Map.entry(0, "bi-brightness-high-fill"), // Clear Sky (Day)
            Map.entry(1, "bi-cloud-sun-fill"),       // Mainly Clear
            Map.entry(2, "bi-cloudy-fill"),          // Partly Cloudy
            Map.entry(3, "bi-clouds-fill"),          // Overcast
            Map.entry(45, "bi-cloud-fog2-fill"),     // Fog
            Map.entry(48, "bi-cloud-fog2-fill"),     // Depositing Rime Fog
            Map.entry(51, "bi-cloud-drizzle-fill"),  // Drizzle Light
            Map.entry(53, "bi-cloud-drizzle-fill"),  // Drizzle Moderate
            Map.entry(55, "bi-cloud-drizzle-fill"),  // Drizzle Dense
            Map.entry(56, "bi-cloud-drizzle-fill"),  // Freezing Drizzle
            Map.entry(57, "bi-cloud-drizzle-fill"),
            Map.entry(61, "bi-cloud-rain-fill"),     // Rain Slight
            Map.entry(63, "bi-cloud-rain-heavy-fill"),// Rain Moderate
            Map.entry(65, "bi-cloud-rain-heavy-fill"),// Rain Heavy
            Map.entry(66, "bi-cloud-sleet-fill"),    // Freezing Rain
            Map.entry(67, "bi-cloud-sleet-fill"),
            Map.entry(71, "bi-cloud-snow-fill"),     // Snow Slight
            Map.entry(73, "bi-cloud-snow-fill"),
            Map.entry(75, "bi-cloud-snow-fill"),
            Map.entry(77, "bi-snow"),                 // Snow Grains
            Map.entry(80, "bi-cloud-showers-fill"),  // Rain Showers Slight
            Map.entry(81, "bi-cloud-showers-heavy-fill"),
            Map.entry(82, "bi-cloud-showers-heavy-fill"),
            Map.entry(85, "bi-cloud-snow-fill"),
            Map.entry(86, "bi-cloud-snow-fill"),
            Map.entry(95, "bi-cloud-lightning-rain-fill"), // Thunderstorm
            Map.entry(96, "bi-cloud-lightning-rain-fill"),
            Map.entry(99, "bi-cloud-lightning-rain-fill")
        );
    }
    public static String getIconClass(int code) {
        return a.getOrDefault(code, "bi-question-circle-fill");
    }
}
