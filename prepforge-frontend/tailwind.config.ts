import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "var(--background)",
        foreground: "var(--foreground)",
        brand: {
          50: "#f5f7ff",
          100: "#ebf0fe",
          200: "#dce5fe",
          300: "#c2d1fd",
          400: "#9eb3fb",
          500: "#758ef7",
          600: "#4f65f0",
          700: "#3b4ce0",
          800: "#323db5",
          900: "#2d378f",
          950: "#1a1f54",
        },
      },
      fontFamily: {
        sans: ["var(--font-sans)", "system-ui", "sans-serif"],
        mono: ["var(--font-mono)", "monospace"],
      },
    },
  },
  plugins: [],
};
export default config;
