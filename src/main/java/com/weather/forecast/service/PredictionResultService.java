package com.weather.forecast.service;

import com.weather.forecast.model.DailyForecast;
import com.weather.forecast.model.PredictionResult;
import com.weather.forecast.model.WeatherHistory;
import com.weather.forecast.repository.PredictionResultRepository;
import com.weather.forecast.repository.WeatherHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý và đánh giá kết quả dự đoán XGBoost.
 * - Lưu prediction khi XGBoost chạy
 * - Verify với actual data khi có
 * - Tính accuracy metrics
 */
@Service
public class PredictionResultService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionResultService.class);

    private final PredictionResultRepository predictionResultRepository;
    private final WeatherHistoryRepository weatherHistoryRepository;

    @Autowired
    public PredictionResultService(PredictionResultRepository predictionResultRepository,
            WeatherHistoryRepository weatherHistoryRepository) {
        this.predictionResultRepository = predictionResultRepository;
        this.weatherHistoryRepository = weatherHistoryRepository;
    }

    /**
     * Lưu kết quả dự đoán XGBoost (async để không block)
     */
    @Async
    public void savePrediction(String province, DailyForecast forecast) {
        try {
            // Kiểm tra đã có prediction cho ngày này chưa
            Optional<PredictionResult> existing = predictionResultRepository
                    .findByProvinceAndPredictionDate(province, forecast.getDate());

            if (existing.isPresent()) {
                logger.debug("Prediction đã tồn tại cho {} ngày {}", province, forecast.getDate());
                return;
            }

            PredictionResult result = new PredictionResult();
            result.setProvince(province);
            result.setPredictionDate(forecast.getDate());
            result.setPredictedMaxTemp(forecast.getTempMax());
            result.setPredictedMinTemp(forecast.getTempMin());
            result.setPredictedRainProb(forecast.getRainProbability());
            result.setPredictedWeatherCode(forecast.getWeatherCode());

            predictionResultRepository.save(result);
            logger.info("✓ Đã lưu prediction cho {} ngày {}: max={}°C, min={}°C",
                    province, forecast.getDate(), forecast.getTempMax(), forecast.getTempMin());

        } catch (Exception e) {
            logger.error("Lỗi lưu prediction cho {}: {}", province, e.getMessage());
        }
    }

    /**
     * Lưu nhiều predictions cùng lúc
     */
    @Async
    public void savePredictions(String province, List<DailyForecast> forecasts) {
        for (DailyForecast forecast : forecasts) {
            savePrediction(province, forecast);
        }
    }

    /**
     * Chạy mỗi ngày lúc 23:00 để verify predictions với actual data
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void verifyPendingPredictions() {
        logger.info("Bắt đầu verify predictions...");

        LocalDate today = LocalDate.now();
        List<PredictionResult> pendingResults = predictionResultRepository
                .findByIsVerifiedFalseAndPredictionDateBefore(today);

        int verified = 0;
        for (PredictionResult prediction : pendingResults) {
            try {
                // Lấy actual data từ weather_history
                Optional<WeatherHistory> actualData = weatherHistoryRepository
                        .findByProvinceAndRecordDate(prediction.getProvince(), prediction.getPredictionDate());

                if (actualData.isPresent()) {
                    WeatherHistory actual = actualData.get();
                    prediction.verifyWithActual(
                            actual.getTempMax(),
                            actual.getTempMin(),
                            actual.getPrecipitationProbability(),
                            actual.getWeatherCode());
                    predictionResultRepository.save(prediction);
                    verified++;

                    logger.info("✓ Verified {}/{}: MAE max={}°C, MAE min={}°C",
                            prediction.getProvince(), prediction.getPredictionDate(),
                            prediction.getMaeMaxTemp(), prediction.getMaeMinTemp());
                }
            } catch (Exception e) {
                logger.error("Lỗi verify prediction {}: {}", prediction.getId(), e.getMessage());
            }
        }

        logger.info("Hoàn thành verify: {}/{} predictions", verified, pendingResults.size());
    }

    /**
     * Lấy accuracy tổng thể (MAE trung bình)
     */
    public Double getOverallAccuracy() {
        return predictionResultRepository.getOverallAverageMaeMaxTemp();
    }

    /**
     * Lấy accuracy theo tỉnh
     */
    public Double getAccuracyByProvince(String province) {
        return predictionResultRepository.getAverageMaeMaxTempByProvince(province);
    }

    /**
     * Lấy dự đoán gần nhất của một tỉnh
     */
    public List<PredictionResult> getRecentPredictions(String province) {
        return predictionResultRepository.findTop7ByProvinceOrderByPredictionDateDesc(province);
    }
}
