-- PostgreSQL schema based on the provided thong_tin_mau.txt and project logic.
-- Note: This is a conversion from MySQL and adapted for this project's needs.

-- Table for storing historical weather data fetched from the API.
-- This data is the source for training the AI model.
CREATE TABLE IF NOT EXISTS weather_history (
    id SERIAL PRIMARY KEY,
    province VARCHAR(100) NOT NULL,
    lat DECIMAL(9, 6) NOT NULL,
    lon DECIMAL(9, 6) NOT NULL,
    "date" DATE NOT NULL,
    "time" TIME NOT NULL,
    temperature REAL,
    humidity REAL,
    wind_speed REAL,
    precipitation REAL,
    pressure REAL,
    cloudcover REAL,
    recorded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(province, "date", "time")
);

CREATE INDEX IF NOT EXISTS idx_weather_history_province_date ON weather_history(province, "date");

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
