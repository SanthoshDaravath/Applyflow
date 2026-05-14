import React, { useMemo, useState } from 'react';
import { Badge, Card, CardContent, CardHeader, CardTitle } from './ui';
import { GripVertical } from 'lucide-react';

const statuses = ['SAVED', 'APPLIED', 'ONLINE_ASSESSMENT', 'INTERVIEW', 'OFFER', 'REJECTED'];
const toneByStatus = {
  SAVED: 'default',
  APPLIED: 'info',
  ONLINE_ASSESSMENT: 'warning',
  INTERVIEW: 'purple',
  OFFER: 'success',
  REJECTED: 'danger'
};

export default function KanbanBoard({ applications = [], onMove }) {
  const [dragId, setDragId] = useState(null);
  const grouped = useMemo(() => Object.fromEntries(statuses.map((status) => [status, applications.filter((item) => item.status === status)])), [applications]);

  return (
    <div className="grid gap-4 xl:grid-cols-6">
      {statuses.map((status) => (
        <Card key={status} className="min-h-[320px] bg-white/4">
          <CardHeader>
            <div className="flex items-center justify-between gap-2">
              <CardTitle className="text-sm uppercase tracking-[0.25em] text-slate-300">{status.replaceAll('_', ' ')}</CardTitle>
              <Badge tone={toneByStatus[status]}>{grouped[status].length}</Badge>
            </div>
          </CardHeader>
          <CardContent
            className="space-y-3 p-3"
            onDragOver={(e) => e.preventDefault()}
            onDrop={() => dragId && onMove?.(dragId, status)}
          >
            {grouped[status].map((app) => (
              <article
                key={app.id}
                draggable
                onDragStart={() => setDragId(app.id)}
                onDragEnd={() => setDragId(null)}
                className="cursor-grab rounded-2xl border border-white/10 bg-slate-950/60 p-4 shadow-lg shadow-black/10 transition hover:-translate-y-0.5 hover:border-cyan-400/30"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="font-semibold text-white">{app.company}</div>
                    <div className="text-sm text-slate-400">{app.role}</div>
                  </div>
                  <GripVertical className="h-4 w-4 text-slate-500" />
                </div>
                <div className="mt-3 flex flex-wrap gap-2 text-xs text-slate-400">
                  <Badge tone={toneByStatus[status]}>{status.replaceAll('_', ' ')}</Badge>
                  {app.sourcePlatform && <span>{app.sourcePlatform}</span>}
                </div>
                {app.notes && <p className="mt-3 line-clamp-3 text-sm text-slate-400">{app.notes}</p>}
              </article>
            ))}
            {grouped[status].length === 0 && <div className="rounded-2xl border border-dashed border-white/10 px-4 py-10 text-center text-sm text-slate-500">Drop items here</div>}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
