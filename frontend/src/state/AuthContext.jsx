import { createContext, useContext, useMemo, useState } from 'react'

const TOKEN_KEY = 'auth_token'
const EMAIL_KEY = 'auth_email'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY) || '')
  const [email, setEmail] = useState(() => localStorage.getItem(EMAIL_KEY) || '')

  const value = useMemo(() => {
    return {
      token,
      email,
      login: ({ token: nextToken, email: nextEmail }) => {
        localStorage.setItem(TOKEN_KEY, nextToken)
        localStorage.setItem(EMAIL_KEY, nextEmail)
        setToken(nextToken)
        setEmail(nextEmail)
      },
      logout: () => {
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(EMAIL_KEY)
        setToken('')
        setEmail('')
      },
    }
  }, [token, email])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

