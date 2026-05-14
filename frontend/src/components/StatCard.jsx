import React from 'react';
import { Card, CardContent } from './ui';

export default function StatCard({ title, value, delta, icon: Icon, description }) {
  return (
    <Card className="glass-panel">
      <CardContent className="flex items-start justify-between gap-4 p-5">
        <div>
          <p className="text-sm text-slate-400">{title}</p>
          <div className="mt-2 text-3xl font-semibold text-white">{value}</div>
          <p className="mt-2 text-sm text-slate-400">{description}</p>
          {delta && <div className="mt-3 inline-flex rounded-full bg-emerald-500/15 px-3 py-1 text-xs font-semibold text-emerald-200">{delta}</div>}
        </div>
        {Icon && <div className="rounded-2xl bg-gradient-to-br from-cyan-400/15 to-violet-500/15 p-3 text-cyan-200"><Icon className="h-6 w-6" /></div>}
      </CardContent>
    </Card>
  );
}
