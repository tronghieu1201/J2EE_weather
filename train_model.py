import pandas as pd
import numpy as np
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
import os

def load_training_data():
    
    print("Loading historical weather data...")
    # Example: dummy data for 100 days
    data = {
        'latitude': np.random.uniform(10, 20, 100),
        'longitude': np.random.uniform(100, 110, 100),
        'day_of_year_today': np.random.randint(1, 365, 100),
        'past_day1_max_temp': np.random.uniform(20, 35, 100),
        'past_day1_min_temp': np.random.uniform(15, 30, 100),
        'past_day1_rain_sum': np.random.uniform(0, 10, 100),
        'past_day2_max_temp': np.random.uniform(20, 35, 100),
        'past_day2_min_temp': np.random.uniform(15, 30, 100),
        'past_day2_rain_sum': np.random.uniform(0, 10, 100),
        'past_day3_max_temp': np.random.uniform(20, 35, 100),
        'past_day3_min_temp': np.random.uniform(15, 30, 100),
        'past_day3_rain_sum': np.random.uniform(0, 10, 100),
        # Target variables for the *next* day
        'target_next_day_max_temp': np.random.uniform(20, 35, 100),
        'target_next_day_min_temp': np.random.uniform(15, 30, 100),
        'target_next_day_rain_prob': np.random.uniform(0, 1, 100)
    }
    df = pd.DataFrame(data)
    print(f"Loaded {len(df)} rows of data.")
    return df

def prepare_features(df):
    """
    Prepare feature matrix (X) and target vectors (y) from the DataFrame.
    Ensure that the order of features in `X` matches the order expected by the Java application
    when it creates the feature vector.

    In Java's WeatherService, the features are:
    features[0] = (float) coords[0]; # latitude
    features[1] = (float) coords[1]; # longitude
    features[2] = predictionDate.getDayOfYear(); # day_of_year
    Then for each of PAST_DAYS_FOR_FEATURES (e.g., 3 days):
        features[...] = (float) day.getTempMax();
        features[...] = (float) day.getTempMin();
        features[...] = (float) day.getRainProbability(); (Note: In Java this is `rain` from historical data, which came from `precipitation_sum` from API)
    """
    print("Preparing features and target variables...")
    # Define features (X) - these must match the order and type used in Java's WeatherService
    features = [
        'latitude', 'longitude', 'day_of_year_today',
        'past_day1_max_temp', 'past_day1_min_temp', 'past_day1_rain_sum',
        'past_day2_max_temp', 'past_day2_min_temp', 'past_day2_rain_sum',
        'past_day3_max_temp', 'past_day3_min_temp', 'past_day3_rain_sum'
    ]
    X = df[features]

    # Define target variables (y)
    y_max_temp = df['target_next_day_max_temp']
    y_min_temp = df['target_next_day_min_temp']
    y_rain_prob = df['target_next_day_rain_prob']

    print(f"Features (X) shape: {X.shape}")
    print(f"Target (y_max_temp) shape: {y_max_temp.shape}")
    return X, y_max_temp, y_min_temp, y_rain_prob

def train_and_save_model(X, y, model_name, output_dir):
    """
    Train an XGBoost Regressor model and save it to the specified directory.
    """
    print(f"Training model for {model_name}...")
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    model = xgb.XGBRegressor(objective='reg:squarederror', n_estimators=100, learning_rate=0.1, random_state=42)
    model.fit(X_train, y_train)

    predictions = model.predict(X_test)
    rmse = np.sqrt(mean_squared_error(y_test, predictions))
    print(f"  {model_name} RMSE on test set: {rmse:.2f}")

    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)
    
    # Save the model
    model_path = os.path.join(output_dir, model_name + ".bin")
    # model.save_model(model_path) # Use save_model for binary format
    booster = model.get_booster()
    booster.save_model(model_path)
    print(f"  Model saved to {model_path}")
    return model_path

if __name__ == "__main__":
    output_models_dir = "src/main/resources/models/" # Path relative to project root

    # 1. Load Data
    df_data = load_training_data()

    # 2. Prepare Features and Targets
    X_features, y_max_temp, y_min_temp, y_rain_prob = prepare_features(df_data)

    # 3. Train and Save Models
    print("\n--- Starting Model Training ---")
    
    # Train Daily Max Temperature Model
    train_and_save_model(X_features, y_max_temp, "daily_model_max_temp", output_models_dir)

    # Train Daily Min Temperature Model
    train_and_save_model(X_features, y_min_temp, "daily_model_min_temp", output_models_dir)
    
    # Train Daily Rain Probability Model
    train_and_save_model(X_features, y_rain_prob, "daily_model_rain_prob", output_models_dir)

    print("\n--- Model Training Complete ---")
    print("Please ensure these models are correctly named and placed in:")
    print(f"{output_models_dir}")
    print("The Java application will load them automatically on startup.")
    print("Remember to adjust the Java code (WeatherService) and this script's feature")
    print("preparation to ensure they match for successful prediction.")
    print("The WeatherService is now designed to load these three separate models for each prediction target.")
