import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from '@/features/auth/AuthContext'
import { ThemeProvider } from '@/features/theme/ThemeContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import Layout from '@/components/Layout'
import LoginPage from '@/features/auth/LoginPage'
import DashboardPage from '@/features/dashboard/DashboardPage'
import EmployeeListPage from '@/features/employees/EmployeeListPage'
import AttendancePage from '@/features/attendance/AttendancePage'
import LeavePage from '@/features/leave/LeavePage'
import PayrollPage from '@/features/payroll/PayrollPage'
import PerformancePage from '@/features/performance/PerformancePage'
import AiInsightsPage from '@/features/ai/AiInsightsPage'

const queryClient = new QueryClient()

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <BrowserRouter>
          <AuthProvider>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route
                element={
                  <ProtectedRoute>
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route path="/" element={<DashboardPage />} />
                <Route
                  path="/employees"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'HR_MANAGER']}>
                      <EmployeeListPage />
                    </ProtectedRoute>
                  }
                />
                <Route path="/attendance" element={<AttendancePage />} />
                <Route path="/leave" element={<LeavePage />} />
                <Route path="/payroll" element={<PayrollPage />} />
                <Route path="/performance" element={<PerformancePage />} />
                <Route path="/ai-insights" element={<AiInsightsPage />} />
              </Route>
            </Routes>
          </AuthProvider>
        </BrowserRouter>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
