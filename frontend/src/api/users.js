async function handle(res) {
  if (!res.ok) throw new Error(await res.text() || `Request failed: ${res.status}`)
  return res.json()
}

export const usersApi = {
  profile: (username) => fetch(`/api/users/${encodeURIComponent(username)}`, { credentials: 'include' }).then(handle),
}
