import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applyLeave, getLeaveTypes } from './api'

export default function LeaveApplyForm() {
  const queryClient = useQueryClient()
  const [leaveTypeId, setLeaveTypeId] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [reason, setReason] = useState('')

  const leaveTypesQuery = useQuery({ queryKey: ['leave-types'], queryFn: getLeaveTypes })

  const applyMutation = useMutation({
    mutationFn: applyLeave,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leaves', 'my'] })
      setStartDate('')
      setEndDate('')
      setReason('')
    },
  })

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    applyMutation.mutate({ leaveTypeId: Number(leaveTypeId), startDate, endDate, reason })
  }

  const fieldClass =
    'rounded-md border border-slate-300 px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100'

  return (
    <form onSubmit={handleSubmit} className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
      <h3 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Apply for Leave</h3>
      <div className="grid grid-cols-4 gap-3">
        <div>
          <label htmlFor="leave-type" className="sr-only">Leave type</label>
          <select
            id="leave-type"
            value={leaveTypeId}
            onChange={(e) => setLeaveTypeId(e.target.value)}
            required
            className={`w-full ${fieldClass}`}
          >
            <option value="" disabled>
              Leave type
            </option>
            {leaveTypesQuery.data?.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="leave-start" className="sr-only">Start date</label>
          <input
            id="leave-start"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            required
            className={`w-full ${fieldClass}`}
          />
        </div>
        <div>
          <label htmlFor="leave-end" className="sr-only">End date</label>
          <input
            id="leave-end"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            required
            className={`w-full ${fieldClass}`}
          />
        </div>
        <div>
          <label htmlFor="leave-reason" className="sr-only">Reason</label>
          <input
            id="leave-reason"
            type="text"
            placeholder="Reason"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            required
            className={`w-full ${fieldClass}`}
          />
        </div>
      </div>
      {applyMutation.isError && (
        <p className="mt-2 text-sm text-red-600 dark:text-red-400">Could not save draft — check your inputs.</p>
      )}
      <button
        type="submit"
        disabled={applyMutation.isPending}
        className="mt-3 rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
      >
        Save as Draft
      </button>
    </form>
  )
}
