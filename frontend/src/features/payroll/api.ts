import { api } from '@/lib/api'

export interface PayrollRecord {
  payrollId: number
  employeeId: number
  employeeName: string
  payrollMonth: string
  grossSalary: number
  pfDeduction: number
  esiDeduction: number
  taxDeduction: number
  netSalary: number
  status: string
}

export async function getMyPayrollHistory() {
  const res = await api.get<PayrollRecord[]>('/api/payroll/my')
  return res.data
}

export async function getPayrollByMonth(payrollMonth: string) {
  const res = await api.get<PayrollRecord[]>(`/api/payroll/month/${payrollMonth}`)
  return res.data
}

async function downloadPdf(url: string, filename: string) {
  const res = await api.get(url, { responseType: 'blob' })
  const blobUrl = window.URL.createObjectURL(new Blob([res.data]))
  const link = document.createElement('a')
  link.href = blobUrl
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(blobUrl)
}

export function downloadMyPayslip(payrollId: number) {
  return downloadPdf(`/api/payroll/my/${payrollId}/payslip`, `payslip-${payrollId}.pdf`)
}

export function downloadPayslip(payrollId: number) {
  return downloadPdf(`/api/payroll/${payrollId}/payslip`, `payslip-${payrollId}.pdf`)
}

export async function approvePayroll(payrollId: number) {
  const res = await api.put<PayrollRecord>(`/api/payroll/${payrollId}/approve`)
  return res.data
}

export async function lockPayroll(payrollId: number) {
  const res = await api.put<PayrollRecord>(`/api/payroll/${payrollId}/lock`)
  return res.data
}

export async function markPayrollPaid(payrollId: number) {
  const res = await api.put<PayrollRecord>(`/api/payroll/${payrollId}/paid`)
  return res.data
}
