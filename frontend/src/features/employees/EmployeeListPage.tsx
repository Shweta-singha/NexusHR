import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getAllEmployees, getDepartmentHierarchy } from './api'
import DepartmentTree from './DepartmentTree'

export default function EmployeeListPage() {
  const [page, setPage] = useState(0)

  const employeesQuery = useQuery({
    queryKey: ['employees', page],
    queryFn: () => getAllEmployees(page),
  })

  const departmentsQuery = useQuery({
    queryKey: ['departments', 'hierarchy'],
    queryFn: getDepartmentHierarchy,
  })

  return (
    <div className="grid grid-cols-3 gap-8">
      <div className="col-span-2">
        <h1 className="mb-4 text-2xl font-semibold text-slate-900 dark:text-slate-100">Employees</h1>

        {employeesQuery.isLoading && <p className="text-slate-500 dark:text-slate-400">Loading…</p>}
        {employeesQuery.isError && <p className="text-red-600 dark:text-red-400">Failed to load employees.</p>}

        {employeesQuery.data && (
          <>
            <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm dark:border-slate-700 dark:bg-slate-800">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400">
                  <th className="px-4 py-2 font-medium">Username</th>
                  <th className="px-4 py-2 font-medium">Email</th>
                  <th className="px-4 py-2 font-medium">Role</th>
                </tr>
              </thead>
              <tbody>
                {employeesQuery.data.content.map((emp) => (
                  <tr key={emp.id} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
                    <td className="px-4 py-2 text-slate-900 dark:text-slate-100">{emp.username}</td>
                    <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{emp.email}</td>
                    <td className="px-4 py-2 text-slate-600 dark:text-slate-400">{emp.role}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="mt-4 flex items-center gap-3 text-sm">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600"
              >
                Previous
              </button>
              <span className="text-slate-600 dark:text-slate-400">
                Page {employeesQuery.data.number + 1} of {Math.max(1, employeesQuery.data.totalPages)}
              </span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={page + 1 >= employeesQuery.data.totalPages}
                className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-500 dark:border-slate-600"
              >
                Next
              </button>
            </div>
          </>
        )}
      </div>

      <div>
        <h2 className="mb-4 text-lg font-semibold text-slate-900 dark:text-slate-100">Departments</h2>
        {departmentsQuery.isLoading && <p className="text-slate-500 dark:text-slate-400">Loading…</p>}
        {departmentsQuery.data && (
          <div className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-800">
            <DepartmentTree nodes={departmentsQuery.data} />
          </div>
        )}
      </div>
    </div>
  )
}
