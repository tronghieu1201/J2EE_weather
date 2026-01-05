let temperatureChart = null;
let rainChart = null;
let currentCityName = 'Hồ Chí Minh';

function getCityFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('city') || 'Hồ Chí Minh';
}

async function loadChartData(city) {
    currentCityName = city;
    document.getElementById('currentCity').textContent = city;

    document.getElementById('loadingArea').style.display = 'flex';
    document.getElementById('errorArea').style.display = 'none';
    document.getElementById('chartContent').style.display = 'none';

    try {
        const response = await fetch(`/api/weather-forecast?city=${encodeURIComponent(city)}`);

        if (!response.ok) {
            throw new Error('Failed to fetch data');
        }

        const data = await response.json();

        document.getElementById('loadingArea').style.display = 'none';
        document.getElementById('chartContent').style.display = 'block';

        displayChartData(data);

    } catch (error) {
        console.error('Error loading chart data:', error);
        document.getElementById('loadingArea').style.display = 'none';
        document.getElementById('errorArea').style.display = 'block';
    }
}

function displayChartData(data) {
    if (!data.sevenDayForecast || data.sevenDayForecast.length === 0) {
        document.getElementById('errorArea').style.display = 'block';
        document.getElementById('chartContent').style.display = 'none';
        return;
    }

    const forecast = data.sevenDayForecast;

    const labels = forecast.map((day, index) => {
        if (index === 0) return 'Hôm nay';
        const date = new Date(day.date);
        return date.toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit' });
    });

    const tempMax = forecast.map(day => Math.round(day.tempMax));
    const tempMin = forecast.map(day => Math.round(day.tempMin));
    const rainProb = forecast.map(day => Math.round(day.rainProbability * 100));

    const avgTemp = Math.round((tempMax.reduce((a, b) => a + b, 0) + tempMin.reduce((a, b) => a + b, 0)) / (tempMax.length + tempMin.length));
    const maxTempValue = Math.max(...tempMax);
    const minTempValue = Math.min(...tempMin);
    const avgRainValue = Math.round(rainProb.reduce((a, b) => a + b, 0) / rainProb.length);

    document.getElementById('avgTemp').textContent = avgTemp + '°C';
    document.getElementById('maxTemp').textContent = maxTempValue + '°C';
    document.getElementById('minTemp').textContent = minTempValue + '°C';
    document.getElementById('avgRain').textContent = avgRainValue + '%';

    const tempCtx = document.getElementById('temperatureChart').getContext('2d');
    if (temperatureChart) {
        temperatureChart.destroy();
    }

    temperatureChart = new Chart(tempCtx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Nhiệt độ cao nhất',
                    data: tempMax,
                    borderColor: '#ef4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Nhiệt độ thấp nhất',
                    data: tempMin,
                    borderColor: '#3b82f6',
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    tension: 0.4,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.dataset.label + ': ' + context.parsed.y + '°C';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    ticks: {
                        callback: function (value) {
                            return value + '°C';
                        }
                    }
                }
            }
        }
    });

    const rainCtx = document.getElementById('rainChart').getContext('2d');
    if (rainChart) {
        rainChart.destroy();
    }

    rainChart = new Chart(rainCtx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Xác suất mưa',
                data: rainProb,
                backgroundColor: 'rgba(94, 179, 214, 0.8)',
                borderColor: '#5eb3d6',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return 'Xác suất mưa: ' + context.parsed.y + '%';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100,
                    ticks: {
                        callback: function (value) {
                            return value + '%';
                        }
                    }
                }
            }
        }
    });
}

function handleSearch(event) {
    event.preventDefault();
    const city = document.getElementById('cityInput').value.trim();
    if (city) {
        loadChartData(city);
        const newUrl = window.location.pathname + '?city=' + encodeURIComponent(city);
        window.history.pushState({ city: city }, '', newUrl);
    }
}

window.addEventListener('DOMContentLoaded', function () {
    const initialCity = getCityFromUrl();
    loadChartData(initialCity);
});

window.addEventListener('popstate', function (event) {
    if (event.state && event.state.city) {
        loadChartData(event.state.city);
    }
});