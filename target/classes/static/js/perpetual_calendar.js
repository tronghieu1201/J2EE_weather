let currentMonth = new Date().getMonth();
    let currentYear = new Date().getFullYear();

    const calendarGrid = document.getElementById('calendarGrid');
    const currentMonthYearHeader = document.getElementById('currentMonthYear');
    const prevMonthBtn = document.getElementById('prevMonthBtn');
    const nextMonthBtn = document.getElementById('nextMonthBtn');
    
    const monthNames = ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"];

    function renderCalendar(year, month) {
        // Clear calendar grid
        calendarGrid.innerHTML = '';
        
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const numDays = lastDay.getDate();
        const startDayOfWeek = firstDay.getDay();

        currentMonthYearHeader.textContent = `${monthNames[month]}, ${year}`;

        const today = new Date();
        const isCurrentMonth = month === today.getMonth() && year === today.getFullYear();
        const todayDate = today.getDate();

        const dayElements = [];

        // Fill leading empty days
        for (let i = 0; i < startDayOfWeek; i++) {
            const emptyDiv = document.createElement('div');
            emptyDiv.classList.add('col', 'day-cell', 'empty');
            calendarGrid.appendChild(emptyDiv);
        }

        // Fill days of the month
        for (let day = 1; day <= numDays; day++) {
            const dayDiv = document.createElement('div');
            dayDiv.classList.add('col', 'day-cell');
            
            if (isCurrentMonth && day === todayDate) {
                dayDiv.classList.add('today');
            }

            const dayNumber = document.createElement('div');
            dayNumber.classList.add('day-number');
            dayNumber.textContent = day;
            dayDiv.appendChild(dayNumber);

            const lunarDate = document.createElement('div');
            lunarDate.classList.add('lunar-date', 'loading');
            lunarDate.textContent = 'Đang tải...';
            dayDiv.appendChild(lunarDate);

            dayElements.push({ element: lunarDate, day: day });

            // Add click event
            dayDiv.addEventListener('click', () => showDayDetails(day, month, year));

            calendarGrid.appendChild(dayDiv);
        }
        
        // Fetch lunar dates for the entire month
        fetch('/api/lunar-month-dates', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ month: month + 1, year: year })
        })
        .then(response => response.json())
        .then(lunarDates => {
            dayElements.forEach((item, index) => {
                if (index < lunarDates.length) {
                    const lunarData = lunarDates[index];
                    const element = item.element;
                    
                    element.classList.remove('loading');
                    
                    if (lunarData && lunarData.day && lunarData.month) {
                        element.textContent = `${lunarData.day}/${lunarData.month} AL`;
                        if (lunarData.day === 1) {
                            element.style.fontWeight = 'bold';
                            element.style.color = '#dc3545';
                        }
                    } else {
                        element.textContent = 'N/A';
                        element.style.color = '#999';
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error fetching lunar month dates:', error);
            dayElements.forEach(item => {
                item.element.classList.remove('loading');
                item.element.textContent = 'Lỗi';
                item.element.style.color = '#dc3545';
            });
        });
    }

    function showDayDetails(day, month, year) {
        const modal = new bootstrap.Modal(document.getElementById('dayDetailModal'));
        const modalTitle = document.getElementById('modalTitle');
        const modalBody = document.getElementById('modalBody');
        
        modalTitle.textContent = `Ngày ${day}/${month + 1}/${year}`;
        modalBody.innerHTML = `
            <div class="text-center">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Đang tải...</span>
                </div>
                <p class="mt-2">Đang tải thông tin chi tiết...</p>
            </div>
        `;
        
        modal.show();

        // Simulate fetching detailed information
        // Replace this with actual API call to get lunar calendar details
        setTimeout(() => {
            modalBody.innerHTML = `
                <div class="row">
                    <div class="col-6">
                        <h6>Dương lịch</h6>
                        <p class="fw-bold">${day}/${month + 1}/${year}</p>
                    </div>
                    <div class="col-6">
                        <h6>Âm lịch</h6>
                        <p class="fw-bold">15/10 (Ví dụ)</p>
                    </div>
                </div>
                <hr>
                <h6>Giờ Hoàng Đạo</h6>
                <p class="text-success">Tý (23-01h), Mão (05-07h), Ngọ (11-13h), Dậu (17-19h)</p>
                
                <h6 class="mt-3">Giờ Hắc Đạo</h6>
                <p class="text-danger">Sửu (01-03h), Thìn (07-09h), Mùi (13-15h), Tuất (19-21h)</p>
                
                <div class="alert alert-info mt-3">
                    <small><strong>Lưu ý:</strong> Đây là dữ liệu mẫu. Cần kết nối API thực tế để hiển thị thông tin chính xác.</small>
                </div>
            `;
        }, 800);
    }

    function changeMonth(delta) {
        currentMonth += delta;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        } else if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        renderCalendar(currentYear, currentMonth);
    }

    // Event Listeners
    prevMonthBtn.addEventListener('click', () => changeMonth(-1));
    nextMonthBtn.addEventListener('click', () => changeMonth(1));

    // Initial render
    renderCalendar(currentYear, currentMonth);