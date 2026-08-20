import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { cancelLeave, getMyLeaves, submitLeave } from './api'

const STATUS_COLORS: Record<string, string> = {
  DRAFT: 'bg-slate-100 text-slate-700',
  SUBMITTED: 'bg-amber-100 text-amber-800',
  APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-slate-100 text-slate-500',
  CLOSED: 'bg-slate-100 text-slate-500',
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
      <h3 className="mb-3 font-semibold text-slate-900">My Leaves</h3>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600">
            <th className="px-4 py-2 font-medium">Type</th>
            <th className="px-4 py-2 font-medium">Dates</th>
            <th className="px-4 py-2 font-medium">Days</th>
            <th className="px-4 py-2 font-medium">Status</th>
            <th className="px-4 py-2 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          {leavesQuery.data?.map((leave) => (
            <tr key={leave.id} className="border-b border-slate-100 last:border-0">
              <td className="px-4 py-2 text-slate-900">{leave.leaveType}</td>
              <td className="px-4 py-2 text-slate-600">
                {leave.startDate} → {leave.endDate}
              </td>
              <td className="px-4 py-2 text-slate-600">{leave.totalDays}</td>
              <td className="px-4 py-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[leave.status] ?? ''}`}>
                  {leave.status}
                </span>
              </td>
              <td className="px-4 py-2">
                {leave.status === 'DRAFT' && (
                  <button
                    onClick={() => submitMutation.mutate(leave.id)}
                    className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100"
                  >
                    Submit
                  </button>
                )}
                {(leave.status === 'SUBMITTED' || leave.status === 'APPROVED') && (
                  <button
                    onClick={() => cancelMutation.mutate(leave.id)}
                    className="rounded-md border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100"
                  >
                    Cancel
                  </button>
                )}
              </td>
            </tr>
          ))}
          {leavesQuery.data?.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500">
                No leave requests yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
