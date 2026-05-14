import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

async function request(path, options = {}) {
  const response = await fetch(`${baseURL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    credentials: 'include',
    ...options
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.message || 'Request failed');
  }
  return data;
}

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      hydrated: false,
      setHydrated: () => set({ hydrated: true }),
      setAuth: (payload) => {
        const auth = payload.accessToken ? payload : payload.data || payload;
        set({
          user: auth.user,
          accessToken: auth.accessToken,
          refreshToken: auth.refreshToken
        });
      },
      logout: () => set({ user: null, accessToken: null, refreshToken: null }),
      bootstrap: async () => {
        const { accessToken } = get();
        if (!accessToken) {
          set({ hydrated: true });
          return;
        }
        try {
          const data = await request('/auth/me', { headers: { Authorization: `Bearer ${accessToken}` } });
          set({ user: data, hydrated: true });
        } catch {
          set({ user: null, accessToken: null, refreshToken: null, hydrated: true });
        }
      },
      login: async (email, password) => {
        const data = await request('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
        get().setAuth(data);
        return data;
      },
      register: async (payload) => {
        const data = await request('/auth/register', { method: 'POST', body: JSON.stringify(payload) });
        get().setAuth(data);
        return data;
      },
      exchangeOAuthCode: async (code) => {
        const data = await request('/auth/oauth/exchange', { method: 'POST', body: JSON.stringify({ code }) });
        get().setAuth(data);
        return data;
      }
    }),
    {
      name: 'applyflow-auth',
      partialize: (state) => ({ user: state.user, accessToken: state.accessToken, refreshToken: state.refreshToken })
    }
  )
);
