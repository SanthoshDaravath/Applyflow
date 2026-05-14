import { useEffect } from 'react';
import { useUiStore } from '../store/uiStore';

export default function ThemePrimer() {
  const theme = useUiStore((state) => state.theme);
  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark');
    document.documentElement.classList.toggle('light', theme === 'light');
  }, [theme]);
  return null;
}
