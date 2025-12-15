package com.weather.forecast.util;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

public class LunarConverterUtil {

    private static final Map<String, String> LUNAR_MONTH_MAP = Map.ofEntries(
            Map.entry("正", "Giêng"),
            Map.entry("二", "Hai"),
            Map.entry("三", "Ba"),
            Map.entry("四", "Tư"),
            Map.entry("五", "Năm"),
            Map.entry("六", "Sáu"),
            Map.entry("七", "Bảy"),
            Map.entry("八", "Tám"),
            Map.entry("九", "Chín"),
            Map.entry("十", "Mười"),
            Map.entry("冬", "Mười Một"),
            Map.entry("腊", "Chạp")
    );

    private static final Map<String, String> LUNAR_DAY_MAP = Map.ofEntries(
        Map.entry("初一", "Mùng 1"), Map.entry("初二", "Mùng 2"), Map.entry("初三", "Mùng 3"),
        Map.entry("初四", "Mùng 4"), Map.entry("初五", "Mùng 5"), Map.entry("初六", "Mùng 6"),
        Map.entry("初七", "Mùng 7"), Map.entry("初八", "Mùng 8"), Map.entry("初九", "Mùng 9"),
        Map.entry("初十", "Mùng 10"), Map.entry("十一", "11"), Map.entry("十二", "12"),
        Map.entry("十三", "13"), Map.entry("十四", "14"), Map.entry("十五", "15"),
        Map.entry("十六", "16"), Map.entry("十七", "17"), Map.entry("十八", "18"),
        Map.entry("十九", "19"), Map.entry("二十", "20"), Map.entry("廿一", "21"),
        Map.entry("廿二", "22"), Map.entry("廿三", "23"), Map.entry("廿四", "24"),
        Map.entry("廿五", "25"), Map.entry("廿六", "26"), Map.entry("廿七", "27"),
        Map.entry("廿八", "28"), Map.entry("廿九", "29"), Map.entry("三十", "30")
    );

    private static final Map<String, String> GAN_MAP = Map.ofEntries(
            Map.entry("甲", "Giáp"), Map.entry("乙", "Ất"), Map.entry("丙", "Bính"),
            Map.entry("丁", "Đinh"), Map.entry("戊", "Mậu"), Map.entry("己", "Kỷ"),
            Map.entry("庚", "Canh"), Map.entry("辛", "Tân"), Map.entry("壬", "Nhâm"),
            Map.entry("癸", "Quý")
    );

    private static final Map<String, String> ZHI_MAP = Map.ofEntries(
            Map.entry("子", "Tý"), Map.entry("丑", "Sửu"), Map.entry("寅", "Dần"),
            Map.entry("卯", "Mão"), Map.entry("辰", "Thìn"), Map.entry("巳", "Tỵ"),
            Map.entry("午", "Ngọ"), Map.entry("未", "Mùi"), Map.entry("申", "Thân"),
            Map.entry("酉", "Dậu"), Map.entry("戌", "Tuất"), Map.entry("亥", "Hợi")
    );

    public static String getVietnameseLunarMonth(String chineseMonth) {
        return "Tháng " + LUNAR_MONTH_MAP.getOrDefault(chineseMonth, chineseMonth);
    }

    public static String getVietnameseLunarDay(String chineseDay) {
        return LUNAR_DAY_MAP.getOrDefault(chineseDay, chineseDay);
    }
    
    public static String getVietnameseYearName(String gan, String zhi) {
        String vietnameseGan = GAN_MAP.getOrDefault(gan, gan);
        String vietnameseZhi = ZHI_MAP.getOrDefault(zhi, zhi);
        return "Năm " + vietnameseGan + " " + vietnameseZhi;
    }
    
    public static List<String> getVietnameseHoangDao(List<String> chineseZhiList) {
        return chineseZhiList.stream()
                .map(zhi -> ZHI_MAP.getOrDefault(zhi, zhi))
                .collect(Collectors.toList());
    }
}
