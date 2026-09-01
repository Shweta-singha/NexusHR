import { useAuth } from '@/features/auth/AuthContext'
import AdminDashboard from '@/features/admin/AdminDashboard'

const ADMIN_DASHBOARD_ROLES = ['ADMIN', 'HR_MANAGER', 'MANAGER']

export default function DashboardPage() {
  const { user } = useAuth()
  const showAdminDashboard = user && ADMIN_DASHBOARD_ROLES.includes(user.role)

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">
        Welcome, {user?.username}
      </h1>
      <p className="mt-2 mb-6 text-slate-600 dark:text-slate-400">Role: {user?.role}</p>

      {showAdminDashboard && <AdminDashboard />}
    </div>
  )
}
