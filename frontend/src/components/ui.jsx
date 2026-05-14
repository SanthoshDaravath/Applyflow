import React from 'react';

const base = 'rounded-2xl border border-white/10 bg-white/5 text-slate-100 shadow-glass backdrop-blur-xl transition focus:outline-none focus:ring-2 focus:ring-cyan-400/40';

export const Button = ({ className = '', variant = 'primary', size = 'md', ...props }) => {
  const variants = {
    primary: 'bg-gradient-to-r from-cyan-400 to-violet-500 text-slate-950 hover:opacity-90',
    secondary: 'bg-white/8 hover:bg-white/12 text-slate-100',
    ghost: 'border-transparent bg-transparent hover:bg-white/8 text-slate-200',
    danger: 'bg-rose-500/15 text-rose-200 hover:bg-rose-500/25'
  };
  const sizes = {
    sm: 'h-9 px-3 text-sm',
    md: 'h-11 px-4 text-sm',
    lg: 'h-12 px-5 text-base'
  };
  return <button className={`${base} inline-flex items-center justify-center gap-2 font-medium ${variants[variant]} ${sizes[size]} ${className}`} {...props} />;
};

export const Card = ({ className = '', ...props }) => <div className={`${base} ${className}`} {...props} />;
export const CardHeader = ({ className = '', ...props }) => <div className={`border-b border-white/10 p-5 ${className}`} {...props} />;
export const CardTitle = ({ className = '', ...props }) => <h3 className={`text-base font-semibold text-white ${className}`} {...props} />;
export const CardDescription = ({ className = '', ...props }) => <p className={`mt-1 text-sm text-slate-400 ${className}`} {...props} />;
export const CardContent = ({ className = '', ...props }) => <div className={`p-5 ${className}`} {...props} />;

export const Input = ({ className = '', ...props }) => <input className={`${base} h-11 w-full px-4 text-sm placeholder:text-slate-500 ${className}`} {...props} />;
export const Textarea = ({ className = '', ...props }) => <textarea className={`${base} min-h-[120px] w-full px-4 py-3 text-sm placeholder:text-slate-500 ${className}`} {...props} />;
export const Select = ({ className = '', children, ...props }) => <select className={`${base} h-11 w-full px-4 text-sm ${className}`} {...props}>{children}</select>;
export const Label = ({ className = '', ...props }) => <label className={`text-sm font-medium text-slate-300 ${className}`} {...props} />;
export const Badge = ({ className = '', tone = 'default', ...props }) => {
  const tones = {
    default: 'bg-white/10 text-slate-200',
    success: 'bg-emerald-500/15 text-emerald-200',
    warning: 'bg-amber-500/15 text-amber-200',
    danger: 'bg-rose-500/15 text-rose-200',
    info: 'bg-cyan-500/15 text-cyan-200',
    purple: 'bg-violet-500/15 text-violet-200'
  };
  return <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] ${tones[tone]} ${className}`} {...props} />;
};

export const Skeleton = ({ className = '' }) => <div className={`animate-pulse rounded-2xl bg-white/8 ${className}`} />;

export const Avatar = ({ children, className = '' }) => (
  <div className={`flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-cyan-400/20 to-violet-500/20 text-sm font-bold text-cyan-100 ${className}`}>{children}</div>
);

export const Divider = () => <div className="h-px w-full bg-white/10" />;
