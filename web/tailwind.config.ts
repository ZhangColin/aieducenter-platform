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
    extend: {
      colors: {
        primary: '#308ce8',
        'background-light': '#f6f7f8',
        'background-dark': '#111921',
      },
      fontFamily: {
        display: ['PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', 'Arial', 'sans-serif'],
      },
      borderRadius: {
        DEFAULT: '0.25rem',
        lg: '0.5rem',
        xl: '0.75rem',
        full: '9999px',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
}

export default config
