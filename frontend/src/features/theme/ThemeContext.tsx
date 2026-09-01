import { createContext, useContext, useState, type ReactNode } from 'react'

export type Theme = 'light' | 'dark'

const COOKIE_NAME = 'nexushr_theme'
// 1 year - a UI preference, not a session credential, so a long-lived cookie
// is appropriate (unlike the JWT tokens in lib/api.ts, which are kept out of
// cookies entirely).
const COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365

interface ThemeContextValue {
  theme: Theme
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)

function readThemeCookie(): Theme {
  const match = document.cookie.match(/(?:^|; )nexushr_theme=(dark|light)/)
  return match?.[1] === 'dark' ? 'dark' : 'light'
}

function writeThemeCookie(theme: Theme) {
  document.cookie = `${COOKIE_NAME}=${theme}; max-age=${COOKIE_MAX_AGE_SECONDS}; path=/; SameSite=Lax`
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  // index.html's inline script already applied the `dark` class before
  // first paint if needed - this just syncs React state to match, rather
  // than re-deciding (and potentially flashing) on mount.
  const [theme, setTheme] = useState<Theme>(() =>
    document.documentElement.classList.contains('dark') ? 'dark' : readThemeCookie(),
  )

  function toggleTheme() {
    setTheme((prev) => {
      const next: Theme = prev === 'dark' ? 'light' : 'dark'
      document.documentElement.classList.toggle('dark', next === 'dark')
      writeThemeCookie(next)
      return next
    })
  }

  return <ThemeContext.Provider value={{ theme, toggleTheme }}>{children}</ThemeContext.Provider>
}

export function useTheme() {
  const ctx = useContext(ThemeContext)
  if (!ctx) throw new Error('useTheme must be used within ThemeProvider')
  return ctx
}
