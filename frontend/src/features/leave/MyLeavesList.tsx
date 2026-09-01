import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { cancelLeave, getMyLeaves, submitLeave } from './api'

// CANCELLED/CLOSED originally used text-slate-500 on bg-slate-100 - that
// pair is 4.34:1, just under WCAG AA's 4.5:1 for normal text. slate-600
// (6.92:1) keeps the same "muted" intent and passes.
const STATUS_COLORS: Record<string, string> = {
  DRAFT: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
  SUBMITTED: 'bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200',
  APPROVED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
  REJECTED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
  CANCELLED: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
  CLOSED: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
}

export default function MyLeavesList() {
  const queryClient = useQueryClient()

  const leavesQuery = useQuery({ queryKey: ['leaves', 'my'], queryFn: getMyLeaves })

  const submitMutation = useMutation({
    mutationFn: submitLeave,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['leaves', 'my'] }),
  })

  const cancelMutation = useMutation({
    mutationFn: cancelLeave,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leaves', 'my'] })
      queryClient.invalidateQueries({ queryKey: ['leaves', 'balance'] })
    },
  })

  return (
    <div className="mt-6">
      <h3 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">My Leaves</h3>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
            <th className="px-4 py-2 font-medium">Type</th>
            <th className="px-4 py-2 font-medium">Dates</th>
            <th className="px-4 py-2 font-medium">Days</th>
            <th className="px-4 py-2 font-medium">Status</th>
            <th className="px-4 py-2 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          {leavesQuery.data?.map((leave) => (
            <tr key={leave.id} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{leave.leaveType}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">
                {leave.startDate} → {leave.endDate}
              </td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{leave.totalDays}</td>
              <td className="px-4 py-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[leave.status] ?? ''}`}>
                  {leave.status}
                </span>
              </td>
              <td className="px-4 py-2">
                {leave.status === 'DRAFT' && (
                  <button
                    onClick={() => submitMutation.mutate(leave.id)}
                    className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:hover:bg-slate-700"
                  >
                    Submit
                  </button>
                )}
                {(leave.status === 'SUBMITTED' || leave.status === 'APPROVED') && (
                  <button
                    onClick={() => cancelMutation.mutate(leave.id)}
                    className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:hover:bg-slate-700"
                  >
                    Cancel
                  </button>
                )}
              </td>
            </tr>
          ))}
          {leavesQuery.data?.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500 dark:text-slate-400">
                No leave requests yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
