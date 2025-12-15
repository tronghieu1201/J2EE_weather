document.addEventListener('DOMContentLoaded', () => {
    // --- Modal Logic ---
    const modal = document.getElementById('dayDetailModal');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');
    const calendarGrid = document.getElementById('calendarGrid');

    if (calendarGrid) {
        calendarGrid.addEventListener('click', (event) => {
            const dayCell = event.target.closest('.calendar-day');
            if (dayCell) {
                modalTitle.textContent = `Chi tiết ngày ${dayCell.dataset.solardate}`;
                modalBody.innerHTML = `
                    <p><strong>Âm lịch:</strong> ${dayCell.dataset.lunardate}</p>
                    <p><strong>Năm âm lịch:</strong> ${dayCell.dataset.lunaryearname}</p>
                    <p><strong>Tháng âm lịch:</strong> ${dayCell.dataset.lunarmonthname}</p>
                    <p><strong>Ngày âm lịch:</strong> ${dayCell.dataset.lunardayname}</p>
                    <p><strong>Giờ hoàng đạo:</strong> ${dayCell.dataset.hoangdao}</p>
                `;
                modal.classList.remove('hidden');
            }
        });
    }

    if(closeModalBtn) {
        closeModalBtn.addEventListener('click', () => {
            modal.classList.add('hidden');
        });
    }

    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            modal.classList.add('hidden');
        }
    });

    // --- Date Conversion Logic ---
    const solarToLunarForm = document.getElementById('solarToLunarForm');
    if (solarToLunarForm) {
        const solarToLunarResult = document.getElementById('solarToLunarResult');
        solarToLunarForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            // This part is now non-functional as the backend proxy was removed.
            // It could be reimplemented if a reliable conversion API is found.
            solarToLunarResult.innerHTML = `<p class="text-yellow-500">Chức năng đang được cập nhật.</p>`;
            solarToLunarResult.classList.remove('hidden');
        });
    }

    const lunarToSolarForm = document.getElementById('lunarToSolarForm');
    if(lunarToSolarForm) {
        const lunarToSolarResult = document.getElementById('lunarToSolarResult');
        lunarToSolarForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            // This part is now non-functional as the backend proxy was removed.
            lunarToSolarResult.innerHTML = `<p class="text-yellow-500">Chức năng đang được cập nhật.</p>`;
            lunarToSolarResult.classList.remove('hidden');
        });
    }
});