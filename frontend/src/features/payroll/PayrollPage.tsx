import { useAuth } from '@/features/auth/AuthContext'
import MyPayslips from './MyPayslips'
import AdminPayrollMonthView from './AdminPayrollMonthView'

const ADMIN_ROLES = ['ADMIN', 'HR_MANAGER']

export default function PayrollPage() {
  const { user } = useAuth()

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold text-slate-900 dark:text-slate-100">Payroll</h1>
      <MyPayslips />
      {user && ADMIN_ROLES.includes(user.role) && <AdminPayrollMonthView />}
    </div>
  )
}
