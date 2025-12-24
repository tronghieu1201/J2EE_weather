@echo off
echo ========================================
echo   CHAY NHANH - Weather Forecast App
echo ========================================
echo.

:: Kiem tra xem JAR da ton tai chua
if exist "target\weather-forecast-j2ee-1.0-SNAPSHOT.jar" (
    echo [OK] Tim thay file JAR, dang khoi dong...
    echo.
    java -Xms128m -Xmx512m -XX:+UseG1GC -jar target\weather-forecast-j2ee-1.0-SNAPSHOT.jar
) else (
    echo [!] Chua co file JAR. Dang build...
    echo.
    call mvn clean package -DskipTests
    echo.
    echo [OK] Build xong. Dang khoi dong...
    echo.
    java -Xms128m -Xmx512m -XX:+UseG1GC -jar target\weather-forecast-j2ee-1.0-SNAPSHOT.jar
)
