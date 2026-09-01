import { useState } from 'react'
import { useAuth } from '@/features/auth/AuthContext'
import GoalTracker from './GoalTracker'
import ReviewForm from './ReviewForm'
import ReviewHistory from './ReviewHistory'

// Matches ReviewController's @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
// on both POST /api/reviews and GET /api/reviews/employee/{id} - employees
// can't view their own review history through this endpoint yet.
const REVIEWER_ROLES = ['ADMIN', 'HR_MANAGER', 'MANAGER']

export default function PerformancePage() {
  const { user } = useAuth()
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number | null>(null)
  const canReview = user && REVIEWER_ROLES.includes(user.role)

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold text-slate-900 dark:text-slate-100">Performance</h1>

      <GoalTracker />

      {canReview && (
        <div className="mt-8">
          <ReviewForm selectedEmployeeId={selectedEmployeeId} onSelectEmployee={setSelectedEmployeeId} />
          {selectedEmployeeId && <ReviewHistory employeeId={selectedEmployeeId} />}
        </div>
      )}
    </div>
  )
}
