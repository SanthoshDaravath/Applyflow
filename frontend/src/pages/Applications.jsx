import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api/client';
import PageHeader from '../components/PageHeader';
import KanbanBoard from '../components/KanbanBoard';
import { Badge, Button, Card, CardContent, CardHeader, CardTitle, Input, Label, Select, Textarea } from '../components/ui';
import { useUiStore } from '../store/uiStore';
import { currency, dateOnly } from '../lib/format';

const statuses = ['SAVED', 'APPLIED', 'ONLINE_ASSESSMENT', 'INTERVIEW', 'OFFER', 'REJECTED'];

export default function Applications() {
  const [searchParams, setSearchParams] = useSearchParams();
  const pushToast = useUiStore((state) => state.pushToast);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(8);
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState(searchParams.get('status') || '');
  const [sortBy, setSortBy] = useState('created');
  const [result, setResult] = useState({ items: [], totalPages: 0, totalElements: 0 });
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ company: '', role: '', salary: '', location: '', sourcePlatform: 'LinkedIn', applicationDate: new Date().toISOString().slice(0, 10), status: 'SAVED', notes: '', jobUrl: '', resumeSnapshot: '' });

  const load = async () => {
    try {
      const { data } = await api.get('/applications', { params: { q: query || undefined, status: status || undefined, page, size } });
      let items = data.items || [];
      if (sortBy === 'company') items = [...items].sort((a, b) => a.company.localeCompare(b.company));
      if (sortBy === 'role') items = [...items].sort((a, b) => a.role.localeCompare(b.role));
      setResult({ ...data, items });
    } catch (error) {
      pushToast({ title: 'Applications failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  useEffect(() => {
    load();
  }, [page, size, status, sortBy]);

  useEffect(() => {
    const urlStatus = searchParams.get('status') || '';
    if (urlStatus !== status) {
      setStatus(urlStatus);
      setPage(0);
    }
  }, [searchParams]);

  const onSearch = async (event) => {
    event.preventDefault();
    setPage(0);
    await load();
  };

  const create = async (event) => {
    event.preventDefault();
    try {
      const payload = {
        ...form,
        salary: form.salary ? Number(form.salary) : null
      };
      if (editingId) {
        await api.put(`/applications/${editingId}`, payload);
        pushToast({ title: 'Application updated', message: `${form.company} was updated successfully.`, type: 'success' });
      } else {
        await api.post('/applications', payload);
        pushToast({ title: 'Application saved', message: `${form.company} was added to your tracker.`, type: 'success' });
      }
      setEditingId(null);
      setForm({ company: '', role: '', salary: '', location: '', sourcePlatform: 'LinkedIn', applicationDate: new Date().toISOString().slice(0, 10), status: 'SAVED', notes: '', jobUrl: '', resumeSnapshot: '' });
      load();
    } catch (error) {
      pushToast({ title: 'Save failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  const startEdit = (item) => {
    setEditingId(item.id);
    setForm({
      company: item.company || '',
      role: item.role || '',
      salary: item.salary?.toString?.() || '',
      location: item.location || '',
      sourcePlatform: item.sourcePlatform || 'LinkedIn',
      applicationDate: item.applicationDate || new Date().toISOString().slice(0, 10),
      status: item.status || 'SAVED',
      notes: item.notes || '',
      jobUrl: item.jobUrl || '',
      resumeSnapshot: item.resumeSnapshot || ''
    });
  };

  const onMove = async (id, nextStatus) => {
    try {
      await api.patch(`/applications/${id}/status`, { status: nextStatus });
      pushToast({ title: 'Status updated', message: `Moved application to ${nextStatus}.`, type: 'success' });
      load();
    } catch (error) {
      pushToast({ title: 'Update failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  const remove = async (id) => {
    try {
      await api.delete(`/applications/${id}`);
      load();
    } catch (error) {
      pushToast({ title: 'Delete failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  const totalPages = result.totalPages || 1;

  return (
    <div className="space-y-6">
      <PageHeader kicker="Application ops" title="Applications" description="Create, edit, search, sort, and drag applications through your pipeline." actions={[
        <Badge key="count" tone="info">{result.totalElements} tracked</Badge>
      ]} />

      <div className="grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
        <Card className="glass-panel">
          <CardHeader><CardTitle>{editingId ? 'Edit application' : 'New application'}</CardTitle></CardHeader>
          <CardContent>
            <form className="grid gap-4 sm:grid-cols-2" onSubmit={create}>
              {['company', 'role', 'location', 'jobUrl'].map((field) => (
                <div key={field} className={field === 'jobUrl' ? 'sm:col-span-2' : ''}>
                  <Label className="capitalize">{field}</Label>
                  <Input className="mt-2" value={form[field]} onChange={(e) => setForm((prev) => ({ ...prev, [field]: e.target.value }))} />
                </div>
              ))}
              <div>
                <Label>Salary</Label>
                <Input className="mt-2" type="number" value={form.salary} onChange={(e) => setForm((prev) => ({ ...prev, salary: e.target.value }))} />
              </div>
              <div>
                <Label>Date</Label>
                <Input className="mt-2" type="date" value={form.applicationDate} onChange={(e) => setForm((prev) => ({ ...prev, applicationDate: e.target.value }))} />
              </div>
              <div>
                <Label>Source</Label>
                <Select className="mt-2" value={form.sourcePlatform} onChange={(e) => setForm((prev) => ({ ...prev, sourcePlatform: e.target.value }))}>
                  {['LinkedIn', 'Gmail', 'Indeed', 'Naukri', 'Wellfound', 'Greenhouse', 'Lever', 'Manual'].map((platform) => <option key={platform} value={platform}>{platform}</option>)}
                </Select>
              </div>
              <div>
                <Label>Status</Label>
                <Select className="mt-2" value={form.status} onChange={(e) => setForm((prev) => ({ ...prev, status: e.target.value }))}>
                  {statuses.map((value) => <option key={value} value={value}>{value.replaceAll('_', ' ')}</option>)}
                </Select>
              </div>
              <div className="sm:col-span-2">
                <Label>Notes</Label>
                <Textarea className="mt-2" value={form.notes} onChange={(e) => setForm((prev) => ({ ...prev, notes: e.target.value }))} />
              </div>
              <div className="sm:col-span-2 flex justify-end">
                <div className="flex gap-3">
                  {editingId && <Button type="button" variant="ghost" onClick={() => { setEditingId(null); setForm({ company: '', role: '', salary: '', location: '', sourcePlatform: 'LinkedIn', applicationDate: new Date().toISOString().slice(0, 10), status: 'SAVED', notes: '', jobUrl: '', resumeSnapshot: '' }); }}>Cancel</Button>}
                  <Button type="submit">{editingId ? 'Update application' : 'Save application'}</Button>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        <Card className="glass-panel">
          <CardHeader><CardTitle>Search and filters</CardTitle></CardHeader>
          <CardContent>
            <form className="grid gap-4 sm:grid-cols-3" onSubmit={onSearch}>
              <div className="sm:col-span-2">
                <Label>Search</Label>
                <Input className="mt-2" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Company, role, or notes" />
              </div>
              <div>
                <Label>Status</Label>
                <Select
                  className="mt-2"
                  value={status}
                  onChange={(e) => {
                    const nextStatus = e.target.value;
                    setStatus(nextStatus);
                    setPage(0);
                    if (nextStatus) {
                      setSearchParams({ status: nextStatus });
                    } else {
                      setSearchParams({});
                    }
                  }}
                >
                  <option value="">All</option>
                  {statuses.map((value) => <option key={value} value={value}>{value.replaceAll('_', ' ')}</option>)}
                </Select>
              </div>
              <div>
                <Label>Sort</Label>
                <Select className="mt-2" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                  <option value="created">Newest</option>
                  <option value="company">Company</option>
                  <option value="role">Role</option>
                </Select>
              </div>
              <div>
                <Label>Page size</Label>
                <Select className="mt-2" value={size} onChange={(e) => setSize(Number(e.target.value))}>
                  {[5, 8, 10, 20].map((item) => <option key={item} value={item}>{item}</option>)}
                </Select>
              </div>
              <div className="sm:col-span-3">
                <Button type="submit" className="w-full">Apply filters</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>

      <Card className="glass-panel overflow-hidden">
        <CardHeader><CardTitle>Applications list</CardTitle></CardHeader>
        <CardContent className="overflow-x-auto p-0">
          <table className="min-w-full divide-y divide-white/10 text-sm">
            <thead className="bg-white/5 text-slate-300">
              <tr>
                {['Company', 'Role', 'Salary', 'Platform', 'Status', 'Date', 'Actions'].map((head) => <th key={head} className="px-4 py-3 text-left font-medium">{head}</th>)}
              </tr>
            </thead>
            <tbody className="divide-y divide-white/10">
              {result.items.map((item) => (
                <tr key={item.id} className="bg-slate-950/40">
                  <td className="px-4 py-4 text-white">{item.company}</td>
                  <td className="px-4 py-4 text-slate-300">{item.role}</td>
                  <td className="px-4 py-4 text-slate-300">{item.salary ? currency(item.salary) : '—'}</td>
                  <td className="px-4 py-4 text-slate-300">{item.sourcePlatform}</td>
                  <td className="px-4 py-4"><Badge tone={item.status === 'OFFER' ? 'success' : item.status === 'REJECTED' ? 'danger' : 'info'}>{item.status}</Badge></td>
                  <td className="px-4 py-4 text-slate-300">{dateOnly(item.applicationDate)}</td>
                  <td className="px-4 py-4">
                    <div className="flex gap-2">
                      <Button size="sm" variant="secondary" onClick={() => startEdit(item)}>Edit</Button>
                      <Button size="sm" variant="secondary" onClick={() => onMove(item.id, 'INTERVIEW')}>Interview</Button>
                      <Button size="sm" variant="ghost" onClick={() => remove(item.id)}>Delete</Button>
                    </div>
                  </td>
                </tr>
              ))}
              {result.items.length === 0 && (
                <tr>
                  <td colSpan="7" className="px-4 py-12 text-center text-slate-400">No applications found for the selected filters.</td>
                </tr>
              )}
            </tbody>
          </table>
        </CardContent>
      </Card>

      <div className="flex items-center justify-between text-sm text-slate-400">
        <div>Page {page + 1} of {totalPages}</div>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage((value) => Math.max(0, value - 1))}>Previous</Button>
          <Button variant="secondary" size="sm" disabled={page + 1 >= totalPages} onClick={() => setPage((value) => value + 1)}>Next</Button>
        </div>
      </div>

      <Card className="glass-panel">
        <CardHeader><CardTitle>Drag and drop pipeline</CardTitle></CardHeader>
        <CardContent className="overflow-x-auto p-3">
          <KanbanBoard applications={result.items} onMove={onMove} />
        </CardContent>
      </Card>
    </div>
  );
}
