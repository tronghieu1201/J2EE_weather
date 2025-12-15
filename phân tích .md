Walkthrough: Tích Hợp XGBoost Dự Đoán Thời Tiết
Tóm Tắt Thay Đổi
Đã hoàn thành tích hợp XGBoost vào dự án để dự đoán thời tiết dựa trên dữ liệu lịch sử.

Các File Đã Tạo/Sửa
1. Model & Repository (Mới)
File	Mô tả
WeatherHistory.java
Entity cho bảng weather_history với đầy đủ fields cho XGBoost
WeatherHistoryRepository.java
Repository với các query cần thiết
2. Services (Cập nhật)
File	Thay đổi
DataUpdateService.java
Thu thập dữ liệu daily cho 63 tỉnh/thành, lưu vào weather_history
WeatherService.java
Thêm 
get7DayForecast()
 sử dụng XGBoost với fallback về API
3. Controllers (Cập nhật)
File	Thay đổi
DataUpdateController.java
Sử dụng 
WeatherHistoryRepository
WeatherController.java
Gọi 
get7DayForecast(city)
 thay vì 
get7DayForecastFromReport()
4. Templates (Cập nhật)
File	Thay đổi
update-data.html
Giao diện mới với thống kê và hướng dẫn
5. Python (Cập nhật)
File	Thay đổi
train_model.py
Đọc dữ liệu từ PostgreSQL thay vì dummy data
Workflow Sử Dụng
1. Thu thập dữ liệu
2. Huấn luyện model
3. Chạy web
4. Xem kết quả
Bước 1: Thu thập dữ liệu
# Khởi động Spring Boot
mvn spring-boot:run
# Truy cập trang admin
# http://localhost:8080/admin/data-update
# Nhấn "Thu Thập Dữ Liệu Ngay"
Bước 2: Huấn luyện XGBoost
# Cài dependencies Python (nếu chưa có)
pip install psycopg2-binary pandas numpy xgboost scikit-learn
# Chạy training
python train_model.py
Bước 3: Khởi động lại Spring Boot
# Tắt Spring Boot hiện tại (Ctrl+C)
# Chạy lại để load model mới
mvn spring-boot:run
Bước 4: Kiểm tra kết quả
Truy cập http://localhost:8080
Console sẽ hiển thị: ✓ Using XGBoost prediction for [tên tỉnh]
Cấu Trúc Database
Bảng weather_history (Mới)
Column	Type	Mô tả
id	SERIAL	Primary key
province	VARCHAR(100)	Tên tỉnh/thành
latitude	DOUBLE	Vĩ độ
longitude	DOUBLE	Kinh độ
record_date	DATE	Ngày ghi nhận
temp_max	DOUBLE	Nhiệt độ cao nhất
temp_min	DOUBLE	Nhiệt độ thấp nhất
temp_current	DOUBLE	Nhiệt độ hiện tại
humidity	DOUBLE	Độ ẩm
precipitation_probability	DOUBLE	Xác suất mưa (0-1)
weather_code	INTEGER	Mã thời tiết
recorded_at	TIMESTAMP	Thời gian lưu
Logic XGBoost
Features (12 features)
latitude - Vĩ độ
longitude - Kinh độ
day_of_year - Ngày trong năm (1-365) 4-6. past_day1_* - Dữ liệu ngày hôm qua 7-9. past_day2_* - Dữ liệu 2 ngày trước 10-12. past_day3_* - Dữ liệu 3 ngày trước
Models
daily_model_max_temp.bin
 - Dự đoán nhiệt độ cao nhất
daily_model_min_temp.bin
 - Dự đoán nhiệt độ thấp nhất
daily_model_rain_prob.bin
 - Dự đoán xác suất mưa
Fallback
Nếu không đủ dữ liệu lịch sử (< 3 ngày), hệ thống sẽ tự động fallback về API Open-Meteo.

Lưu Ý Quan Trọng
IMPORTANT

Cần thu thập dữ liệu ít nhất 4 ngày liên tiếp để XGBoost có thể hoạt động.

Bạn nên thu thập dữ liệu hàng ngày để model có độ chính xác cao hơn.

TIP

Có thể set up scheduled task để tự động thu thập dữ liệu hàng ngày bằng Spring @Scheduled.