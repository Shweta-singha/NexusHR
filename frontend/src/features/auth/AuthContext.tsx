import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { api, tokenStorage } from '@/lib/api'

export interface CurrentUser {
  username: string
  role: string
}

interface AuthContextValue {
  user: CurrentUser | null
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  register: (input: { username: string; password: string; email: string }) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const token = tokenStorage.getAccessToken()
    if (!token) {
      setIsLoading(false)
      return
    }
    api
      .get<CurrentUser>('/api/auth/me')
      .then((res) => setUser(res.data))
      .catch(() => tokenStorage.clear())
      .finally(() => setIsLoading(false))
  }, [])

  async function login(username: string, password: string) {
    const res = await api.post('/api/auth/login', { username, password })
    tokenStorage.setTokens(res.data.token, res.data.refreshToken)
    const me = await api.get<CurrentUser>('/api/auth/me')
    setUser(me.data)
  }

  async function register(input: { username: string; password: string; email: string }) {
    const res = await api.post('/api/auth/register', input)
    tokenStorage.setTokens(res.data.token, res.data.refreshToken)
    const me = await api.get<CurrentUser>('/api/auth/me')
    setUser(me.data)
  }

  function logout() {
    const refreshToken = tokenStorage.getRefreshToken()
    api.post('/api/auth/logout', { refreshToken }).catch(() => {
      // best-effort — clear local state regardless of whether the server call succeeds
    })
    tokenStorage.clear()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
