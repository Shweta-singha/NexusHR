import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { approveLeave, getAllLeaves, rejectLeave } from './api'

export default function AdminLeaveQueue() {
  const queryClient = useQueryClient()

  const allLeavesQuery = useQuery({ queryKey: ['leaves', 'all'], queryFn: getAllLeaves })

  const approveMutation = useMutation({
    mutationFn: approveLeave,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['leaves', 'all'] }),
  })

  const rejectMutation = useMutation({
    mutationFn: (id: number) => rejectLeave(id, 'Rejected by manager'),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['leaves', 'all'] }),
  })

  const pending = allLeavesQuery.data?.filter((l) => l.status === 'SUBMITTED') ?? []

  return (
    <div className="mt-8">
      <h3 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Approval Queue ({pending.length} pending)</h3>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
            <th className="px-4 py-2 font-medium">Employee</th>
            <th className="px-4 py-2 font-medium">Type</th>
            <th className="px-4 py-2 font-medium">Dates</th>
            <th className="px-4 py-2 font-medium">Reason</th>
            <th className="px-4 py-2 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          {pending.map((leave) => (
            <tr key={leave.id} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{leave.employeeUsername}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{leave.leaveType}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">
                {leave.startDate} → {leave.endDate}
              </td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{leave.reason}</td>
              <td className="px-4 py-2 space-x-2">
                <button
                  onClick={() => approveMutation.mutate(leave.id)}
                  aria-label={`Approve leave for ${leave.employeeUsername}`}
                  className="rounded-md bg-slate-900 px-2 py-1 text-xs text-white hover:bg-slate-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
                >
                  Approve
                </button>
                <button
                  onClick={() => rejectMutation.mutate(leave.id)}
                  aria-label={`Reject leave for ${leave.employeeUsername}`}
                  className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:hover:bg-slate-700"
                >
                  Reject
                </button>
              </td>
            </tr>
          ))}
          {pending.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500 dark:text-slate-400">
                No pending requests.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
