async function fetchWeatherData(city) {
    const weatherDisplayArea = document.getElementById('weatherDisplayArea');
    if (weatherDisplayArea) {
        weatherDisplayArea.innerHTML = '<div class="flex flex-col items-center justify-center p-5 space-y-3"><div class="animate-spin rounded-full h-12 w-12 border-4 border-primary border-t-transparent" role="status"><span class="sr-only">Đang tải...</span></div><p class="text-lg text-slate-700 dark:text-slate-300">Đang tải dữ liệu thời tiết cho ' + city + '...</p></div>';
        try {
            const response = await fetch(`/get-weather-fragment?city=${encodeURIComponent(city)}`);
            if (response.ok) {
                const htmlFragment = await response.text();
                weatherDisplayArea.innerHTML = htmlFragment;
            } else {
                weatherDisplayArea.innerHTML = '<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative text-center" role="alert">Không thể tải dữ liệu thời tiết cho ' + city + '. Vui lòng thử lại.</div>';
                console.error('Failed to fetch weather fragment:', response.statusText);
            }
        } catch (error) {
            weatherDisplayArea.innerHTML = '<div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative text-center" role="alert">Có lỗi xảy ra khi tải dữ liệu thời tiết.</div>';
            console.error('Error fetching weather data:', error);
        }
    }
}
