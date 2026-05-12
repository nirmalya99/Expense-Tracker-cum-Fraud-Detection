import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { loginUser } from '../api/auth'
import { setAuthToken } from '../api/http'
import { useAuth } from '../state/AuthContext.jsx'

export function LoginPage() {
  const navigate = useNavigate()
  const auth = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await loginUser({ email, password })
      auth.login({ token: data.token, email })
      setAuthToken(data.token)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err?.response?.data?.error || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <div className="card" style={{ maxWidth: 520, margin: '40px auto' }}>
        <div style={{ marginBottom: 10 }}>
          <div style={{ fontSize: 22, fontWeight: 800 }}>Login</div>
          <div className="muted" style={{ fontSize: 13 }}>
            Use your email + password to get a JWT token.
          </div>
        </div>

        {error ? <div className="error">{error}</div> : null}

        <form className="grid" onSubmit={onSubmit} style={{ marginTop: 12 }}>
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Email
            </div>
            <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
          </div>

          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Password
            </div>
            <input
              className="input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          <button className="btn btn--primary" disabled={loading || !email || !password}>
            {loading ? 'Logging in…' : 'Login'}
          </button>
        </form>

        <div className="muted" style={{ marginTop: 12, fontSize: 13 }}>
          New user? <Link to="/register" style={{ color: 'white' }}>Create an account</Link>
        </div>
      </div>
    </div>
  )
}

