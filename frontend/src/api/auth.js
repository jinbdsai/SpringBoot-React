const BASE = '/api/auth'

async function handle(res) {
  if (!res.ok) {
    const message = await res.text()
    throw new Error(message || `Request failed: ${res.status}`)
  }
  if (res.status === 204) return null
  return res.json()
}

const opts = (method, body) => ({
  method,
  credentials: 'include',
  headers: body ? { 'Content-Type': 'application/json' } : undefined,
  body: body ? JSON.stringify(body) : undefined,
})

export const authApi = {
  me: () =>
    fetch(`${BASE}/me`, { credentials: 'include' }).then((res) => {
      if (res.status === 401) return null
      return handle(res)
    }),
  login: (username, password) =>
    fetch(`${BASE}/login`, opts('POST', { username, password })).then(handle),
  register: (username, password) =>
    fetch(`${BASE}/register`, opts('POST', { username, password })).then(handle),
  logout: () => fetch(`${BASE}/logout`, opts('POST')).then(handle),
}
