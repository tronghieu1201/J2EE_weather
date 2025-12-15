document.addEventListener('DOMContentLoaded', function() {
    const feedbackForm = document.getElementById('feedbackForm');
    if (feedbackForm) {
        feedbackForm.addEventListener('submit', function(event) {
            event.preventDefault(); // Prevent actual form submission
            const submitButton = feedbackForm.querySelector('button[type="submit"]');
            
            // Disable fields and button
            const nameInput = document.getElementById('feedbackName');
            const emailInput = document.getElementById('feedbackEmail');
            const contentInput = document.getElementById('feedbackContent');

            if(nameInput) nameInput.disabled = true;
            if(emailInput) emailInput.disabled = true;
            if(contentInput) contentInput.disabled = true;
            if(submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = 'Đã gửi';
                submitButton.classList.remove('bg-primary', 'hover:bg-primary-dark');
                submitButton.classList.add('bg-green-500', 'hover:bg-green-600');
            }

            // Optional: Re-enable after a few seconds
            setTimeout(() => {
                 if(nameInput) {
                    nameInput.disabled = false;
                    nameInput.value = '';
                 }
                if(emailInput) {
                    emailInput.disabled = false;
                    emailInput.value = '';
                }
                if(contentInput) {
                    contentInput.disabled = false;
                    contentInput.value = '';
                }
                if(submitButton) {
                    submitButton.disabled = false;
                    submitButton.textContent = 'Gửi';
                    submitButton.classList.remove('bg-green-500', 'hover:bg-green-600');
                    submitButton.classList.add('bg-primary', 'hover:bg-primary-dark');
                }
            }, 3000); // Reset after 3 seconds
        });
    }

    // Real-time clock function
    function updateLocalTime() {
        const localTimeElement = document.getElementById('local-time');
        if (localTimeElement) {
            const now = new Date();
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            const seconds = String(now.getSeconds()).padStart(2, '0');
            localTimeElement.textContent = `${hours}:${minutes}:${seconds}`;
        }
    }

    // Update time every second
    setInterval(updateLocalTime, 1000);
    // Initial call to display time immediately
    updateLocalTime();
});
