const BASE = '/api/posts'

async function handle(res) {
  if (!res.ok) {
    const message = await res.text()
    throw new Error(message || `Request failed: ${res.status}`)
  }
  if (res.status === 204) return null
  return res.json()
}

export const postsApi = {
  list: () => fetch(BASE, { credentials: 'include' }).then(handle),
  get: (id) => fetch(`${BASE}/${id}`, { credentials: 'include' }).then(handle),
  create: (data) =>
    fetch(BASE, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(handle),
  update: (id, data) =>
    fetch(`${BASE}/${id}`, {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(handle),
  remove: (id) =>
    fetch(`${BASE}/${id}`, {
      method: 'DELETE',
      credentials: 'include',
    }).then(handle),
}
