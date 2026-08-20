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
      <h3 className="mb-3 font-semibold text-slate-900">Approval Queue ({pending.length} pending)</h3>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600">
            <th className="px-4 py-2 font-medium">Employee</th>
            <th className="px-4 py-2 font-medium">Type</th>
            <th className="px-4 py-2 font-medium">Dates</th>
            <th className="px-4 py-2 font-medium">Reason</th>
            <th className="px-4 py-2 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          {pending.map((leave) => (
            <tr key={leave.id} className="border-b border-slate-100 last:border-0">
              <td className="px-4 py-2 text-slate-900">{leave.employeeUsername}</td>
              <td className="px-4 py-2 text-slate-600">{leave.leaveType}</td>
              <td className="px-4 py-2 text-slate-600">
                {leave.startDate} → {leave.endDate}
              </td>
              <td className="px-4 py-2 text-slate-600">{leave.reason}</td>
              <td className="px-4 py-2 space-x-2">
                <button
                  onClick={() => approveMutation.mutate(leave.id)}
                  className="rounded-md bg-slate-900 px-2 py-1 text-xs text-white hover:bg-slate-800"
                >
                  Approve
                </button>
                <button
                  onClick={() => rejectMutation.mutate(leave.id)}
                  className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100"
                >
                  Reject
                </button>
              </td>
            </tr>
          ))}
          {pending.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500">
                No pending requests.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
