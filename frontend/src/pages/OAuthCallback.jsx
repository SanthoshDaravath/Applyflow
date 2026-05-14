import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { useUiStore } from '../store/uiStore';

export default function OAuthCallback() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const pushToast = useUiStore((state) => state.pushToast);
  const [message, setMessage] = useState('Completing Google sign-in…');

  useEffect(() => {
    const code = params.get('code');
    if (!code) {
      pushToast({ title: 'OAuth failed', message: 'Missing authorization code.', type: 'error' });
      navigate('/login');
      return;
    }
    (async () => {
      try {
        const { data } = await api.post('/auth/oauth/exchange', { code });
        setAuth(data);
        pushToast({ title: 'Google sign-in complete', message: 'You are now signed in.', type: 'success' });
        navigate('/');
      } catch (error) {
        setMessage(error.response?.data?.message || error.message || 'OAuth exchange failed');
      }
    })();
  }, [params, navigate, setAuth, pushToast]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-hero-gradient p-6 text-slate-100">
      <div className="glass-panel max-w-md rounded-3xl p-8 text-center">
        <div className="text-sm uppercase tracking-[0.28em] text-cyan-200/70">OAuth callback</div>
        <h1 className="mt-3 text-2xl font-semibold text-white">{message}</h1>
        <p className="mt-3 text-sm text-slate-400">We’re securely exchanging the short-lived code for session tokens.</p>
      </div>
    </div>
  );
}
