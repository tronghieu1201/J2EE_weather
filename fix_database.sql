-- =====================================================
-- RUN THIS SCRIPT IN PostgreSQL TO FIX DATABASE SCHEMA
-- =====================================================
-- Mở pgAdmin4 hoặc psql và chạy các lệnh sau:

-- 1. Xóa bảng cũ (nếu có)
DROP TABLE IF EXISTS weather_history CASCADE;

-- 2. Tạo bảng mới với schema phù hợp cho XGBoost
CREATE TABLE weather_history (
    id SERIAL PRIMARY KEY,
    province VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    record_date DATE NOT NULL,
    record_time TIME,
    temp_max DOUBLE PRECISION,
    temp_min DOUBLE PRECISION,
    temp_current DOUBLE PRECISION,
    humidity DOUBLE PRECISION,          -- NULL allowed
    wind_speed DOUBLE PRECISION,
    precipitation DOUBLE PRECISION,
    precipitation_probability DOUBLE PRECISION,
    pressure DOUBLE PRECISION,
    cloud_cover DOUBLE PRECISION,
    weather_code INTEGER,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(province, record_date)
);

-- 3. Tạo index cho tốc độ query
CREATE INDEX idx_weather_history_province_date ON weather_history(province, record_date);

-- 4. Verify
SELECT 'Table created successfully!' as status;
