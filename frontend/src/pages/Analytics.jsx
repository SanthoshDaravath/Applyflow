import React, { useEffect, useMemo, useState } from 'react';
import { api } from '../api/client';
import PageHeader from '../components/PageHeader';
import StatCard from '../components/StatCard';
import { PlatformChart, StatusBarChart, TimelineChart } from '../components/AnalyticsCharts';
import { Badge, Card, CardContent, CardHeader, CardTitle } from '../components/ui';
import { BriefcaseBusiness, ChartColumn, Lightbulb, ShieldCheck, TrendingUp } from 'lucide-react';
import { useUiStore } from '../store/uiStore';

export default function Analytics() {
  const pushToast = useUiStore((state) => state.pushToast);
  const [dashboard, setDashboard] = useState(null);

  const load = async () => {
    try {
      const { data } = await api.get('/analytics/dashboard');
      setDashboard(data);
    } catch (error) {
      pushToast({ title: 'Analytics unavailable', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  useEffect(() => {
    load();
  }, []);

  const statusData = useMemo(() => {
    const stats = dashboard?.applicationStats;
    return stats ? [
      { label: 'Saved', value: stats.savedCount },
      { label: 'Applied', value: stats.appliedCount },
      { label: 'Assessment', value: stats.onlineAssessmentCount },
      { label: 'Interview', value: stats.interviewCount },
      { label: 'Offer', value: stats.offerCount },
      { label: 'Rejected', value: stats.rejectedCount }
    ] : [];
  }, [dashboard]);

  return (
    <div className="space-y-6">
      <PageHeader kicker="Analytics engine" title="Performance analytics" description="Understand where your applications convert, what is missing, and how to refine your job search strategy." actions={[<Badge key="live" tone="success">Live metrics</Badge>]} />

      <div className="grid gap-4 xl:grid-cols-4">
        <StatCard title="Applications" value={dashboard?.applicationStats?.totalApplications ?? 0} description="Tracked across all sources" icon={BriefcaseBusiness} />
        <StatCard title="Interview rate" value={`${dashboard?.applicationStats?.interviewRate ?? 0}%`} description="Strong interview signal" icon={TrendingUp} />
        <StatCard title="Offer rate" value={`${dashboard?.applicationStats?.offerRate ?? 0}%`} description="Conversion into offers" icon={ShieldCheck} />
        <StatCard title="Insights" value={dashboard?.insights?.length ?? 0} description="AI-generated recommendations" icon={Lightbulb} />
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <TimelineChart data={dashboard?.applicationTimeline || []} />
        <PlatformChart data={dashboard?.platformBreakdown || []} />
      </div>

      <StatusBarChart data={statusData} />

      <Card className="glass-panel">
        <CardHeader><CardTitle>Platform insights</CardTitle></CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {(dashboard?.insights || []).map((insight) => (
            <div key={insight.id} className="rounded-2xl border border-white/10 bg-slate-950/50 p-4">
              <div className="flex items-center justify-between gap-2">
                <div className="font-semibold text-white">{insight.title}</div>
                <Badge tone="purple">{Math.round((insight.confidence || 0) * 100)}%</Badge>
              </div>
              <p className="mt-2 text-sm text-slate-400">{insight.summary}</p>
              <ul className="mt-3 space-y-2 text-sm text-slate-300">
                {(insight.recommendations || []).slice(0, 4).map((rec) => <li key={rec} className="rounded-xl bg-cyan-400/10 px-3 py-2">{rec}</li>)}
              </ul>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
