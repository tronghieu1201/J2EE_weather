package com.weather.forecast.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.weather.forecast.ai.ForecastModel; // Added
import org.springframework.beans.factory.annotation.Qualifier; // Added
import org.springframework.beans.factory.annotation.Value; // Added
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("vi"));
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    @Qualifier("dailyMaxTempForecastModel")
    public ForecastModel dailyMaxTempForecastModel(@Value("${models.daily.max_temp.path}") String modelPath) {
        return new ForecastModel(modelPath);
    }

    @Bean
    @Qualifier("dailyMinTempForecastModel")
    public ForecastModel dailyMinTempForecastModel(@Value("${models.daily.min_temp.path}") String modelPath) {
        return new ForecastModel(modelPath);
    }

    @Bean
    @Qualifier("dailyRainProbForecastModel")
    public ForecastModel dailyRainProbForecastModel(@Value("${models.daily.rain_prob.path}") String modelPath) {
        return new ForecastModel(modelPath);
    }

    @Bean
    @Qualifier("hourlyForecastModel")
    public ForecastModel hourlyForecastModel(@Value("${models.hourly.path}") String modelPath) {
        return new ForecastModel(modelPath);
    }
}
