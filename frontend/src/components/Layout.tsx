import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import { cn } from '@/lib/utils'

const ADMIN_ROLES = ['ADMIN', 'HR_MANAGER']

export default function Layout() {
  const { user, logout } = useAuth()
  const location = useLocation()

  const navItems = [
    { to: '/', label: 'Dashboard' },
    { to: '/employees', label: 'Employees', roles: ADMIN_ROLES },
    { to: '/attendance', label: 'Attendance' },
    { to: '/leave', label: 'Leave' },
    { to: '/payroll', label: 'Payroll' },
  ]

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-3">
          <span className="text-lg font-semibold text-slate-900">NexusHR</span>
          <nav className="flex gap-1">
            {navItems
              .filter((item) => !item.roles || (user && item.roles.includes(user.role)))
              .map((item) => (
                <Link
                  key={item.to}
                  to={item.to}
                  className={cn(
                    'rounded-md px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-100',
                    location.pathname === item.to && 'bg-slate-100 text-slate-900',
                  )}
                >
                  {item.label}
                </Link>
              ))}
          </nav>
          <div className="flex items-center gap-3 text-sm text-slate-600">
            <span>
              {user?.username} <span className="text-slate-400">({user?.role})</span>
            </span>
            <button
              onClick={logout}
              className="rounded-md border border-slate-300 px-3 py-1 text-sm hover:bg-slate-100"
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
