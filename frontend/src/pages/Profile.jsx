import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import PageHeader from '../components/PageHeader';
import { Avatar, Badge, Card, CardContent, CardHeader, CardTitle } from '../components/ui';
import { useAuthStore } from '../store/authStore';

export default function Profile() {
  const user = useAuthStore((state) => state.user);
  const [stats, setStats] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const { data } = await api.get('/applications/stats');
        setStats(data);
      } catch {
        setStats(null);
      }
    })();
  }, []);

  return (
    <div className="space-y-6">
      <PageHeader kicker="User profile" title="Profile" description="Your identity, permissions, and high-level progress at a glance." />
      <div className="grid gap-4 xl:grid-cols-[0.85fr_1.15fr]">
        <Card className="glass-panel">
          <CardContent className="flex flex-col items-center gap-4 p-8 text-center">
            <Avatar className="h-20 w-20 text-lg">{(user?.fullName || 'AF').split(' ').map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</Avatar>
            <div>
              <div className="text-2xl font-semibold text-white">{user?.fullName}</div>
              <div className="mt-1 text-slate-400">{user?.email}</div>
            </div>
            <Badge tone={user?.role === 'ADMIN' ? 'purple' : 'info'}>{user?.role}</Badge>
          </CardContent>
        </Card>

        <Card className="glass-panel">
          <CardHeader><CardTitle>Performance snapshot</CardTitle></CardHeader>
          <CardContent className="grid gap-4 sm:grid-cols-2">
            {[
              ['Total applications', stats?.totalApplications ?? 0],
              ['Interviews', stats?.interviewCount ?? 0],
              ['Offers', stats?.offerCount ?? 0],
              ['Rejections', stats?.rejectedCount ?? 0]
            ].map(([label, value]) => (
              <div key={label} className="rounded-3xl border border-white/10 bg-slate-950/50 p-5">
                <div className="text-sm text-slate-400">{label}</div>
                <div className="mt-2 text-3xl font-semibold text-white">{value}</div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
