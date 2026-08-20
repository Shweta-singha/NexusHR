import { api } from '@/lib/api'

export interface LeaveType {
  id: number
  name: string
  maxDaysPerYear: number
}

export interface LeaveBalance {
  casual: number
  sick: number
  earned: string
  compOff: number
}

export interface LeaveResponse {
  id: number
  leaveType: string
  startDate: string
  endDate: string
  totalDays: number
  reason: string
  status: string
  appliedAt: string
  approvedAt: string | null
  approvedBy: string | null
}

export interface AdminLeaveResponse extends LeaveResponse {
  employeeUsername: string
}

export async function getLeaveTypes() {
  const res = await api.get<LeaveType[]>('/api/leave-types')
  return res.data
}

export async function getMyBalance() {
  const res = await api.get<LeaveBalance>('/api/leaves/balance')
  return res.data
}

export async function getMyLeaves() {
  const res = await api.get<LeaveResponse[]>('/api/leaves/my')
  return res.data
}

export async function applyLeave(input: {
  leaveTypeId: number
  startDate: string
  endDate: string
  reason: string
}) {
  const res = await api.post<LeaveResponse>('/api/leaves/apply', input)
  return res.data
}

export async function submitLeave(id: number) {
  const res = await api.put<LeaveResponse>(`/api/leaves/${id}/submit`)
  return res.data
}

export async function cancelLeave(id: number) {
  const res = await api.put<LeaveResponse>(`/api/leaves/${id}/cancel`)
  return res.data
}

export async function getAllLeaves() {
  const res = await api.get<AdminLeaveResponse[]>('/api/leaves/all')
  return res.data
}

export async function approveLeave(id: number) {
  const res = await api.post<LeaveResponse>(`/api/leaves/${id}/approve`)
  return res.data
}

export async function rejectLeave(id: number, reason: string) {
  const res = await api.post<LeaveResponse>(`/api/leaves/${id}/reject`, { reason })
  return res.data
}
