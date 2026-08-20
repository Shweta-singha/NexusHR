import { api } from '@/lib/api'

export interface AttendanceDto {
  id: number
  employeeId: number
  username: string
  date: string
  checkIn: string | null
  checkOut: string | null
  workingHours: number | null
  overtimeHours: number | null
  attendanceStatus: string
}

export async function checkIn() {
  const res = await api.post<AttendanceDto>('/api/attendance/check-in')
  return res.data
}

export async function checkOut() {
  const res = await api.post<AttendanceDto>('/api/attendance/check-out')
  return res.data
}

export async function getMyAttendance() {
  const res = await api.get<AttendanceDto[]>('/api/attendance/my')
  return res.data
}
