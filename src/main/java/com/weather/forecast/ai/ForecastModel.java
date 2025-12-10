package com.weather.forecast.ai;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * A wrapper for the XGBoost model to handle loading and prediction.
 * This class is not a bean itself, but is created by the AppConfig.
 */
public class ForecastModel {

    private Booster model;

    public ForecastModel(String modelPath) {
        try {
            // Load the model as a classpath resource
            InputStream inputStream = ForecastModel.class.getClassLoader().getResourceAsStream(modelPath);
            if (inputStream == null) {
                System.err.println("Warning: Model file not found in classpath at " + modelPath);
                System.err.println("Prediction will not work until a valid model is placed there.");
                this.model = null;
            } else {
                this.model = XGBoost.loadModel(inputStream);
                System.out.println("Successfully loaded model from classpath: " + modelPath);
            }
        } catch (Exception e) { // Catching generic Exception because stream handling can throw IOException
            this.model = null;
            System.err.println("Error loading XGBoost model from classpath " + modelPath + ": " + e.getMessage());
        }
    }

    /**
     * Predicts a single result for a given set of features.
     * @param features A flat float array representing the input features.
     * @return A single float prediction.
     * @throws XGBoostError if the model is not loaded or prediction fails.
     * @throws IllegalArgumentException if the model returns unexpected output dimensions.
     */
    public float predict(float[] features) throws XGBoostError, IllegalArgumentException {
        if (model == null) {
            throw new XGBoostError("Model is not loaded, cannot perform prediction.");
        }
        DMatrix dmat = new DMatrix(features, 1, features.length, Float.NaN);
        float[][] prediction = model.predict(dmat);
        if (prediction != null && prediction.length > 0 && prediction[0].length > 0) {
            return prediction[0][0]; // Assuming a single output value
        } else {
            throw new IllegalArgumentException("Model prediction returned no valid output.");
        }
    }
}
