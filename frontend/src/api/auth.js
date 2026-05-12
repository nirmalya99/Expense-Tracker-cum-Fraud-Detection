import { http } from './http'

export async function registerUser({ name, email, password }) {
  await http.post('/api/auth/register', { name, email, password })
}

export async function loginUser({ email, password }) {
  const res = await http.post('/api/auth/login', { email, password })
  return res.data
}

