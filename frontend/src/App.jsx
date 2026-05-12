import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './state/AuthContext.jsx'
import { AppLayout } from './components/AppLayout.jsx'
import { LoginPage } from './pages/LoginPage.jsx'
import { RegisterPage } from './pages/RegisterPage.jsx'
import { DashboardPage } from './pages/DashboardPage.jsx'
import { AddTransactionPage } from './pages/AddTransactionPage.jsx'

function Protected({ children }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        element={
          <Protected>
            <AppLayout />
          </Protected>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/transactions/new" element={<AddTransactionPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
