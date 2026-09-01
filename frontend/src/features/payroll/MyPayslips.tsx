import { useQuery } from '@tanstack/react-query'
import { downloadMyPayslip, getMyPayrollHistory } from './api'

export default function MyPayslips() {
  const query = useQuery({ queryKey: ['payroll', 'my'], queryFn: getMyPayrollHistory })

  return (
    <div>
      <h3 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">My Payslips</h3>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
            <th className="px-4 py-2 font-medium">Month</th>
            <th className="px-4 py-2 font-medium">Gross</th>
            <th className="px-4 py-2 font-medium">Net</th>
            <th className="px-4 py-2 font-medium">Status</th>
            <th className="px-4 py-2 font-medium">Payslip</th>
          </tr>
        </thead>
        <tbody>
          {query.data?.map((p) => (
            <tr key={p.payrollId} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{p.payrollMonth}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{p.grossSalary.toFixed(2)}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{p.netSalary.toFixed(2)}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{p.status}</td>
              <td className="px-4 py-2">
                <button
                  onClick={() => downloadMyPayslip(p.payrollId)}
                  aria-label={`Download payslip PDF for ${p.payrollMonth}`}
                  className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:hover:bg-slate-700"
                >
                  Download PDF
                </button>
              </td>
            </tr>
          ))}
          {query.data?.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500 dark:text-slate-400">
                No payslips yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
