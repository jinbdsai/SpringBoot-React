const BASE = '/api/posts'

async function handle(res) {
  if (!res.ok) {
    const message = await res.text()
    throw new Error(message || `Request failed: ${res.status}`)
  }
  if (res.status === 204) return null
  return res.json()
}

const json = (method, body) => ({
  method,
  credentials: 'include',
  headers: body ? { 'Content-Type': 'application/json' } : undefined,
  body: body ? JSON.stringify(body) : undefined,
})

export const postsApi = {
  list: ({ category, sort } = {}) => {
    const qs = new URLSearchParams()
    if (category) qs.set('category', category)
    if (sort) qs.set('sort', sort)
    const url = qs.toString() ? `${BASE}?${qs}` : BASE
    return fetch(url, { credentials: 'include' }).then(handle)
  },
  get: (id) => fetch(`${BASE}/${id}`, { credentials: 'include' }).then(handle),
  create: (data) => fetch(BASE, json('POST', data)).then(handle),
  update: (id, data) => fetch(`${BASE}/${id}`, json('PUT', data)).then(handle),
  remove: (id) => fetch(`${BASE}/${id}`, json('DELETE')).then(handle),
}
