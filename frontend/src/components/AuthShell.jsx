import React from 'react';
import { Sparkles } from 'lucide-react';

export default function AuthShell({ title, subtitle, children }) {
  return (
    <div className="min-h-screen bg-hero-gradient px-4 py-10 text-slate-100">
      <div className="mx-auto grid min-h-[calc(100vh-5rem)] max-w-6xl gap-6 lg:grid-cols-[1.05fr_0.95fr]">
        <div className="glass-panel flex flex-col justify-between rounded-[2rem] p-8 lg:p-10">
          <div>
            <div className="flex items-center gap-3">
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-cyan-400 to-violet-500 text-slate-950">
                <Sparkles className="h-6 w-6" />
              </div>
              <div>
                <div className="text-lg font-semibold text-white">ApplyFlow AI</div>
                <div className="text-xs uppercase tracking-[0.28em] text-slate-400">AI Career OS</div>
              </div>
            </div>
            <div className="mt-10 max-w-xl">
              <div className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">Track every application like a modern startup team.</div>
              <p className="mt-5 max-w-lg text-base leading-7 text-slate-300">Automate Gmail ingestion, score resumes, manage interviews, and turn your job search into a premium analytics workflow.</p>
            </div>
            <div className="mt-10 grid gap-4 sm:grid-cols-3">
              {['AI email classification', 'ATS resume scoring', 'Interview and follow-up automation'].map((item) => (
                <div key={item} className="rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-slate-200">{item}</div>
              ))}
            </div>
          </div>
          <div className="mt-8 rounded-3xl border border-cyan-400/15 bg-cyan-400/8 p-5 text-sm text-cyan-50/80">
            Built for portfolio-worthy SaaS experiences with glassmorphism, dark mode, charts, and clean API-first architecture.
          </div>
        </div>

        <div className="flex items-center justify-center">
          <div className="glass-panel w-full max-w-md rounded-[2rem] p-6 sm:p-8">
            <div className="mb-6">
              <div className="text-xs uppercase tracking-[0.28em] text-cyan-200/60">{subtitle}</div>
              <h1 className="mt-2 text-2xl font-semibold text-white">{title}</h1>
            </div>
            {children}
          </div>
        </div>
      </div>
    </div>
  );
}
