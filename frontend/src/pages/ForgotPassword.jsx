import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail } from 'lucide-react';
import AuthShell from '../components/AuthShell';
import { Button, Input, Label } from '../components/ui';
import { api } from '../api/client';
import { useUiStore } from '../store/uiStore';

export default function ForgotPassword() {
  const pushToast = useUiStore((state) => state.pushToast);
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);

  const onSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email });
      pushToast({ title: 'Reset code generated', message: 'Check your configured mail flow or logs for the reset token.', type: 'success' });
    } catch (error) {
      pushToast({ title: 'Reset failed', message: error.response?.data?.message || error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell title="Reset your password" subtitle="Account recovery">
      <form className="space-y-4" onSubmit={onSubmit}>
        <div>
          <Label>Email</Label>
          <div className="relative mt-2">
            <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <Input className="pl-10" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
        </div>
        <Button type="submit" className="w-full" size="lg" disabled={loading}>{loading ? 'Sending…' : 'Send reset code'}</Button>
        <p className="text-center text-sm text-slate-400"><Link to="/login" className="text-cyan-200 hover:text-cyan-100">Back to login</Link></p>
      </form>
    </AuthShell>
  );
}
