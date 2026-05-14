import React, { useEffect } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import { useUiStore } from './store/uiStore';
import ThemePrimer from './components/ThemePrimer';
import ToastHost from './components/ToastHost';
import ProtectedRoute from './components/ProtectedRoute';
import AppShell from './components/AppShell';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import OAuthCallback from './pages/OAuthCallback';
import Dashboard from './pages/Dashboard';
import Applications from './pages/Applications';
import Analytics from './pages/Analytics';
import ResumeAnalyzer from './pages/ResumeAnalyzer';
import Interviews from './pages/Interviews';
import Settings from './pages/Settings';
import Profile from './pages/Profile';

function Bootstrap() {
  const bootstrap = useAuthStore((state) => state.bootstrap);
  useEffect(() => {
    bootstrap();
  }, [bootstrap]);
  return null;
}

export default function App() {
  const theme = useUiStore((state) => state.theme);

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark');
  }, [theme]);

  return (
    <>
      <ThemePrimer />
      <ToastHost />
      <Bootstrap />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/auth/callback" element={<OAuthCallback />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="applications" element={<Applications />} />
          <Route path="analytics" element={<Analytics />} />
          <Route path="resume" element={<ResumeAnalyzer />} />
          <Route path="interviews" element={<Interviews />} />
          <Route path="settings" element={<Settings />} />
          <Route path="profile" element={<Profile />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
