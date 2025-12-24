document.addEventListener('DOMContentLoaded', () => {
    // --- Modal Logic ---
    const modal = document.getElementById('dayDetailModal');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');
    const calendarGrid = document.getElementById('calendarGrid');

    const showModal = () => {
        modal.classList.remove('hidden');
        modal.style.display = 'flex';
    };

    const hideModal = () => {
        modal.classList.add('hidden');
        modal.style.display = 'none';
    };

    if (calendarGrid) {
        calendarGrid.addEventListener('click', (event) => {
            const dayCell = event.target.closest('.calendar-day');
            if (dayCell) {
                modalTitle.textContent = `Ngày ${dayCell.dataset.solardate}`;
                modalBody.innerHTML = `
                    <div class="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-4 space-y-3">
                        <div class="flex items-center gap-3">
                            <span class="material-icons-round text-pink-500">dark_mode</span>
                            <div>
                                <p class="text-xs text-slate-400 uppercase">Âm lịch</p>
                                <p class="font-semibold text-slate-700 dark:text-slate-200">${dayCell.dataset.lunardate}</p>
                            </div>
                        </div>
                        <div class="flex items-center gap-3">
                            <span class="material-icons-round text-indigo-500">calendar_month</span>
                            <div>
                                <p class="text-xs text-slate-400 uppercase">Năm âm lịch</p>
                                <p class="font-semibold text-slate-700 dark:text-slate-200">${dayCell.dataset.lunaryearname}</p>
                            </div>
                        </div>
                        <div class="flex items-center gap-3">
                            <span class="material-icons-round text-amber-500">event</span>
                            <div>
                                <p class="text-xs text-slate-400 uppercase">Tháng âm lịch</p>
                                <p class="font-semibold text-slate-700 dark:text-slate-200">${dayCell.dataset.lunarmonthname}</p>
                            </div>
                        </div>
                        <div class="flex items-center gap-3">
                            <span class="material-icons-round text-emerald-500">today</span>
                            <div>
                                <p class="text-xs text-slate-400 uppercase">Ngày âm lịch</p>
                                <p class="font-semibold text-slate-700 dark:text-slate-200">${dayCell.dataset.lunardayname}</p>
                            </div>
                        </div>
                        <div class="flex items-center gap-3">
                            <span class="material-icons-round text-yellow-500">star</span>
                            <div>
                                <p class="text-xs text-slate-400 uppercase">Giờ hoàng đạo</p>
                                <p class="font-semibold text-slate-700 dark:text-slate-200">${dayCell.dataset.hoangdao}</p>
                            </div>
                        </div>
                    </div>
                `;
                showModal();
            }
        });
    }

    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', () => {
            hideModal();
        });
    }

    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            hideModal();
        }
    });

    // --- Date Conversion Logic ---
    const solarToLunarForm = document.getElementById('solarToLunarForm');
    if (solarToLunarForm) {
        const solarToLunarResult = document.getElementById('solarToLunarResult');
        solarToLunarForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            solarToLunarResult.innerHTML = `<p class="text-yellow-500">Chức năng đang được cập nhật.</p>`;
            solarToLunarResult.classList.remove('hidden');
        });
    }

    const lunarToSolarForm = document.getElementById('lunarToSolarForm');
    if (lunarToSolarForm) {
        const lunarToSolarResult = document.getElementById('lunarToSolarResult');
        lunarToSolarForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            lunarToSolarResult.innerHTML = `<p class="text-yellow-500">Chức năng đang được cập nhật.</p>`;
            lunarToSolarResult.classList.remove('hidden');
        });
    }

    // --- Local Time Update ---
    function updateTime() {
        const now = new Date();
        const timeStr = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        const el = document.getElementById('local-time');
        if (el) el.textContent = timeStr;
    }
    setInterval(updateTime, 1000);
    updateTime();
});