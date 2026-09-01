import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/features/auth/AuthContext'
import { getMyBalance } from './api'
import LeaveApplyForm from './LeaveApplyForm'
import MyLeavesList from './MyLeavesList'
import AdminLeaveQueue from './AdminLeaveQueue'

const ADMIN_ROLES = ['ADMIN', 'HR_MANAGER']

export default function LeavePage() {
  const { user } = useAuth()
  const balanceQuery = useQuery({ queryKey: ['leaves', 'balance'], queryFn: getMyBalance })

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold text-slate-900 dark:text-slate-100">Leave</h1>

      {balanceQuery.data && (
        <div className="mb-6 grid grid-cols-4 gap-4">
          {[
            { label: 'Casual', value: balanceQuery.data.casual },
            { label: 'Sick', value: balanceQuery.data.sick },
            { label: 'Earned', value: balanceQuery.data.earned },
            { label: 'Comp-Off', value: balanceQuery.data.compOff },
          ].map((b) => (
            <div key={b.label} className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
              <p className="text-xs text-slate-500 dark:text-slate-400">{b.label}</p>
              <p className="text-xl font-semibold text-slate-900 dark:text-slate-100">{b.value}</p>
            </div>
          ))}
        </div>
      )}

      <LeaveApplyForm />
      <MyLeavesList />

      {user && ADMIN_ROLES.includes(user.role) && <AdminLeaveQueue />}
    </div>
  )
}
