import React, { useMemo } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { BarChart3, BriefcaseBusiness, CircleDollarSign, GraduationCap, Home, LogOut, Menu, MoonStar, Settings, Sparkles, SunMedium, UserCircle2, Mail, CalendarDays } from 'lucide-react';
import { Avatar, Button, Divider } from './ui';
import { useAuthStore } from '../store/authStore';
import { useUiStore } from '../store/uiStore';

const navItems = [
  { to: '/', label: 'Dashboard', icon: Home },
  { to: '/applications', label: 'Applications', icon: BriefcaseBusiness },
  { to: '/analytics', label: 'Analytics', icon: BarChart3 },
  { to: '/resume', label: 'Resume Analyzer', icon: GraduationCap },
  { to: '/interviews', label: 'Interviews', icon: CalendarDays },
  { to: '/settings', label: 'Settings', icon: Settings },
  { to: '/profile', label: 'Profile', icon: UserCircle2 }
];

export default function AppShell() {
  const location = useLocation();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const theme = useUiStore((state) => state.theme);
  const toggleTheme = useUiStore((state) => state.toggleTheme);

  const initials = useMemo(() => (user?.fullName || 'AF').split(' ').filter(Boolean).map((part) => part[0]).slice(0, 2).join('').toUpperCase() || 'AF', [user]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-hero-gradient text-slate-100">
      <div className="flex min-h-screen bg-slate-950/40">
        <aside className="hidden w-72 border-r border-white/10 bg-slate-950/70 px-5 py-6 backdrop-blur-xl xl:flex xl:flex-col">
          <div className="mb-8 flex items-center gap-3 px-2">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-cyan-400 to-violet-500 text-slate-950 shadow-lg shadow-cyan-500/20">
              <Sparkles className="h-6 w-6" />
            </div>
            <div>
              <div className="text-lg font-semibold text-white">ApplyFlow AI</div>
              <div className="text-xs uppercase tracking-[0.25em] text-slate-400">Career OS</div>
            </div>
          </div>
          <nav className="flex-1 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) => `flex items-center gap-3 rounded-2xl px-4 py-3 text-sm transition ${isActive ? 'bg-white/10 text-white' : 'text-slate-400 hover:bg-white/5 hover:text-white'}`}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </NavLink>
              );
            })}
          </nav>
          <Divider />
          <div className="mt-5 rounded-3xl border border-cyan-400/20 bg-cyan-400/10 p-4">
            <div className="text-xs uppercase tracking-[0.28em] text-cyan-200/70">AI assistant</div>
            <p className="mt-2 text-sm text-cyan-50/80">Track interviews, optimize resumes, and keep momentum with AI-powered insights.</p>
          </div>
        </aside>

        <main className="flex min-h-screen flex-1 flex-col">
          <header className="sticky top-0 z-20 border-b border-white/10 bg-slate-950/65 backdrop-blur-xl">
            <div className="flex items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
              <div className="flex items-center gap-3">
                <Button variant="ghost" className="xl:hidden" onClick={() => navigate('/')}> <Menu className="h-5 w-5" /> </Button>
                <div>
                  <div className="text-sm uppercase tracking-[0.22em] text-slate-400">ApplyFlow AI</div>
                  <div className="text-xl font-semibold text-white">Hello, {user?.fullName || 'Friend'}</div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Button variant="secondary" size="sm" onClick={toggleTheme} className="gap-2">
                  {theme === 'dark' ? <SunMedium className="h-4 w-4" /> : <MoonStar className="h-4 w-4" />}
                  {theme === 'dark' ? 'Light' : 'Dark'}
                </Button>
                <button className="flex items-center gap-3 rounded-2xl border border-white/10 bg-white/5 px-3 py-2 text-left backdrop-blur-xl" onClick={() => navigate('/profile')}>
                  <Avatar>{initials}</Avatar>
                  <div className="hidden sm:block">
                    <div className="text-sm font-semibold text-white">{user?.fullName || 'User'}</div>
                    <div className="text-xs text-slate-400">{user?.email}</div>
                  </div>
                </button>
                <Button variant="ghost" size="sm" onClick={handleLogout} className="gap-2">
                  <LogOut className="h-4 w-4" />
                  Sign out
                </Button>
              </div>
            </div>
          </header>
          <div className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
            <AnimatePresence mode="wait">
              <motion.div key={location.pathname} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -12 }} transition={{ duration: 0.22 }}>
                <Outlet />
              </motion.div>
            </AnimatePresence>
          </div>
        </main>
      </div>
    </div>
  );
}
