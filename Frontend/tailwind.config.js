/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#1B3A6B",
        accent: "#F59E0B",
        success: "#10B981",
        danger: "#EF4444",
      },
    },
  },
  plugins: [],
}
