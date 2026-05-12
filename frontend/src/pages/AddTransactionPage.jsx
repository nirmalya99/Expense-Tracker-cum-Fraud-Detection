import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { addTransaction } from '../api/transactions'
import { Select } from '../components/Select.jsx'

export function AddTransactionPage() {
  const navigate = useNavigate()
  const [amount, setAmount] = useState('')
  const [type, setType] = useState('DEBIT')
  const [category, setCategory] = useState('')
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await addTransaction({ amount: Number(amount), type, category, date })
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err?.response?.data?.error || 'Failed to add transaction')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card" style={{ maxWidth: 720, margin: '0 auto' }}>
      <div style={{ marginBottom: 10 }}>
        <div style={{ fontSize: 20, fontWeight: 800 }}>Add transaction</div>
        <div className="muted" style={{ fontSize: 13 }}>
          This calls the secured API using your JWT token.
        </div>
      </div>

      {error ? <div className="error">{error}</div> : null}

      <form className="grid" onSubmit={onSubmit} style={{ marginTop: 12 }}>
        <div className="grid grid--2">
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Amount (₹)
            </div>
            <input
              className="input"
              type="number"
              step="0.01"
              min="0"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="5000"
            />
          </div>

          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Type
            </div>
            <Select
              value={type}
              onChange={setType}
              options={[
                { value: 'DEBIT', label: 'DEBIT' },
                { value: 'CREDIT', label: 'CREDIT' },
              ]}
            />
          </div>
        </div>

        <div className="grid grid--2">
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Category
            </div>
            <input className="input" value={category} onChange={(e) => setCategory(e.target.value)} placeholder="food / travel / salary" />
          </div>
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Date
            </div>
            <input className="input" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </div>
        </div>

        <div className="row" style={{ justifyContent: 'flex-end' }}>
          <button className="btn" type="button" onClick={() => navigate('/dashboard')}>
            Cancel
          </button>
          <button className="btn btn--primary" disabled={loading || !amount || Number(amount) <= 0 || !category || !date}>
            {loading ? 'Saving…' : 'Save'}
          </button>
        </div>
      </form>
    </div>
  )
}

