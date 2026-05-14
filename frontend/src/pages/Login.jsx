import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, LockKeyhole, Mail } from 'lucide-react';
import AuthShell from '../components/AuthShell';
import { Button, Input, Label } from '../components/ui';
import { useAuthStore } from '../store/authStore';
import { useUiStore } from '../store/uiStore';

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const login = useAuthStore((state) => state.login);
  const pushToast = useUiStore((state) => state.pushToast);
  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
  const oauthUrl = `${apiBase.replace(/\/api\/v1$/, '')}/oauth2/authorization/google`;
  const [form, setForm] = useState({ email: 'demo@applyflow.ai', password: 'Password123!' });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      await login(form.email, form.password);
      pushToast({ title: 'Welcome back', message: 'Your dashboard is ready.', type: 'success' });
      navigate(location.state?.from?.pathname || '/');
    } catch (error) {
      pushToast({ title: 'Login failed', message: error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell title="Sign in to ApplyFlow AI" subtitle="Secure authentication">
      <form className="space-y-4" onSubmit={onSubmit}>
        <div>
          <Label>Email</Label>
          <div className="relative mt-2">
            <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <Input className="pl-10" type="email" value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
          </div>
        </div>
        <div>
          <Label>Password</Label>
          <div className="relative mt-2">
            <LockKeyhole className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <Input className="pl-10 pr-12" type={showPassword ? 'text' : 'password'} value={form.password} onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} />
            <button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-white" onClick={() => setShowPassword((value) => !value)}>
              {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
        </div>
        <div className="flex items-center justify-between text-sm text-slate-400">
          <Link to="/forgot-password" className="hover:text-cyan-200">Forgot password?</Link>
          <Link to="/register" className="hover:text-cyan-200">Create account</Link>
        </div>
        <Button type="submit" className="w-full" size="lg" disabled={loading}>
          {loading ? 'Signing in…' : 'Sign in'}
        </Button>
        <Button type="button" variant="secondary" className="w-full" size="lg" onClick={() => (window.location.href = oauthUrl)}>
          Continue with Google
        </Button>
        <p className="text-center text-xs text-slate-500">Demo credentials: <span className="text-slate-300">demo@applyflow.ai / Password123!</span></p>
      </form>
    </AuthShell>
  );
}
