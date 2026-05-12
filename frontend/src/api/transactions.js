import { http } from './http'

export async function addTransaction({ amount, type, category, date }) {
  const res = await http.post('/api/transactions', { amount, type, category, date })
  return res.data
}

export async function getTransactions() {
  const res = await http.get('/api/transactions')
  return res.data
}

export async function filterTransactions({ category, type, startDate, endDate }) {
  const params = {}
  if (category) params.category = category
  if (type) params.type = type
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate

  const res = await http.get('/api/transactions/filter', { params })
  return res.data
}

