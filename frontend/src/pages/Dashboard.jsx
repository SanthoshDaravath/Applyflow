import React, { useEffect, useMemo, useState } from 'react';
import { BriefcaseBusiness, CircleCheckBig, Mail, Sparkles, TrendingUp, CalendarDays } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import KanbanBoard from '../components/KanbanBoard';
import { PlatformChart, StatusBarChart, TimelineChart } from '../components/AnalyticsCharts';
import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from '../components/ui';
import { currency, dateTime } from '../lib/format';
import { useUiStore } from '../store/uiStore';

export default function Dashboard() {
  const navigate = useNavigate();
  const pushToast = useUiStore((state) => state.pushToast);
  const [dashboard, setDashboard] = useState(null);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const [dashRes, appsRes] = await Promise.all([api.get('/analytics/dashboard'), api.get('/applications?size=100')]);
      setDashboard(dashRes.data);
      setApplications(appsRes.data.items || []);
    } catch (error) {
      pushToast({ title: 'Dashboard unavailable', message: error.response?.data?.message || error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const statusMix = useMemo(() => {
    const stats = dashboard?.applicationStats;
    if (!stats) return [];
    return [
      { label: 'Saved', value: stats.savedCount },
      { label: 'Applied', value: stats.appliedCount },
      { label: 'Assessment', value: stats.onlineAssessmentCount },
      { label: 'Interview', value: stats.interviewCount },
      { label: 'Offer', value: stats.offerCount },
      { label: 'Rejected', value: stats.rejectedCount }
    ];
  }, [dashboard]);

  const onMove = async (id, status) => {
    try {
      await api.patch(`/applications/${id}/status`, { status });
      await load();
    } catch (error) {
      pushToast({ title: 'Status update failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  if (loading) {
    return <div className="rounded-3xl border border-white/10 bg-white/5 p-10 text-slate-300">Loading dashboard…</div>;
  }

  const stats = dashboard?.applicationStats;
  return (
    <div className="space-y-6">
      <PageHeader
        kicker="Command center"
        title="Career dashboard"
        description="Monitor applications, interview performance, and AI-generated career signals in a premium Linear-style workspace."
        actions={[
          <Badge key="ai" tone="info">AI insights enabled</Badge>,
          <Badge key="sync" tone="success">Gmail sync ready</Badge>
        ]}
      />

      <div className="grid gap-4 xl:grid-cols-5">
        <StatCard title="Total applications" value={stats?.totalApplications ?? 0} description="Across all connected platforms" icon={BriefcaseBusiness} />
        <StatCard title="Interview rate" value={`${stats?.interviewRate ?? 0}%`} description="Interviews from total applications" icon={TrendingUp} />
        <StatCard title="Offer rate" value={`${stats?.offerRate ?? 0}%`} description="Offers from total applications" icon={CircleCheckBig} />
        <StatCard title="Upcoming interviews" value={dashboard?.upcomingInterviews?.length ?? 0} description="Scheduled across active pipelines" icon={CalendarDays} />
        <StatCard title="AI insights" value={dashboard?.insights?.length ?? 0} description="Automated recommendations generated" icon={Sparkles} />
      </div>

      <Card className="glass-panel">
        <CardHeader><CardTitle>Pipeline quick access</CardTitle></CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <Button variant="secondary" className="justify-between" onClick={() => navigate('/applications?status=APPLIED')}>
            <span>Applied</span>
            <Badge tone="info">{stats?.appliedCount ?? 0}</Badge>
          </Button>
          <Button variant="secondary" className="justify-between" onClick={() => navigate('/applications?status=ONLINE_ASSESSMENT')}>
            <span>Shortlisted</span>
            <Badge tone="purple">{stats?.onlineAssessmentCount ?? 0}</Badge>
          </Button>
          <Button variant="secondary" className="justify-between" onClick={() => navigate('/applications?status=INTERVIEW')}>
            <span>Interview</span>
            <Badge tone="info">{stats?.interviewCount ?? 0}</Badge>
          </Button>
          <Button variant="secondary" className="justify-between" onClick={() => navigate('/applications?status=REJECTED')}>
            <span>Rejected</span>
            <Badge tone="danger">{stats?.rejectedCount ?? 0}</Badge>
          </Button>
        </CardContent>
      </Card>

      <div className="grid gap-4 xl:grid-cols-3">
        <div className="xl:col-span-2 space-y-4">
          <TimelineChart data={dashboard?.applicationTimeline || []} />
          <StatusBarChart data={statusMix} />
        </div>
        <PlatformChart data={dashboard?.platformBreakdown || []} />
      </div>

      <Card className="glass-panel">
        <CardHeader><CardTitle>Kanban board</CardTitle></CardHeader>
        <CardContent className="overflow-x-auto p-3">
          <KanbanBoard applications={applications} onMove={onMove} />
        </CardContent>
      </Card>

      <div className="grid gap-4 xl:grid-cols-2">
        <Card className="glass-panel">
          <CardHeader><CardTitle>AI insights</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {(dashboard?.insights || []).map((insight) => (
              <div key={insight.id} className="rounded-2xl border border-white/10 bg-slate-950/50 p-4">
                <div className="flex items-center justify-between gap-3">
                  <div className="font-semibold text-white">{insight.title}</div>
                  <Badge tone="purple">{Math.round((insight.confidence || 0) * 100)}%</Badge>
                </div>
                <p className="mt-2 text-sm text-slate-400">{insight.summary}</p>
                <div className="mt-3 flex flex-wrap gap-2 text-xs text-cyan-100">
                  {(insight.recommendations || []).slice(0, 3).map((recommendation) => <span key={recommendation} className="rounded-full bg-cyan-400/10 px-3 py-1">{recommendation}</span>)}
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
        <Card className="glass-panel">
          <CardHeader><CardTitle>Upcoming interviews</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {(dashboard?.upcomingInterviews || []).length === 0 && <div className="text-sm text-slate-400">No upcoming interviews. Keep applying and follow up consistently.</div>}
            {(dashboard?.upcomingInterviews || []).map((interview) => (
              <div key={interview.id} className="rounded-2xl border border-white/10 bg-slate-950/50 p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="font-semibold text-white">{interview.company} · {interview.roundName}</div>
                    <div className="text-sm text-slate-400">{interview.role}</div>
                  </div>
                  <Badge tone={interview.status === 'SCHEDULED' ? 'info' : 'default'}>{interview.status}</Badge>
                </div>
                <div className="mt-2 text-sm text-slate-400">{dateTime(interview.scheduledAt)} · {interview.location || 'Remote'}</div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
