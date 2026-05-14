import React from 'react';
import { Area, AreaChart, Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from './ui';

const COLORS = ['#67e8f9', '#a78bfa', '#f472b6', '#f59e0b', '#34d399', '#fb7185'];

export function TimelineChart({ data = [] }) {
  return (
    <Card className="glass-panel">
      <CardHeader><CardTitle>Applications timeline</CardTitle></CardHeader>
      <CardContent className="h-72 p-2 sm:p-5">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data}>
            <defs>
              <linearGradient id="timelineFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#67e8f9" stopOpacity={0.45} />
                <stop offset="95%" stopColor="#67e8f9" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.15)" />
            <XAxis dataKey="label" stroke="#94a3b8" />
            <YAxis stroke="#94a3b8" />
            <Tooltip contentStyle={{ background: '#020617', border: '1px solid rgba(255,255,255,0.12)', borderRadius: 16 }} />
            <Area type="monotone" dataKey="value" stroke="#67e8f9" fill="url(#timelineFill)" strokeWidth={2.5} />
          </AreaChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}

export function PlatformChart({ data = [] }) {
  return (
    <Card className="glass-panel">
      <CardHeader><CardTitle>Platforms</CardTitle></CardHeader>
      <CardContent className="h-72 p-2 sm:p-5">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={data} dataKey="value" nameKey="label" innerRadius={55} outerRadius={92} paddingAngle={3}>
              {data.map((entry, index) => <Cell key={entry.label} fill={COLORS[index % COLORS.length]} />)}
            </Pie>
            <Tooltip contentStyle={{ background: '#020617', border: '1px solid rgba(255,255,255,0.12)', borderRadius: 16 }} />
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}

export function StatusBarChart({ data = [] }) {
  return (
    <Card className="glass-panel">
      <CardHeader><CardTitle>Status mix</CardTitle></CardHeader>
      <CardContent className="h-72 p-2 sm:p-5">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.15)" />
            <XAxis dataKey="label" stroke="#94a3b8" />
            <YAxis stroke="#94a3b8" />
            <Tooltip contentStyle={{ background: '#020617', border: '1px solid rgba(255,255,255,0.12)', borderRadius: 16 }} />
            <Bar dataKey="value" fill="#a78bfa" radius={[12, 12, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
