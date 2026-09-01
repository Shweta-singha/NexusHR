import { Link, Outlet, useLocation } from 'react-router-dom'
import { Moon, Sun } from 'lucide-react'
import { useAuth } from '@/features/auth/AuthContext'
import { useTheme } from '@/features/theme/ThemeContext'
import { cn } from '@/lib/utils'

const ADMIN_ROLES = ['ADMIN', 'HR_MANAGER']

export default function Layout() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const location = useLocation()

  const navItems = [
    { to: '/', label: 'Dashboard' },
    { to: '/employees', label: 'Employees', roles: ADMIN_ROLES },
    { to: '/attendance', label: 'Attendance' },
    { to: '/leave', label: 'Leave' },
    { to: '/payroll', label: 'Payroll' },
    { to: '/performance', label: 'Performance' },
    { to: '/ai-insights', label: 'AI Insights' },
  ]

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <header className="border-b border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-3">
          <span className="text-lg font-semibold text-slate-900 dark:text-slate-100">NexusHR</span>
          <nav className="flex gap-1">
            {navItems
              .filter((item) => !item.roles || (user && item.roles.includes(user.role)))
              .map((item) => (
                <Link
                  key={item.to}
                  to={item.to}
                  className={cn(
                    'rounded-md px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:text-slate-400 dark:hover:bg-slate-800',
                    location.pathname === item.to &&
                      'bg-slate-100 text-slate-900 dark:bg-slate-800 dark:text-slate-100',
                  )}
                >
                  {item.label}
                </Link>
              ))}
          </nav>
          <div className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
            <button
              onClick={toggleTheme}
              aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
              className="rounded-md border border-slate-300 p-1.5 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-700 dark:hover:bg-slate-800"
            >
              {theme === 'dark' ? <Sun className="size-4" aria-hidden="true" /> : <Moon className="size-4" aria-hidden="true" />}
            </button>
            <span>
              {user?.username} <span className="text-slate-400 dark:text-slate-500">({user?.role})</span>
            </span>
            <button
              onClick={logout}
              className="rounded-md border border-slate-300 px-3 py-1 text-sm hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-700 dark:hover:bg-slate-800"
            >
              Log out
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  )
}
