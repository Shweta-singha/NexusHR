import { api } from '@/lib/api'

export type GoalStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'

export interface GoalResponse {
  id: number
  employeeId: number
  employeeUsername: string
  title: string
  description: string | null
  targetDate: string | null
  status: GoalStatus
  createdAt: string
  updatedAt: string
}

export async function getMyGoals() {
  const res = await api.get<GoalResponse[]>('/api/goals')
  return res.data
}

export async function createGoal(input: {
  title: string
  description?: string
  targetDate?: string
}) {
  const res = await api.post<GoalResponse>('/api/goals', input)
  return res.data
}

export interface ReviewResponse {
  id: number
  employeeId: number
  employeeUsername: string
  reviewer: string
  reviewPeriod: string
  rating: number
  comments: string | null
  createdAt: string
}

export async function createReview(input: {
  employeeId: number
  reviewPeriod: string
  rating: number
  comments?: string
}) {
  const res = await api.post<ReviewResponse>('/api/reviews', input)
  return res.data
}

export async function getReviewsByEmployee(employeeId: number) {
  const res = await api.get<ReviewResponse[]>(`/api/reviews/employee/${employeeId}`)
  return res.data
}
