async function handle(res) {
  if (!res.ok) throw new Error(await res.text() || `Request failed: ${res.status}`)
  return res.json()
}

export const mediaApi = {
  upload: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    return fetch('/api/media', { method: 'POST', body: fd, credentials: 'include' }).then(handle)
  },
}
