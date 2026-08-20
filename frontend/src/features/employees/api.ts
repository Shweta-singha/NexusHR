import { api } from '@/lib/api'

export interface EmployeeDto {
  id: number
  username: string
  email: string
  role: string
  promotedBy: string | null
  promotedAt: string | null
}

interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
  size: number
}

export async function getAllEmployees(page: number, size = 10) {
  const res = await api.get<PageResponse<EmployeeDto>>('/api/employee/all', {
    params: { page, size },
  })
  return res.data
}

export interface DepartmentHierarchyNode {
  id: number
  name: string
  employeeCount: number
  children: DepartmentHierarchyNode[]
}

export async function getDepartmentHierarchy() {
  const res = await api.get<DepartmentHierarchyNode[]>('/api/departments/hierarchy')
  return res.data
}
