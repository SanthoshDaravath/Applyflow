import React, { useState } from 'react';
import { api } from '../api/client';
import PageHeader from '../components/PageHeader';
import { Badge, Button, Card, CardContent, CardHeader, CardTitle, Input, Label, Textarea } from '../components/ui';
import { useUiStore } from '../store/uiStore';

export default function ResumeAnalyzer() {
  const pushToast = useUiStore((state) => state.pushToast);
  const [form, setForm] = useState({ title: 'Primary Resume', jobDescription: '', resumeText: '' });
  const [result, setResult] = useState(null);
  const [uploadFile, setUploadFile] = useState(null);

  const analyze = async (event) => {
    event.preventDefault();
    try {
      const { data } = await api.post('/resumes/analyze', { title: form.title, jobDescription: form.jobDescription, resumeText: form.resumeText });
      setResult(data);
      pushToast({ title: 'Resume analyzed', message: `ATS score: ${data.atsScore}%`, type: 'success' });
    } catch (error) {
      pushToast({ title: 'Analysis failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  const upload = async () => {
    if (!uploadFile) return;
    const formData = new FormData();
    formData.append('file', uploadFile);
    formData.append('title', form.title);
    formData.append('jobDescription', form.jobDescription);
    formData.append('extractedText', form.resumeText);
    try {
      const { data } = await api.post('/resumes/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      setResult({ atsScore: data.atsScore || 0, matchedKeywords: data.matchedKeywords || [], missingKeywords: data.missingKeywords || [], suggestions: data.suggestions || [], summary: 'Resume uploaded and analyzed.' });
      pushToast({ title: 'Resume stored', message: 'The uploaded resume is now attached to your profile.', type: 'success' });
    } catch (error) {
      pushToast({ title: 'Upload failed', message: error.response?.data?.message || error.message, type: 'error' });
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader kicker="Resume intelligence" title="Resume analyzer" description="Compare a resume against a job description and get ATS-style scoring with keyword gaps and improvement ideas." />

      <div className="grid gap-4 xl:grid-cols-[1.05fr_0.95fr]">
        <Card className="glass-panel">
          <CardHeader><CardTitle>Analyze your resume</CardTitle></CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={analyze}>
              <div>
                <Label>Title</Label>
                <Input className="mt-2" value={form.title} onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))} />
              </div>
              <div>
                <Label>Job description</Label>
                <Textarea className="mt-2" value={form.jobDescription} onChange={(e) => setForm((prev) => ({ ...prev, jobDescription: e.target.value }))} />
              </div>
              <div>
                <Label>Resume text</Label>
                <Textarea className="mt-2 min-h-[240px]" value={form.resumeText} onChange={(e) => setForm((prev) => ({ ...prev, resumeText: e.target.value }))} />
              </div>
              <div className="flex flex-wrap items-center gap-3">
                <Input type="file" className="max-w-sm" onChange={(e) => setUploadFile(e.target.files?.[0] || null)} />
                <Button type="button" variant="secondary" onClick={upload} disabled={!uploadFile}>Upload & attach</Button>
                <Button type="submit">Analyze ATS fit</Button>
              </div>
            </form>
          </CardContent>
        </Card>

        <Card className="glass-panel">
          <CardHeader><CardTitle>ATS result</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            {!result && <div className="text-sm text-slate-400">Run an analysis to see keyword matches, missing skills, and optimization suggestions.</div>}
            {result && (
              <>
                <div className="rounded-3xl border border-cyan-400/20 bg-cyan-400/10 p-5 text-center">
                  <div className="text-xs uppercase tracking-[0.28em] text-cyan-200/70">ATS score</div>
                  <div className="mt-3 text-5xl font-semibold text-white">{result.atsScore}%</div>
                  <p className="mt-2 text-sm text-slate-300">{result.summary}</p>
                </div>
                <div>
                  <div className="mb-2 text-sm font-semibold text-white">Matched keywords</div>
                  <div className="flex flex-wrap gap-2">{(result.matchedKeywords || []).map((keyword) => <Badge key={keyword} tone="success">{keyword}</Badge>)}</div>
                </div>
                <div>
                  <div className="mb-2 text-sm font-semibold text-white">Missing keywords</div>
                  <div className="flex flex-wrap gap-2">{(result.missingKeywords || []).map((keyword) => <Badge key={keyword} tone="danger">{keyword}</Badge>)}</div>
                </div>
                <div>
                  <div className="mb-2 text-sm font-semibold text-white">Suggestions</div>
                  <ul className="space-y-2 text-sm text-slate-300">{(result.suggestions || []).map((suggestion) => <li key={suggestion} className="rounded-2xl border border-white/10 bg-slate-950/50 px-3 py-2">{suggestion}</li>)}</ul>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
