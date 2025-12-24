// Dark Mode Toggle with localStorage persistence
(function () {
    // Apply saved theme on page load (before content renders)
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.documentElement.classList.add('dark');
    } else if (savedTheme === 'light') {
        document.documentElement.classList.remove('dark');
    } else {
        // Check system preference
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            document.documentElement.classList.add('dark');
        }
    }

    // Update icon when DOM is ready
    document.addEventListener('DOMContentLoaded', function () {
        updateThemeIcon();
    });
})();

// Update the theme icon based on current mode
function updateThemeIcon() {
    const themeIcon = document.getElementById('themeIcon');
    if (themeIcon) {
        const isDark = document.documentElement.classList.contains('dark');
        themeIcon.textContent = isDark ? 'light_mode' : 'dark_mode';
    }
}

// Toggle function that persists to localStorage and updates icon
function toggleTheme() {
    const isDark = document.documentElement.classList.toggle('dark');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    updateThemeIcon();
}

// Legacy function name for backward compatibility
function toggleDarkMode() {
    toggleTheme();
}
