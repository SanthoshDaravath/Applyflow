import React from 'react';
import PageHeader from '../components/PageHeader';
import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from '../components/ui';
import { useUiStore } from '../store/uiStore';

export default function Settings() {
  const theme = useUiStore((state) => state.theme);
  const toggleTheme = useUiStore((state) => state.toggleTheme);

  return (
    <div className="space-y-6">
      <PageHeader kicker="Workspace settings" title="Settings" description="Manage theme, integrations, notification preferences, and platform readiness." />
      <div className="grid gap-4 xl:grid-cols-2">
        <Card className="glass-panel">
          <CardHeader><CardTitle>Appearance</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="rounded-3xl border border-white/10 bg-slate-950/50 p-4">
              <div className="text-sm font-semibold text-white">Theme</div>
              <div className="mt-2 text-sm text-slate-400">Current mode: {theme}</div>
            </div>
            <Button onClick={toggleTheme}>{theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}</Button>
          </CardContent>
        </Card>

        <Card className="glass-panel">
          <CardHeader><CardTitle>Integrations</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            {[
              'Google OAuth2',
              'Gmail ingestion',
              'LinkedIn Chrome extension API',
              'Telegram/WhatsApp notifications',
              'Redis and RabbitMQ worker queue'
            ].map((item) => (
              <div key={item} className="flex items-center justify-between rounded-2xl border border-white/10 bg-slate-950/50 px-4 py-3">
                <span className="text-sm text-slate-200">{item}</span>
                <Badge tone="success">Ready</Badge>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
