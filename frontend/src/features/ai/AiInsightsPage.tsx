import { useAuth } from '@/features/auth/AuthContext'
import AttritionDashboard from './AttritionDashboard'
import ChatWidget from './ChatWidget'

// Matches AttritionController's @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')").
const ATTRITION_ROLES = ['ADMIN', 'HR_MANAGER', 'MANAGER']

export default function AiInsightsPage() {
  const { user } = useAuth()

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold text-slate-900 dark:text-slate-100">AI Insights</h1>

      {user && ATTRITION_ROLES.includes(user.role) && <AttritionDashboard />}

      <ChatWidget />
    </div>
  )
}
