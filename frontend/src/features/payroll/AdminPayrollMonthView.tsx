import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  approvePayroll,
  downloadPayslip,
  getPayrollByMonth,
  lockPayroll,
  markPayrollPaid,
} from './api'

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const NEXT_ACTION: Record<string, { label: string; fn: (id: number) => Promise<unknown> } | undefined> = {
  DRAFT: { label: 'Approve', fn: approvePayroll },
  APPROVED: { label: 'Lock', fn: lockPayroll },
  LOCKED: { label: 'Mark Paid', fn: markPayrollPaid },
}

export default function AdminPayrollMonthView() {
  const [month, setMonth] = useState(currentMonth())
  const queryClient = useQueryClient()

  const query = useQuery({
    queryKey: ['payroll', 'month', month],
    queryFn: () => getPayrollByMonth(month),
  })

  const actionMutation = useMutation({
    mutationFn: (input: { id: number; fn: (id: number) => Promise<unknown> }) => input.fn(input.id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['payroll', 'month', month] }),
  })

  return (
    <div className="mt-8">
      <div className="mb-3 flex items-center gap-3">
        <h3 className="font-semibold text-slate-900 dark:text-slate-100">Payroll by Month</h3>
        <label className="sr-only" htmlFor="payroll-month">
          Payroll month
        </label>
        <input
          id="payroll-month"
          type="month"
          value={month}
          onChange={(e) => setMonth(e.target.value)}
          className="rounded-md border border-slate-300 px-2 py-1 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100"
        />
      </div>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
            <th className="px-4 py-2 font-medium">Employee</th>
            <th className="px-4 py-2 font-medium">Gross</th>
            <th className="px-4 py-2 font-medium">Net</th>
            <th className="px-4 py-2 font-medium">Status</th>
            <th className="px-4 py-2 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          {query.data?.map((p) => {
            const next = NEXT_ACTION[p.status]
            return (
              <tr key={p.payrollId} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
                <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{p.employeeName}</td>
                <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{p.grossSalary.toFixed(2)}</td>
                <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{p.netSalary.toFixed(2)}</td>
                <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{p.status}</td>
                <td className="px-4 py-2 space-x-2">
                  {next && (
                    <button
                      onClick={() => actionMutation.mutate({ id: p.payrollId, fn: next.fn })}
                      className="rounded-md bg-slate-900 px-2 py-1 text-xs text-white hover:bg-slate-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
                    >
                      {next.label}
                    </button>
                  )}
                  <button
                    onClick={() => downloadPayslip(p.payrollId)}
                    aria-label={`Download payslip PDF for ${p.employeeName}, ${month}`}
                    className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:hover:bg-slate-700"
                  >
                    PDF
                  </button>
                </td>
              </tr>
            )
          })}
          {query.data?.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500 dark:text-slate-400">
                No payroll records for this month.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
