/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        display: ["Bebas Neue", "sans-serif"],
        body: ["Space Grotesk", "sans-serif"],
      },
      colors: {
        night: "#0b0d11",
        ember: "#ff7a18",
        mint: "#7bdff2",
        dusk: "#20252f",
        neon: "#f8ff4a",
        clay: "#c9a58f",
      },
      boxShadow: {
        glow: "0 0 30px rgba(255, 122, 24, 0.35)",
      },
    },
  },
  plugins: [],
};
