import { useAuth } from '@/features/auth/AuthContext'

export default function DashboardPage() {
  const { user } = useAuth()
  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900">Welcome, {user?.username}</h1>
      <p className="mt-2 text-slate-600">Role: {user?.role}</p>
    </div>
  )
}
