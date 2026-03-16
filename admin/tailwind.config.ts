import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: 'class',
  content: [
    './src/**/*.{ts,tsx}',
    '../../packages/ui/src/**/*.{ts,tsx}',
  ],
  safelist: [
    // Custom color utilities for shadcn/ui
    'bg-primary', 'text-primary-foreground',
    'bg-secondary', 'text-secondary-foreground',
    'bg-destructive', 'text-destructive-foreground',
    'bg-muted', 'text-muted-foreground',
    'bg-accent', 'text-accent-foreground',
    'bg-popover', 'text-popover-foreground',
    'bg-card', 'text-card-foreground',
    'bg-background', 'text-foreground',
    'border-border', 'input-border',
    // Hover states
    'hover:bg-primary/90',
    'hover:bg-destructive/90',
    'hover:bg-secondary/80',
    'hover:bg-accent',
    'hover:text-accent-foreground',
    'hover:bg-muted/50',
    // Other variants
    'bg-ring', 'text-ring',
    'focus-visible:ring-ring',
  ],
  theme: {
    container: {
      center: true,
      padding: '2rem',
      screens: {
        '2xl': '1400px',
      },
    },
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: {
          DEFAULT: 'hsl(var(--primary))',
          foreground: 'hsl(var(--primary-foreground))',
        },
        secondary: {
          DEFAULT: 'hsl(var(--secondary))',
          foreground: 'hsl(var(--secondary-foreground))',
        },
        destructive: {
          DEFAULT: 'hsl(var(--destructive))',
          foreground: 'hsl(var(--destructive-foreground))',
        },
        muted: {
          DEFAULT: 'hsl(var(--muted))',
          foreground: 'hsl(var(--muted-foreground))',
        },
        accent: {
          DEFAULT: 'hsl(var(--accent))',
          foreground: 'hsl(var(--accent-foreground))',
        },
        popover: {
          DEFAULT: 'hsl(var(--popover))',
          foreground: 'hsl(var(--popover-foreground))',
        },
        card: {
          DEFAULT: 'hsl(var(--card))',
          foreground: 'hsl(var(--card-foreground))',
        },
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
}

export default config
