import { useEffect, useMemo, useState } from 'react'
import { filterTransactions, getTransactions } from '../api/transactions'
import { getAiAnalysis, getFraudCheck } from '../api/insights'
import { FINANCIAL_LABELS } from '../config/labels'
import { Select } from '../components/Select.jsx'

function formatMoney(value) {
  const num = Number(value || 0)
  return num.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function DashboardPage() {
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [ai, setAi] = useState(null)
  const [fraud, setFraud] = useState(null)
  const [insightsLoading, setInsightsLoading] = useState(false)

  const [category, setCategory] = useState('')
  const [type, setType] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  async function loadAll() {
    setError('')
    setLoading(true)
    try {
      const data = await getTransactions()
      setRows(data)
    } catch (err) {
      setError(err?.response?.data?.error || 'Failed to fetch transactions')
    } finally {
      setLoading(false)
    }
  }

  async function applyFilter() {
    setError('')
    setLoading(true)
    try {
      const data = await filterTransactions({ category, type, startDate, endDate })
      setRows(data)
    } catch (err) {
      setError(err?.response?.data?.error || 'Failed to filter transactions')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadAll()
  }, [])

  async function loadInsights() {
    setInsightsLoading(true)
    try {
      const [aiData, fraudData] = await Promise.all([getAiAnalysis(), getFraudCheck()])
      setAi(aiData)
      setFraud(fraudData)
    } catch {
      // keep dashboard usable even if insights fail
      setAi(null)
      setFraud(null)
    } finally {
      setInsightsLoading(false)
    }
  }

  useEffect(() => {
    loadInsights()
  }, [])

  const totalSpending = useMemo(() => {
    return rows
      .filter((t) => t.type === 'DEBIT')
      .reduce((sum, t) => sum + Number(t.amount || 0), 0)
  }, [rows])

  const totalCredits = useMemo(() => {
    return rows
      .filter((t) => t.type === 'CREDIT')
      .reduce((sum, t) => sum + Number(t.amount || 0), 0)
  }, [rows])

  return (
    <div className="grid" style={{ gap: 16 }}>
      <div className="card">
        <div className="row" style={{ alignItems: 'flex-end' }}>
          <div>
            <div style={{ fontSize: 20, fontWeight: 800 }}>Dashboard</div>
            <div className="muted" style={{ fontSize: 13 }}>
              List transactions, filter by category/type/date, and see totals.
            </div>
          </div>
          <div className="row">
            <div className="badge badge--debit">
              {FINANCIAL_LABELS.totalExpenses} ₹ {formatMoney(totalSpending)}
            </div>
            <div className="badge badge--credit">
              {FINANCIAL_LABELS.totalIncome} ₹ {formatMoney(totalCredits)}
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid--2">
        <div className="card">
          <div className="row">
            <div>
              <div style={{ fontSize: 16, fontWeight: 800 }}>AI Insights</div>
              <div className="muted" style={{ fontSize: 13 }}>
                {ai?.period ? `Period: ${ai.period}` : 'Based on your transactions'}
              </div>
            </div>
            <button className="btn btn--ghost" onClick={loadInsights} disabled={insightsLoading}>
              {insightsLoading ? 'Refreshing…' : 'Refresh'}
            </button>
          </div>

          {ai?.insights?.length ? (
            <ul style={{ marginTop: 10, paddingLeft: 18 }}>
              {ai.insights.slice(0, 6).map((x, i) => (
                <li key={i} className="muted" style={{ marginBottom: 6 }}>
                  <span style={{ color: 'rgba(255,255,255,0.9)' }}>{x}</span>
                </li>
              ))}
            </ul>
          ) : (
            <div className="muted" style={{ marginTop: 10, fontSize: 13 }}>
              No insights yet.
            </div>
          )}

          {ai?.suggestions?.length ? (
            <div style={{ marginTop: 12 }}>
              <div className="muted" style={{ fontSize: 13, marginBottom: 6 }}>
                Suggestions
              </div>
              <ul style={{ paddingLeft: 18 }}>
                {ai.suggestions.slice(0, 4).map((x, i) => (
                  <li key={i} style={{ marginBottom: 6 }}>
                    {x}
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
        </div>

        <div className="card">
          <div style={{ fontSize: 16, fontWeight: 800 }}>Fraud Check</div>
          <div className="muted" style={{ fontSize: 13 }}>
            Rule-based flags (amount & activity)
          </div>

          {fraud?.suspicious ? (
            <div style={{ marginTop: 12 }}>
              <div className="error">
                <b>Warning:</b> Suspicious activity detected.
              </div>
              <ul style={{ marginTop: 10, paddingLeft: 18 }}>
                {fraud.reasons?.map((r, i) => (
                  <li key={i} style={{ marginBottom: 6 }}>
                    {r}
                  </li>
                ))}
              </ul>
            </div>
          ) : (
            <div
              className="card"
              style={{
                marginTop: 12,
                background: 'rgba(34,197,94,0.12)',
                borderColor: 'rgba(34,197,94,0.35)',
              }}
            >
              No suspicious activity found.
            </div>
          )}
        </div>
      </div>

      <div className="card">
        <div className="grid grid--2">
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Category (exact)
            </div>
            <input className="input" value={category} onChange={(e) => setCategory(e.target.value)} placeholder="food / travel / rent" />
          </div>
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Type
            </div>
            <Select
              value={type}
              onChange={setType}
              placeholder="All"
              options={[
                { value: '', label: 'All' },
                { value: 'DEBIT', label: 'DEBIT' },
                { value: 'CREDIT', label: 'CREDIT' },
              ]}
            />
          </div>
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              Start date
            </div>
            <input className="input" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
          </div>
          <div className="grid" style={{ gap: 6 }}>
            <div className="muted" style={{ fontSize: 13 }}>
              End date
            </div>
            <input className="input" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
          </div>
        </div>

        <div className="row" style={{ marginTop: 12 }}>
          <div className="muted" style={{ fontSize: 13 }}>
            Showing <b>{rows.length}</b> transactions
          </div>
          <div className="row">
            <button className="btn" disabled={loading} onClick={loadAll}>
              Reset
            </button>
            <button className="btn btn--primary" disabled={loading} onClick={applyFilter}>
              Apply filter
            </button>
          </div>
        </div>

        {error ? <div className="error" style={{ marginTop: 12 }}>{error}</div> : null}
      </div>

      <div className="card" style={{ overflowX: 'auto' }}>
        <table className="table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Category</th>
              <th>Type</th>
              <th>Amount (₹)</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={4} className="muted">
                  Loading…
                </td>
              </tr>
            ) : rows.length === 0 ? (
              <tr>
                <td colSpan={4} className="muted">
                  No transactions yet.
                </td>
              </tr>
            ) : (
              rows.map((t) => (
                <tr key={t.id}>
                  <td>{t.date}</td>
                  <td>{t.category}</td>
                  <td>
                    <span className={`badge ${t.type === 'DEBIT' ? 'badge--debit' : 'badge--credit'}`}>{t.type}</span>
                  </td>
                  <td style={{ fontWeight: 800 }}>{formatMoney(t.amount)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

