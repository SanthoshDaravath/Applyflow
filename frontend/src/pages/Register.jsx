import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, User, LockKeyhole } from 'lucide-react';
import AuthShell from '../components/AuthShell';
import { Button, Input, Label } from '../components/ui';
import { useAuthStore } from '../store/authStore';
import { useUiStore } from '../store/uiStore';

export default function Register() {
  const navigate = useNavigate();
  const register = useAuthStore((state) => state.register);
  const pushToast = useUiStore((state) => state.pushToast);
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);

  const onSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      await register({
        fullName: form.fullName.trim(),
        email: form.email.trim().toLowerCase(),
        password: form.password
      });
      pushToast({ title: 'Account created', message: 'Your ApplyFlow AI workspace is ready.', type: 'success' });
      navigate('/');
    } catch (error) {
      pushToast({ title: 'Registration failed', message: error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell title="Create your workspace" subtitle="New account">
      <form className="space-y-4" onSubmit={onSubmit}>
        <div>
          <Label>Full name</Label>
          <div className="relative mt-2">
            <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <Input className="pl-10" required minLength={2} autoComplete="name" value={form.fullName} onChange={(e) => setForm((prev) => ({ ...prev, fullName: e.target.value }))} />
          </div>
        </div>
        <div>
          <Label>Email</Label>
          <div className="relative mt-2">
            <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <Input className="pl-10" type="email" required autoComplete="email" value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
          </div>
        </div>
        <div>
          <Label>Password</Label>
          <div className="relative mt-2">
            <LockKeyhole className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <Input className="pl-10" type="password" required minLength={8} autoComplete="new-password" value={form.password} onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} />
          </div>
        </div>
        <Button type="submit" className="w-full" size="lg" disabled={loading}>{loading ? 'Creating…' : 'Create account'}</Button>
        <p className="text-center text-sm text-slate-400">Already have an account? <Link to="/login" className="text-cyan-200 hover:text-cyan-100">Sign in</Link></p>
      </form>
    </AuthShell>
  );
}
