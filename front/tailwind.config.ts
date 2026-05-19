import type { Config } from "tailwindcss";
import animate from "tailwindcss-animate";

const config: Config = {
  content: [
    "./app/**/*.{ts,tsx}",
    "./src/**/*.{ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: "hsl(var(--primary))",
        "primary-foreground": "hsl(var(--primary-foreground))",
        border: "hsl(var(--border))",
        secondary: "hsl(var(--secondary))",
        "secondary-foreground": "hsl(var(--secondary-foreground))",
        surface: "hsl(var(--surface))",
        "surface-muted": "hsl(var(--surface-muted))",
        "on-surface": "hsl(var(--on-surface))",
        "on-surface-muted": "hsl(var(--on-surface-muted))",
        outline: "hsl(var(--outline))"
      },
      fontFamily: {
        display: ["var(--font-display)", "serif"],
        body: ["var(--font-body)", "serif"],
        script: ["var(--font-script)", "cursive"]
      },
      boxShadow: {
        glow: "0 0 24px hsl(var(--primary) / 0.35)",
        panel: "0 24px 80px hsl(248 45% 4% / 0.55)"
      },
      backgroundImage: {
        "mystic-radial": "radial-gradient(circle at 50% 20%, hsl(var(--primary) / 0.18), transparent 34%), radial-gradient(circle at 10% 80%, hsl(var(--secondary) / 0.12), transparent 30%)"
      }
    }
  },
  plugins: [animate]
};

export default config;
