/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/main/resources/templates/**/*.html",
        "./src/main/resources/static/**/*.js"
    ],
    darkMode: 'class',
    theme: {
        extend: {
            colors: {
                primary: {
                    DEFAULT: '#6366f1',
                    dark: '#4f46e5',
                    light: '#818cf8'
                },
                secondary: {
                    DEFAULT: '#a855f7',
                    dark: '#9333ea'
                },
                surface: {
                    light: '#ffffff',
                    dark: '#1e293b'
                },
                background: {
                    light: '#f8fafc',
                    dark: '#0f172a'
                },
                text: {
                    light: '#1e293b',
                    dark: '#f1f5f9'
                }
            }
        },
    },
    plugins: [
        require('@tailwindcss/forms'),
        require('@tailwindcss/typography')
    ],
}
