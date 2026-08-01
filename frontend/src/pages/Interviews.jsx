import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import PageHeader from '../components/PageHeader';
import { Badge, Button, Card, CardContent, CardHeader, CardTitle, Input, Label, Select, Textarea } from '../components/ui';
import { dateTime } from '../lib/format';
import { useUiStore } from '../store/uiStore';

export default function Interviews() {
  const pushToast = useUiStore((state) => state.pushToast);
  const [interviews, setInterviews] = useState([]);
  const [applications, setApplications] = useState([]);
  const [form, setForm] = useState({ applicationId: '', roundName: 'Recruiter Screen', scheduledAt: '', location: 'Google Meet', interviewType: 'Video', feedback: '', notes: '' });

  const load = async () => {
    try {
      const [interviewRes, appRes] = await Promise.all([api.get('/interviews/upcoming'), api.get('/applications?size=100')]);
      setInterviews(interviewRes.data || []);
      setApplications(appRes.data.items || []);
      if (!form.applicationId && appRes.data.items?.[0]?.id) {
        setForm((prev) => ({ ...prev, applicationId: appRes.data.items[0].id }));
      }
    } catch (error) {
      pushToast({ title: 'Interviews unavailable', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  useEffect(() => { load(); }, []);

  const create = async (event) => {
    event.preventDefault();
    try {
      await api.post('/interviews', {
        ...form,
        scheduledAt: form.scheduledAt
      });
      pushToast({ title: 'Interview saved', message: 'Reminder workflow is ready.', type: 'success' });
      load();
    } catch (error) {
      pushToast({ title: 'Save failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader kicker="Interview workflow" title="Interview tracker" description="Keep rounds, schedules, feedback, and prep notes in one place with reminder automation." />

      <div className="grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
        <Card className="glass-panel">
          <CardHeader><CardTitle>Add interview</CardTitle></CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={create}>
              <div>
                <Label>Application</Label>
                <Select className="mt-2" value={form.applicationId} onChange={(e) => setForm((prev) => ({ ...prev, applicationId: e.target.value }))}>
                  {applications.map((application) => <option key={application.id} value={application.id}>{application.company} · {application.role}</option>)}
                </Select>
              </div>
              <div>
                <Label>Round name</Label>
                <Input className="mt-2" value={form.roundName} onChange={(e) => setForm((prev) => ({ ...prev, roundName: e.target.value }))} />
              </div>
              <div>
                <Label>Schedule</Label>
                <Input className="mt-2" type="datetime-local" required value={form.scheduledAt} onChange={(e) => setForm((prev) => ({ ...prev, scheduledAt: e.target.value }))} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Location</Label>
                  <Input className="mt-2" value={form.location} onChange={(e) => setForm((prev) => ({ ...prev, location: e.target.value }))} />
                </div>
                <div>
                  <Label>Type</Label>
                  <Input className="mt-2" value={form.interviewType} onChange={(e) => setForm((prev) => ({ ...prev, interviewType: e.target.value }))} />
                </div>
              </div>
              <div>
                <Label>Notes</Label>
                <Textarea className="mt-2" value={form.notes} onChange={(e) => setForm((prev) => ({ ...prev, notes: e.target.value }))} />
              </div>
              <Button type="submit" className="w-full">Save interview</Button>
            </form>
          </CardContent>
        </Card>

        <Card className="glass-panel">
          <CardHeader><CardTitle>Upcoming interviews</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {interviews.map((interview) => (
              <div key={interview.id} className="rounded-3xl border border-white/10 bg-slate-950/50 p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="font-semibold text-white">{interview.company} · {interview.roundName}</div>
                    <div className="text-sm text-slate-400">{interview.role}</div>
                  </div>
                  <Badge tone={interview.status === 'SCHEDULED' ? 'info' : 'default'}>{interview.status}</Badge>
                </div>
                <div className="mt-3 text-sm text-slate-400">{dateTime(interview.scheduledAt)} · {interview.location || 'Remote'}</div>
                {interview.notes && <p className="mt-3 text-sm text-slate-300">{interview.notes}</p>}
              </div>
            ))}
            {interviews.length === 0 && <div className="text-sm text-slate-400">No interviews scheduled yet.</div>}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
