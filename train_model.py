"""
XGBoost Weather Forecast Model Training Script
===============================================
Script này huấn luyện các model XGBoost để dự đoán thời tiết dựa trên dữ liệu lịch sử
từ PostgreSQL database (bảng weather_history).

Workflow:
1. Kết nối PostgreSQL và đọc dữ liệu từ weather_history
2. Chuẩn bị features và targets
3. Huấn luyện 3 models: max_temp, min_temp, rain_prob
4. Lưu models vào src/main/resources/models/

Chạy: python train_model.py
"""

import pandas as pd
import numpy as np
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error
import os
import sys

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'database': 'data_weather',
    'user': 'postgres',
    'password': '123456',
    'port': 5432
}

def load_training_data():
    """
    Load dữ liệu thực từ PostgreSQL database (bảng weather_history).
    """
    print("=" * 60)
    print("BƯỚC 1: KẾT NỐI DATABASE VÀ LOAD DỮ LIỆU")
    print("=" * 60)
    
    try:
        import psycopg2
    except ImportError:
        print("ERROR: Chưa cài psycopg2. Chạy: pip install psycopg2-binary")
        sys.exit(1)
    
    try:
        print(f"Kết nối tới PostgreSQL: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")
        conn = psycopg2.connect(**DB_CONFIG)
        
        query = """
        SELECT 
            id,
            province,
            latitude,
            longitude,
            record_date,
            temp_max,
            temp_min,
            temp_current,
            humidity,
            wind_speed,
            precipitation,
            precipitation_probability,
            pressure,
            cloud_cover,
            weather_code
        FROM weather_history 
        WHERE temp_max IS NOT NULL 
          AND temp_min IS NOT NULL
        ORDER BY province, record_date
        """
        
        df = pd.read_sql_query(query, conn)
        conn.close()
        
        print(f"✓ Đã load {len(df)} bản ghi từ weather_history")
        print(f"✓ Số tỉnh/thành có dữ liệu: {df['province'].nunique()}")
        
        if len(df) == 0:
            print("\n⚠️ CẢNH BÁO: Không có dữ liệu trong database!")
            print("   Vui lòng truy cập http://localhost:8080/admin/data-update")
            print("   và nhấn 'Thu Thập Dữ Liệu' trước khi chạy script này.")
            sys.exit(1)
            
        return df
        
    except Exception as e:
        print(f"ERROR: Không thể kết nối database: {e}")
        print("\nĐảm bảo:")
        print("  1. PostgreSQL đang chạy")
        print("  2. Database 'weather-j2ee' đã tồn tại")
        print("  3. Thông tin đăng nhập đúng")
        sys.exit(1)


def prepare_features(df):
    """
    Chuẩn bị features và targets từ DataFrame.
    
    Features được tạo để phù hợp với Java WeatherService:
    - latitude, longitude
    - day_of_year
    - Dữ liệu lịch sử 3 ngày trước (temp_max, temp_min, rain_prob)
    """
    print("\n" + "=" * 60)
    print("BƯỚC 2: CHUẨN BỊ FEATURES VÀ TARGETS")
    print("=" * 60)
    
    # Thêm day_of_year từ record_date
    df['record_date'] = pd.to_datetime(df['record_date'])
    df['day_of_year'] = df['record_date'].dt.dayofyear
    
    # Sắp xếp theo province và date
    df = df.sort_values(['province', 'record_date']).reset_index(drop=True)
    
    # Tạo features từ dữ liệu lịch sử (shift để lấy ngày trước)
    training_data = []
    
    for province in df['province'].unique():
        province_df = df[df['province'] == province].copy()
        
        if len(province_df) < 4:  # Cần ít nhất 4 ngày (3 ngày lịch sử + 1 ngày target)
            continue
        
        # Fill NaN cho precipitation_probability nếu không có (Archive API không trả về trực tiếp)
        if province_df['precipitation_probability'].isna().all():
            # Tính từ precipitation: có mưa > 0.1mm = 1, không = 0
            province_df['precipitation_probability'] = (province_df['precipitation'] > 0.1).astype(float)
        else:
            province_df['precipitation_probability'] = province_df['precipitation_probability'].fillna(0)
        
        # Tạo lagged features (dữ liệu của 1, 2, 3 ngày trước)
        for lag in range(1, 4):
            province_df[f'past_day{lag}_max_temp'] = province_df['temp_max'].shift(lag)
            province_df[f'past_day{lag}_min_temp'] = province_df['temp_min'].shift(lag)
            province_df[f'past_day{lag}_rain_prob'] = province_df['precipitation_probability'].shift(lag)
        
        # Chỉ drop NaN trên các cột temp (không drop trên precipitation_probability)
        required_cols = ['past_day1_max_temp', 'past_day1_min_temp', 
                        'past_day2_max_temp', 'past_day2_min_temp',
                        'past_day3_max_temp', 'past_day3_min_temp']
        province_df = province_df.dropna(subset=required_cols)
        
        if len(province_df) > 0:
            training_data.append(province_df)
    
    if len(training_data) == 0:
        print("⚠️ Không đủ dữ liệu để tạo features!")
        print("   Cần ít nhất 4 ngày dữ liệu liên tiếp cho mỗi tỉnh.")
        print("   Hãy thu thập thêm dữ liệu và chạy lại.")
        sys.exit(1)
    
    final_df = pd.concat(training_data, ignore_index=True)
    
    # Định nghĩa features (phải khớp với Java WeatherService)
    feature_columns = [
        'latitude', 'longitude', 'day_of_year',
        'past_day1_max_temp', 'past_day1_min_temp', 'past_day1_rain_prob',
        'past_day2_max_temp', 'past_day2_min_temp', 'past_day2_rain_prob',
        'past_day3_max_temp', 'past_day3_min_temp', 'past_day3_rain_prob'
    ]
    
    X = final_df[feature_columns].fillna(0)  # Fill NaN với 0
    
    # Targets - dự đoán cho ngày hiện tại
    y_max_temp = final_df['temp_max']
    y_min_temp = final_df['temp_min']
    y_rain_prob = final_df['precipitation_probability'].fillna(0)
    
    print(f"✓ Features shape: {X.shape}")
    print(f"✓ Feature columns: {feature_columns}")
    print(f"✓ Số samples để train: {len(X)}")
    
    return X, y_max_temp, y_min_temp, y_rain_prob


def train_and_save_model(X, y, model_name, output_dir, conn):
    """
    Huấn luyện XGBoost Regressor, lưu model và lưu metrics vào database.
    """
    print(f"\n--- Training: {model_name} ---")
    
    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )
    
    print(f"  Train size: {len(X_train)}, Test size: {len(X_test)}")
    
    # Train model
    model = xgb.XGBRegressor(
        objective='reg:squarederror',
        n_estimators=100,
        learning_rate=0.1,
        max_depth=6,
        random_state=42,
        verbosity=0
    )
    
    model.fit(X_train, y_train)
    
    # Evaluate
    predictions = model.predict(X_test)
    rmse = np.sqrt(mean_squared_error(y_test, predictions))
    mae = mean_absolute_error(y_test, predictions)
    
    print(f"  ✓ RMSE: {rmse:.3f}")
    print(f"  ✓ MAE: {mae:.3f}")
    
    # Save model
    os.makedirs(output_dir, exist_ok=True)
    model_path = os.path.join(output_dir, model_name + ".bin")
    
    booster = model.get_booster()
    booster.save_model(model_path)
    
    print(f"  ✓ Model saved to: {model_path}")
    
    # Lưu metrics vào database
    try:
        save_metrics_to_db(conn, model_name, rmse, mae, len(X_train), len(X_test))
        print(f"  ✓ Metrics saved to database")
    except Exception as e:
        print(f"  ⚠ Warning: Could not save metrics to DB: {e}")
    
    return model_path, rmse, mae


def save_metrics_to_db(conn, model_name, rmse, mae, train_samples, test_samples):
    """
    Lưu training metrics vào bảng model_metrics trong PostgreSQL.
    """
    import json
    from datetime import datetime
    
    cursor = conn.cursor()
    
    # Hyperparameters đang sử dụng
    hyperparams = json.dumps({
        "n_estimators": 100,
        "learning_rate": 0.1,
        "max_depth": 6,
        "objective": "reg:squarederror"
    })
    
    # Tạo version tự động theo timestamp
    model_version = datetime.now().strftime("v%Y%m%d_%H%M")
    
    sql = """
    INSERT INTO model_metrics 
    (model_name, model_version, rmse, mae, train_samples, test_samples, hyperparameters, trained_at)
    VALUES (%s, %s, %s, %s, %s, %s, %s, NOW())
    """
    
    cursor.execute(sql, (
        model_name, 
        model_version, 
        float(rmse), 
        float(mae), 
        train_samples, 
        test_samples, 
        hyperparams
    ))
    
    conn.commit()
    cursor.close()


def main():
    print("\n")
    print("╔═══════════════════════════════════════════════════════════╗")
    print("║       XGBOOST WEATHER FORECAST MODEL TRAINING             ║")
    print("╚═══════════════════════════════════════════════════════════╝")
    print()
    
    output_dir = "src/main/resources/models/"
    
    # 1. Load data và lấy connection
    import psycopg2
    conn = psycopg2.connect(**DB_CONFIG)
    
    df = load_training_data()
    
    # 2. Prepare features
    X, y_max_temp, y_min_temp, y_rain_prob = prepare_features(df)
    
    # 3. Train models
    print("\n" + "=" * 60)
    print("BƯỚC 3: HUẤN LUYỆN MODELS")
    print("=" * 60)
    
    results = []
    
    # Max Temperature Model
    path, rmse, mae = train_and_save_model(X, y_max_temp, "daily_model_max_temp", output_dir, conn)
    results.append(("Max Temp", rmse, mae))
    
    # Min Temperature Model
    path, rmse, mae = train_and_save_model(X, y_min_temp, "daily_model_min_temp", output_dir, conn)
    results.append(("Min Temp", rmse, mae))
    
    # Rain Probability Model
    path, rmse, mae = train_and_save_model(X, y_rain_prob, "daily_model_rain_prob", output_dir, conn)
    results.append(("Rain Prob", rmse, mae))
    
    # Close connection
    conn.close()
    
    # Summary
    print("\n" + "=" * 60)
    print("HOÀN THÀNH!")
    print("=" * 60)
    print("\nKết quả huấn luyện:")
    print("-" * 40)
    for name, rmse, mae in results:
        print(f"  {name:15s}: RMSE = {rmse:.3f}, MAE = {mae:.3f}")
    
    print(f"\nModels đã được lưu vào: {output_dir}")
    print("✓ Metrics đã được lưu vào database (table: model_metrics)")
    print("\n📋 BƯỚC TIẾP THEO:")
    print("   1. Khởi động lại Spring Boot: mvn spring-boot:run")
    print("   2. Truy cập http://localhost:8080 để xem kết quả dự đoán")
    print()


if __name__ == "__main__":
    main()
