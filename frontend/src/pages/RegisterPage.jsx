import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { registerUser } from '../api/auth'

export function RegisterPage() {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setDone(false)
    setLoading(true)
    try {
      await registerUser({ name, email, password })
      setDone(true)
      setTimeout(() => navigate('/login', { replace: true }), 500)
    } catch (err) {
      setError(err?.response?.data?.error || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <div className="card" style={{ maxWidth: 560, margin: '40px auto' }}>
        <div style={{ marginBottom: 10 }}>
          <div style={{ fontSize: 22, fontWeight: 800 }}>Register</div>
          <div className="muted" style={{ fontSize: 13 }}>
            Create a user account (stored in MySQL).
          </div>
        </div>

        {error ? <div className="error">{error}</div> : null}
        {done ? (
          <div className="card" style={{ background: 'rgba(34,197,94,0.12)', borderColor: 'rgba(34,197,94,0.35)' }}>
            Registered successfully. Redirecting to login…
          </div>
        ) : null}

        <form className="grid" onSubmit={onSubmit} style={{ marginTop: 12 }}>
          <div className="grid--2 grid">
            <div className="grid" style={{ gap: 6 }}>
              <div className="muted" style={{ fontSize: 13 }}>
                Name
              </div>
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="Your name" />
            </div>
            <div className="grid" style={{ gap: 6 }}>
              <div className="muted" style={{ fontSize: 13 }}>
                Email
              </div>
              <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
            </div>
          </div>

          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Password (min 6 chars)
            </div>
            <input
              className="input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          <button className="btn btn--primary" disabled={loading || !name || !email || password.length < 6}>
            {loading ? 'Creating…' : 'Create account'}
          </button>
        </form>

        <div className="muted" style={{ marginTop: 12, fontSize: 13 }}>
          Already have an account? <Link to="/login" style={{ color: 'white' }}>Login</Link>
        </div>
      </div>
    </div>
  )
}

