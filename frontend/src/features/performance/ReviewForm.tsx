import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getAllEmployees } from '@/features/employees/api'
import { createReview } from './api'

interface ReviewFormProps {
  selectedEmployeeId: number | null
  onSelectEmployee: (id: number | null) => void
}

export default function ReviewForm({ selectedEmployeeId, onSelectEmployee }: ReviewFormProps) {
  const queryClient = useQueryClient()
  const [reviewPeriod, setReviewPeriod] = useState('')
  const [rating, setRating] = useState('')
  const [comments, setComments] = useState('')

  // size=100 - a dropdown-sized fetch, matching this org's scale; would need
  // real pagination/search if the employee list grew much larger.
  const employeesQuery = useQuery({
    queryKey: ['employees', 'all', 0, 100],
    queryFn: () => getAllEmployees(0, 100),
  })

  const createMutation = useMutation({
    mutationFn: createReview,
    onSuccess: (review) => {
      queryClient.invalidateQueries({ queryKey: ['reviews', review.employeeId] })
      setReviewPeriod('')
      setRating('')
      setComments('')
    },
  })

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!selectedEmployeeId) return
    createMutation.mutate({
      employeeId: selectedEmployeeId,
      reviewPeriod,
      rating: Number(rating),
      comments: comments || undefined,
    })
  }

  const fieldClass =
    'w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100'

  return (
    <form onSubmit={handleSubmit} className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
      <h3 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Submit Review</h3>
      {employeesQuery.isError && (
        <p className="mb-3 text-sm text-red-600 dark:text-red-400">
          Could not load the employee list (GET /api/employee/all is ADMIN/HR_MANAGER/MANAGER-only
          on the backend).
        </p>
      )}
      <div className="grid grid-cols-4 gap-3">
        <div>
          <label htmlFor="review-employee" className="sr-only">Employee</label>
          <select
            id="review-employee"
            value={selectedEmployeeId ?? ''}
            onChange={(e) => onSelectEmployee(e.target.value ? Number(e.target.value) : null)}
            required
            className={fieldClass}
          >
            <option value="" disabled>
              Employee
            </option>
            {employeesQuery.data?.content.map((emp) => (
              <option key={emp.id} value={emp.id}>
                {emp.username}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="review-period" className="sr-only">Review period</label>
          <input
            id="review-period"
            type="text"
            placeholder="Review period (e.g. 2026-H1)"
            value={reviewPeriod}
            onChange={(e) => setReviewPeriod(e.target.value)}
            required
            className={fieldClass}
          />
        </div>
        <div>
          <label htmlFor="review-rating" className="sr-only">Rating</label>
          <select
            id="review-rating"
            value={rating}
            onChange={(e) => setRating(e.target.value)}
            required
            className={fieldClass}
          >
            <option value="" disabled>
              Rating
            </option>
            {[1, 2, 3, 4, 5].map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="review-comments" className="sr-only">Comments</label>
          <input
            id="review-comments"
            type="text"
            placeholder="Comments (optional)"
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            className={fieldClass}
          />
        </div>
      </div>
      {createMutation.isError && (
        <p className="mt-2 text-sm text-red-600 dark:text-red-400">Could not submit review — check your inputs.</p>
      )}
      <button
        type="submit"
        disabled={createMutation.isPending || !selectedEmployeeId}
        className="mt-3 rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
      >
        Submit Review
      </button>
    </form>
  )
}
