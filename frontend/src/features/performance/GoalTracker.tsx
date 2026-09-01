import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createGoal, getMyGoals, type GoalStatus } from './api'

const STATUS_LABEL: Record<GoalStatus, string> = {
  NOT_STARTED: 'Not Started',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
}

// NOT_STARTED's text-slate-600 on bg-slate-100 is 6.92:1 (WCAG AA pass);
// the amber/emerald pairs below are 4.51:1 and 4.84:1 respectively - both
// pass but narrowly, see the Day 12 accessibility pass notes.
const STATUS_COLOR: Record<GoalStatus, string> = {
  NOT_STARTED: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
  IN_PROGRESS: 'bg-amber-100 text-amber-700 dark:bg-amber-900 dark:text-amber-200',
  COMPLETED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900 dark:text-emerald-200',
}

export default function GoalTracker() {
  const queryClient = useQueryClient()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [targetDate, setTargetDate] = useState('')

  const goalsQuery = useQuery({ queryKey: ['goals', 'my'], queryFn: getMyGoals })

  const createMutation = useMutation({
    mutationFn: createGoal,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['goals', 'my'] })
      setTitle('')
      setDescription('')
      setTargetDate('')
    },
  })

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    createMutation.mutate({
      title,
      description: description || undefined,
      targetDate: targetDate || undefined,
    })
  }

  const fieldClass =
    'w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100'

  return (
    <div>
      <h2 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">My Goals</h2>

      <form onSubmit={handleSubmit} className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
        <div className="grid grid-cols-3 gap-3">
          <div>
            <label htmlFor="goal-title" className="sr-only">Goal title</label>
            <input
              id="goal-title"
              type="text"
              placeholder="Goal title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              className={fieldClass}
            />
          </div>
          <div>
            <label htmlFor="goal-description" className="sr-only">Description</label>
            <input
              id="goal-description"
              type="text"
              placeholder="Description (optional)"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className={fieldClass}
            />
          </div>
          <div>
            <label htmlFor="goal-target-date" className="sr-only">Target date</label>
            <input
              id="goal-target-date"
              type="date"
              value={targetDate}
              onChange={(e) => setTargetDate(e.target.value)}
              className={fieldClass}
            />
          </div>
        </div>
        {createMutation.isError && (
          <p className="mt-2 text-sm text-red-600 dark:text-red-400">Could not create goal — check your inputs.</p>
        )}
        <button
          type="submit"
          disabled={createMutation.isPending}
          className="mt-3 rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
        >
          Add Goal
        </button>
      </form>

      <table className="mt-4 w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
            <th className="px-4 py-2 font-medium">Title</th>
            <th className="px-4 py-2 font-medium">Description</th>
            <th className="px-4 py-2 font-medium">Target Date</th>
            <th className="px-4 py-2 font-medium">Status</th>
          </tr>
        </thead>
        <tbody>
          {goalsQuery.data?.map((goal) => (
            <tr key={goal.id} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{goal.title}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{goal.description ?? '—'}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{goal.targetDate ?? '—'}</td>
              <td className="px-4 py-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLOR[goal.status]}`}>
                  {STATUS_LABEL[goal.status]}
                </span>
              </td>
            </tr>
          ))}
          {goalsQuery.data?.length === 0 && (
            <tr>
              <td colSpan={4} className="px-4 py-4 text-center text-slate-500 dark:text-slate-400">
                No goals yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
