-- PostgreSQL schema based on the provided thong_tin_mau.txt and project logic.
-- Note: This is a conversion from MySQL and adapted for this project's needs.
-- IMPORTANT: This file is for reference only. Hibernate auto-generates the schema.

-- Table for storing historical weather data fetched from the API.
-- This data is the source for training the AI model.
-- NOTE: The actual table is created by Hibernate based on WeatherHistory.java entity
-- DROP TABLE IF EXISTS weather_history;
-- CREATE TABLE IF NOT EXISTS weather_history (
--     id SERIAL PRIMARY KEY,
--     province VARCHAR(100) NOT NULL,
--     latitude DOUBLE PRECISION NOT NULL,
--     longitude DOUBLE PRECISION NOT NULL,
--     record_date DATE NOT NULL,
--     record_time TIME,
--     temp_max DOUBLE PRECISION,
--     temp_min DOUBLE PRECISION,
--     temp_current DOUBLE PRECISION,
--     humidity DOUBLE PRECISION,
--     wind_speed DOUBLE PRECISION,
--     precipitation DOUBLE PRECISION,
--     precipitation_probability DOUBLE PRECISION,
--     pressure DOUBLE PRECISION,
--     cloud_cover DOUBLE PRECISION,
--     weather_code INTEGER,
--     recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     UNIQUE(province, record_date)
-- );

CREATE INDEX IF NOT EXISTS idx_weather_history_province_date ON weather_history(province, record_date);

-- Table to store the AI's predictions.
-- This helps in comparing AI results with actuals and for user history.
CREATE TABLE IF NOT EXISTS forecast_history (
    id SERIAL PRIMARY KEY,
    province VARCHAR(100) NOT NULL,
    request_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    forecast_date DATE NOT NULL,
    -- Predicted values
    temp_max_predicted REAL,
    temp_min_predicted REAL,
    rain_prob_predicted REAL,
    -- Actual values (can be filled in later)
    temp_max_actual REAL,
    temp_min_actual REAL
);

-- Table for news/alerts, adapted from the user's file.
CREATE TABLE IF NOT EXISTS alerts (
  id SERIAL PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  start_time TIMESTAMPTZ NOT NULL,
  end_time TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  file_path VARCHAR(255)
  -- Ignoring 'created_by' as the 'users' table is excluded per instructions.
);

-- Table for user feedback, adapted from the user's file.
CREATE TABLE IF NOT EXISTS feedback (
  id SERIAL PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Note: The 'weather_cache' table from the original file can be useful for performance,
-- but is omitted for now to keep the core structure simple. It can be added later.

-- Note: The 'users' table is intentionally excluded as per the user's request.
