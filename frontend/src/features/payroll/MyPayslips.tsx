import { useQuery } from '@tanstack/react-query'
import { downloadMyPayslip, getMyPayrollHistory } from './api'

export default function MyPayslips() {
  const query = useQuery({ queryKey: ['payroll', 'my'], queryFn: getMyPayrollHistory })

  return (
    <div>
      <h3 className="mb-3 font-semibold text-slate-900">My Payslips</h3>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600">
            <th className="px-4 py-2 font-medium">Month</th>
            <th className="px-4 py-2 font-medium">Gross</th>
            <th className="px-4 py-2 font-medium">Net</th>
            <th className="px-4 py-2 font-medium">Status</th>
            <th className="px-4 py-2 font-medium">Payslip</th>
          </tr>
        </thead>
        <tbody>
          {query.data?.map((p) => (
            <tr key={p.payrollId} className="border-b border-slate-100 last:border-0">
              <td className="px-4 py-2 text-slate-900">{p.payrollMonth}</td>
              <td className="px-4 py-2 text-slate-600">{p.grossSalary.toFixed(2)}</td>
              <td className="px-4 py-2 text-slate-600">{p.netSalary.toFixed(2)}</td>
              <td className="px-4 py-2 text-slate-600">{p.status}</td>
              <td className="px-4 py-2">
                <button
                  onClick={() => downloadMyPayslip(p.payrollId)}
                  className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100"
                >
                  Download PDF
                </button>
              </td>
            </tr>
          ))}
          {query.data?.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500">
                No payslips yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
