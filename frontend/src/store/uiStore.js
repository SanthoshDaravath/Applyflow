import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useUiStore = create(
  persist(
    (set) => ({
      theme: 'dark',
      toasts: [],
      setTheme: (theme) => set({ theme }),
      toggleTheme: () => set((state) => ({ theme: state.theme === 'dark' ? 'light' : 'dark' })),
      pushToast: (toast) => set((state) => ({
        toasts: [...state.toasts, { id: crypto.randomUUID(), type: 'info', ...toast }].slice(-4)
      })),
      dismissToast: (id) => set((state) => ({ toasts: state.toasts.filter((toast) => toast.id !== id) }))
    }),
    { name: 'applyflow-ui' }
  )
);
