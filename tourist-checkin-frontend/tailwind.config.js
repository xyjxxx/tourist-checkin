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
          900: '#18191C',
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
        muted: '#F1F2F3',
        'muted-foreground': '#9499A0',
        border: '#E3E5E7',
      },
      fontFamily: {
        heading: ['PingFang SC', 'system-ui', 'Microsoft YaHei', 'sans-serif'],
        body: ['PingFang SC', 'system-ui', 'Microsoft YaHei', 'sans-serif'],
      },
      borderRadius: {
        'card': '12px',
        'btn': '8px',
        'dialog': '16px',
      },
      boxShadow: {
        'card': '0 1px 3px 0 rgba(0,0,0,0.04), 0 1px 2px -1px rgba(0,0,0,0.04)',
        'card-hover': '0 4px 12px 0 rgba(0,0,0,0.08), 0 2px 4px -2px rgba(0,0,0,0.06)',
        'dialog': '0 20px 60px -12px rgba(0,0,0,0.12)',
        'nav': '0 1px 3px 0 rgba(0,0,0,0.04)',
        'fab': '0 8px 24px rgba(251,114,153,0.3)',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
        'breathe': 'breathe 2s ease-in-out infinite',
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
      },
    }
  },
  plugins: []
}
