import { useQuery } from '@tanstack/react-query'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { getAllEmployees } from '@/features/employees/api'
import { getAllLeaves } from '@/features/leave/api'
import { getPayrollByMonth } from '@/features/payroll/api'
import { getAttritionScores } from '@/features/ai/api'
import { getDepartmentCosts } from './api'

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
      <p className="text-xs text-slate-500 dark:text-slate-400">{label}</p>
      <p className="text-xl font-semibold text-slate-900 dark:text-slate-100">{value}</p>
    </div>
  )
}

export default function AdminDashboard() {
  const costsQuery = useQuery({ queryKey: ['reports', 'department-costs'], queryFn: getDepartmentCosts })
  const employeesQuery = useQuery({
    queryKey: ['employees', 'all', 0, 1],
    queryFn: () => getAllEmployees(0, 1),
  })
  const leavesQuery = useQuery({ queryKey: ['leaves', 'all'], queryFn: getAllLeaves })
  const payrollQuery = useQuery({
    queryKey: ['payroll', 'month', currentMonth()],
    queryFn: () => getPayrollByMonth(currentMonth()),
  })
  const attritionQuery = useQuery({ queryKey: ['ai', 'attrition-scores'], queryFn: getAttritionScores })

  const headcount = employeesQuery.data?.totalElements
  const openLeaveRequests = leavesQuery.data?.filter((l) => l.status === 'SUBMITTED').length
  const pendingPayrollApprovals = payrollQuery.data?.filter((p) => p.status === 'DRAFT').length
  const avgAttritionRisk = attritionQuery.data?.length
    ? attritionQuery.data.reduce((sum, s) => sum + s.riskScore, 0) / attritionQuery.data.length
    : undefined

  return (
    <div className="mb-8">
      <div className="mb-6 grid grid-cols-4 gap-4">
        <StatCard label="Headcount" value={headcount !== undefined ? String(headcount) : '—'} />
        <StatCard
          label="Open Leave Requests"
          value={openLeaveRequests !== undefined ? String(openLeaveRequests) : '—'}
        />
        <StatCard
          label="Pending Payroll Approvals"
          value={pendingPayrollApprovals !== undefined ? String(pendingPayrollApprovals) : '—'}
        />
        <StatCard
          label="Avg. Attrition Risk"
          value={avgAttritionRisk !== undefined ? avgAttritionRisk.toFixed(3) : '—'}
        />
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
        <h2 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">
          Department Cost Breakdown
        </h2>
        {/* Most employees have no department assigned yet (see README known
            limitations) - "Unassigned" is a real, explicit bucket from the
            backend, not a placeholder, so it's shown like any other bar
            rather than filtered out. */}
        <div
          role="img"
          aria-label="Bar chart of total CTC by department, including an Unassigned bucket for employees with no department on file"
          className="h-72"
        >
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={costsQuery.data ?? []}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-slate-200 dark:stroke-slate-700" />
              <XAxis dataKey="departmentName" tick={{ fontSize: 12 }} className="fill-slate-600 dark:fill-slate-400" />
              <YAxis tick={{ fontSize: 12 }} className="fill-slate-600 dark:fill-slate-400" />
              <Tooltip
                formatter={(value) =>
                  typeof value === 'number'
                    ? value.toLocaleString(undefined, { maximumFractionDigits: 2 })
                    : value
                }
              />
              <Bar dataKey="totalCtc" className="fill-slate-900 dark:fill-slate-300" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
