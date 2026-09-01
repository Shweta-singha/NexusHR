import { api } from '@/lib/api'

export interface DepartmentCostResponse {
  departmentName: string
  employeeCount: number
  totalCtc: number
}

export async function getDepartmentCosts() {
  const res = await api.get<DepartmentCostResponse[]>('/api/reports/department-costs')
  return res.data
}
