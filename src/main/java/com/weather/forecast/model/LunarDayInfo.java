package com.weather.forecast.model;

import lombok.Data;
import java.util.List;

@Data
public class LunarDayInfo {
    private int solarDay;
    private int solarMonth;
    private int solarYear;

    private int lunarDay;
    private int lunarMonth;
    private int lunarYear;

    private String lunarDayName;
    private String lunarMonthName;
    private String lunarYearName;
    
    private List<String> hoangdao;
}
