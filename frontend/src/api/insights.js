import { http } from './http'

export async function getAiAnalysis() {
  const res = await http.get('/api/ai/analyze')
  return res.data
}

export async function getFraudCheck() {
  const res = await http.get('/api/fraud/check')
  return res.data
}

