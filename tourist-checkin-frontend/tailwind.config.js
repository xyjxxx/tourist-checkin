/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#FFF0F3',
          100: '#FFD6E0',
          200: '#FFC1D3',
          300: '#FFA3BF',
          400: '#FF85AB',
          500: '#FB7299',
          600: '#E85D88',
          700: '#D14A76',
          800: '#BA3864',
          900: '#8A2A4A',
        },
        accent: {
          50: '#FFF7ED',
          100: '#FFEDD5',
          200: '#FED7AA',
          300: '#FDBA74',
          400: '#FB923C',
          500: '#F97316',
          600: '#EA580C',
          700: '#C2410C',
          800: '#9A3412',
          900: '#7C2D12',
        },
        surface: '#FFFFFF',
        muted: '#F5F5F7',
        'muted-foreground': '#86868B',
        border: '#E8E8ED',
      },
      fontFamily: {
        heading: ['-apple-system', 'PingFang SC', 'SF Pro Display', 'Helvetica Neue', 'Microsoft YaHei', 'sans-serif'],
        body: ['-apple-system', 'PingFang SC', 'SF Pro Text', 'Helvetica Neue', 'Microsoft YaHei', 'sans-serif'],
      },
      borderRadius: {
        'card': '16px',
        'btn': '10px',
        'dialog': '20px',
      },
      boxShadow: {
        'card': '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.02)',
        'card-hover': '0 8px 24px -4px rgba(0,0,0,0.08), 0 4px 8px -4px rgba(0,0,0,0.04)',
        'dialog': '0 24px 64px -16px rgba(0,0,0,0.14)',
        'nav': '0 1px 3px rgba(0,0,0,0.04)',
        'fab': '0 8px 24px rgba(251,114,153,0.3)',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
        'slide-up': 'slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        'scale-in': 'scaleIn 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
        'breathe': 'breathe 2s ease-in-out infinite',
        'shimmer': 'shimmer 1.5s ease infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        breathe: {
          '0%, 100%': { transform: 'scale(1)', opacity: '1' },
          '50%': { transform: 'scale(1.05)', opacity: '0.85' },
        },
        shimmer: {
          '0%': { backgroundPosition: '200% 0' },
          '100%': { backgroundPosition: '-200% 0' },
        },
      },
    }
  },
  plugins: []
}
