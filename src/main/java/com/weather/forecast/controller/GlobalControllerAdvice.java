package com.weather.forecast.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("cacheBuster")
    public String addCacheBuster() {
        return String.valueOf(System.currentTimeMillis());
    }
}
