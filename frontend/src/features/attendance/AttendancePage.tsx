import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { checkIn, checkOut, getMyAttendance } from './api'
import { useAttendanceStream } from './useAttendanceStream'

function formatTime(value: string | null) {
  if (!value) return '—'
  return new Date(value).toLocaleTimeString()
}

export default function AttendancePage() {
  const queryClient = useQueryClient()
  const { events, connected } = useAttendanceStream()

  const myAttendanceQuery = useQuery({
    queryKey: ['attendance', 'my'],
    queryFn: getMyAttendance,
  })

  const checkInMutation = useMutation({
    mutationFn: checkIn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['attendance', 'my'] }),
  })

  const checkOutMutation = useMutation({
    mutationFn: checkOut,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['attendance', 'my'] }),
  })

  return (
    <div className="grid grid-cols-3 gap-8">
      <div className="col-span-2">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-semibold text-slate-900">Attendance</h1>
          <div className="flex gap-2">
            <button
              onClick={() => checkInMutation.mutate()}
              disabled={checkInMutation.isPending}
              className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
            >
              Check In
            </button>
            <button
              onClick={() => checkOutMutation.mutate()}
              disabled={checkOutMutation.isPending}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-50"
            >
              Check Out
            </button>
          </div>
        </div>

        {checkInMutation.isError && (
          <p className="mb-4 text-sm text-red-600">Check-in failed — you may already be checked in today.</p>
        )}
        {checkOutMutation.isError && (
          <p className="mb-4 text-sm text-red-600">Check-out failed — you may not be checked in.</p>
        )}

        <table className="w-full border-collapse overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
          <thead>
            <tr className="border-b border-slate-200 bg-slate-50 text-left text-slate-600">
              <th className="px-4 py-2 font-medium">Date</th>
              <th className="px-4 py-2 font-medium">Check In</th>
              <th className="px-4 py-2 font-medium">Check Out</th>
              <th className="px-4 py-2 font-medium">Hours</th>
              <th className="px-4 py-2 font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            {myAttendanceQuery.data?.map((record) => (
              <tr key={record.id} className="border-b border-slate-100 last:border-0">
                <td className="px-4 py-2 text-slate-900">{record.date}</td>
                <td className="px-4 py-2 text-slate-600">{formatTime(record.checkIn)}</td>
                <td className="px-4 py-2 text-slate-600">{formatTime(record.checkOut)}</td>
                <td className="px-4 py-2 text-slate-600">{record.workingHours ?? '—'}</td>
                <td className="px-4 py-2 text-slate-600">{record.attendanceStatus}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div>
        <div className="mb-4 flex items-center gap-2">
          <h2 className="text-lg font-semibold text-slate-900">Live Feed</h2>
          <span
            className={`h-2 w-2 rounded-full ${connected ? 'bg-green-500' : 'bg-slate-300'}`}
            title={connected ? 'Connected' : 'Disconnected'}
          />
        </div>
        <div className="space-y-2 rounded-lg border border-slate-200 bg-white p-4">
          {events.length === 0 && <p className="text-sm text-slate-500">No events yet.</p>}
          {events.map((event, i) => (
            <div key={i} className="border-b border-slate-100 pb-2 text-sm last:border-0">
              <span className="font-medium text-slate-900">{event.username}</span>{' '}
              <span className="text-slate-500">
                {event.eventType === 'CHECK_IN' ? 'checked in' : 'checked out'} at{' '}
                {new Date(event.timestamp).toLocaleTimeString()}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
