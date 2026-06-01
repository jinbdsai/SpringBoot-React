const BASE = '/api/notifications'

async function handle(res) {
  if (!res.ok) {
    const message = await res.text()
    throw new Error(message || `Request failed: ${res.status}`)
  }
  if (res.status === 204) return null
  return res.json()
}

export const notificationsApi = {
  list: () => fetch(BASE, { credentials: 'include' }).then(handle),
  unreadCount: () => fetch(`${BASE}/unread-count`, { credentials: 'include' }).then(handle),
  markRead: (id) => fetch(`${BASE}/${id}/read`, { method: 'POST', credentials: 'include' }).then(handle),
  markAllRead: () => fetch(`${BASE}/read-all`, { method: 'POST', credentials: 'include' }).then(handle),
}
