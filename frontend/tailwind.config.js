/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      boxShadow: {
        glass: '0 20px 80px rgba(15, 23, 42, 0.25)'
      },
      backgroundImage: {
        'hero-gradient': 'radial-gradient(circle at top left, rgba(34,211,238,0.20), transparent 36%), radial-gradient(circle at top right, rgba(168,85,247,0.18), transparent 30%), linear-gradient(135deg, rgba(15,23,42,1), rgba(2,6,23,1))'
      }
    }
  },
  plugins: []
};
