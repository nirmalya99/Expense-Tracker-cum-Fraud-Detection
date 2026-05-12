import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../state/AuthContext.jsx'
import { setAuthToken } from '../api/http.js'

export function AppLayout() {
  const { email, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="brand__title">Expense Tracker</div>
        </div>

        <nav className="nav">
          <Link to="/dashboard" className="nav__link">
            Dashboard
          </Link>
          <Link to="/transactions/new" className="nav__link nav__link--primary">
            + Add Transaction
          </Link>
        </nav>

        <div className="user">
          <div className="user__email">{email || 'Logged in'}</div>
          <button
            className="btn btn--ghost"
            onClick={() => {
              logout()
              setAuthToken('')
              navigate('/login', { replace: true })
            }}
          >
            Logout
          </button>
        </div>
      </header>

      <main className="container">
        <Outlet />
      </main>
    </div>
  )
}

