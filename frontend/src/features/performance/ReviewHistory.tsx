import { useQuery } from '@tanstack/react-query'
import { getReviewsByEmployee } from './api'

interface ReviewHistoryProps {
  employeeId: number
}

export default function ReviewHistory({ employeeId }: ReviewHistoryProps) {
  const reviewsQuery = useQuery({
    queryKey: ['reviews', employeeId],
    queryFn: () => getReviewsByEmployee(employeeId),
  })

  return (
    <div className="mt-4">
      <h3 className="mb-3 font-semibold text-slate-900 dark:text-slate-100">Review History</h3>
      <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
            <th className="px-4 py-2 font-medium">Period</th>
            <th className="px-4 py-2 font-medium">Rating</th>
            <th className="px-4 py-2 font-medium">Reviewer</th>
            <th className="px-4 py-2 font-medium">Comments</th>
            <th className="px-4 py-2 font-medium">Date</th>
          </tr>
        </thead>
        <tbody>
          {reviewsQuery.data?.map((review) => (
            <tr key={review.id} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{review.reviewPeriod}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{review.rating} / 5</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{review.reviewer}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{review.comments ?? '—'}</td>
              <td className="px-4 py-2 text-slate-600 dark:text-slate-400">
                {new Date(review.createdAt).toLocaleDateString()}
              </td>
            </tr>
          ))}
          {reviewsQuery.data?.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-4 text-center text-slate-500 dark:text-slate-400">
                No reviews yet for this employee.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
