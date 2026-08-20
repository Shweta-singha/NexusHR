import { useEffect, useState } from 'react'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { tokenStorage } from '@/lib/api'

export interface AttendanceLiveEvent {
  eventType: 'CHECK_IN' | 'CHECK_OUT'
  employeeId: number
  username: string
  timestamp: string
}

export function useAttendanceStream() {
  const [events, setEvents] = useState<AttendanceLiveEvent[]>([])
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    fetchEventSource(`${import.meta.env.VITE_API_BASE_URL}/api/attendance/stream`, {
      headers: {
        Authorization: `Bearer ${tokenStorage.getAccessToken()}`,
      },
      signal: controller.signal,
      openWhenHidden: true,
      onopen: async (response) => {
        if (response.ok) {
          setConnected(true)
          return
        }
        throw new Error(`SSE connection failed: ${response.status}`)
      },
      onmessage(msg) {
        if (msg.event !== 'attendance') return
        const parsed = JSON.parse(msg.data) as AttendanceLiveEvent
        setEvents((prev) => [parsed, ...prev].slice(0, 20))
      },
      onerror(err) {
        setConnected(false)
        // fetch-event-source retries automatically unless we rethrow
        console.error('SSE error', err)
      },
      onclose() {
        setConnected(false)
      },
    })

    return () => controller.abort()
  }, [])

  return { events, connected }
}
