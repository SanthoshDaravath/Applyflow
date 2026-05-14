import React, { useEffect } from 'react';
import { X } from 'lucide-react';
import { useUiStore } from '../store/uiStore';

export default function ToastHost() {
  const toasts = useUiStore((state) => state.toasts);
  const dismissToast = useUiStore((state) => state.dismissToast);

  useEffect(() => {
    const timers = toasts.map((toast) => setTimeout(() => dismissToast(toast.id), 4000));
    return () => timers.forEach(clearTimeout);
  }, [toasts, dismissToast]);

  return (
    <div className="fixed right-4 top-4 z-[100] flex w-[min(420px,calc(100vw-2rem))] flex-col gap-3">
      {toasts.map((toast) => (
        <div key={toast.id} className="glass-panel flex items-start gap-3 rounded-2xl p-4 text-sm text-slate-200">
          <div className={`mt-1 h-2.5 w-2.5 rounded-full ${toast.type === 'success' ? 'bg-emerald-400' : toast.type === 'error' ? 'bg-rose-400' : 'bg-cyan-400'}`} />
          <div className="flex-1">
            <div className="font-semibold text-white">{toast.title}</div>
            <div className="mt-1 text-slate-400">{toast.message}</div>
          </div>
          <button onClick={() => dismissToast(toast.id)} className="rounded-lg p-1 text-slate-400 hover:bg-white/8 hover:text-white">
            <X className="h-4 w-4" />
          </button>
        </div>
      ))}
    </div>
  );
}
