import { api } from '@/lib/api'

// PolicyChatResponse (ai-service is unrelated - this hits the Employee
// module's own /api/hr-chat, Day 9) currently returns only `answer`; no
// source-document citations are included. See the frontend gap report.
export interface PolicyChatResponse {
  answer: string
}

export async function askHrChat(question: string) {
  const res = await api.post<PolicyChatResponse>('/api/hr-chat', { question })
  return res.data
}

// Matches AttritionScoreResponse - confirmed live via GET /api/ai/attrition-scores.
export type RiskBand = 'Low' | 'Medium' | 'High'

export interface AttritionScoreResponse {
  employeeId: number
  employeeUsername: string
  department: string | null
  riskScore: number
  riskBand: RiskBand
  scoredAt: string
}

export async function getAttritionScores() {
  const res = await api.get<AttritionScoreResponse[]>('/api/ai/attrition-scores')
  return res.data
}
