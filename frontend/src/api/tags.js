async function handle(res) {
  if (!res.ok) throw new Error(await res.text() || `Request failed: ${res.status}`)
  return res.json()
}

export const tagsApi = {
  list: () => fetch('/api/tags', { credentials: 'include' }).then(handle),
  popular: () => fetch('/api/tags/popular', { credentials: 'include' }).then(handle),
}
