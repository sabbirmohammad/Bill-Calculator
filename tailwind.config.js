/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Hind Siliguri"', '"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'monospace'],
        bengali: ['"Hind Siliguri"', 'system-ui', 'sans-serif'],
      },
      colors: {
        volt: {
          400: '#facc15',
          500: '#eab308',
          600: '#ca8a04',
        },
        surface: {
          50: '#fafafa',
          100: '#f4f4f5',
          200: '#e4e4e7',
          800: '#27272a',
          900: '#18181b',
          950: '#09090b',
        }
      },
      boxShadow: {
        'subtle': '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        'float': '0 20px 40px -15px rgba(0, 0, 0, 0.1)',
        'glow-sm': '0 0 15px -3px rgba(234, 179, 8, 0.25)',
      }
    },
  },
  plugins: [],
}
