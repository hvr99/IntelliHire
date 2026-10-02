import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/pages/**/*.{js,ts,jsx,tsx,mdx}", "./src/components/**/*.{js,ts,jsx,tsx,mdx}", "./src/app/**/*.{js,ts,jsx,tsx,mdx}"],
  theme: { extend: { colors: { ink: "#182230", mist: "#f5f7fb", coral: "#f26b4f", mint: "#dff6ed" }, boxShadow: { soft: "0 16px 45px rgba(24,34,48,.08)" } } },
  plugins: []
};
export default config;
