import { useQuery } from '@tanstack/react-query'
import { getAttritionScores, type RiskBand } from './api'

// High/Medium pass WCAG AA narrowly (5.30:1 / 4.51:1); Low passes at 4.84:1.
// See the Day 12 accessibility pass notes for the exact ratios checked.
const RISK_BADGE: Record<RiskBand, string> = {
  High: 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-200',
  Medium: 'bg-amber-100 text-amber-700 dark:bg-amber-900 dark:text-amber-200',
  Low: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900 dark:text-emerald-200',
}

export default function AttritionDashboard() {
  const scoresQuery = useQuery({ queryKey: ['ai', 'attrition-scores'], queryFn: getAttritionScores })

  return (
    <div className="mb-8">
      <h2 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Attrition Risk</h2>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
            <th className="px-4 py-2 font-medium">Employee</th>
            <th className="px-4 py-2 font-medium">Department</th>
            <th className="px-4 py-2 font-medium">Risk Score</th>
            <th className="px-4 py-2 font-medium">Risk Band</th>
          </tr>
        </thead>
        <tbody>
          {scoresQuery.data?.map((score) => (
            <tr key={score.employeeId} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{score.employeeUsername}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{score.department ?? '—'}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{score.riskScore.toFixed(4)}</td>
              <td className="px-4 py-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${RISK_BADGE[score.riskBand]}`}>
                  {score.riskBand}
                </span>
              </td>
            </tr>
          ))}
          {scoresQuery.data?.length === 0 && (
            <tr>
              <td colSpan={4} className="px-4 py-4 text-center text-slate-500 dark:text-slate-400">
                No attrition scores yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
