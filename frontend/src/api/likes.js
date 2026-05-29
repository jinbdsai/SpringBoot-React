async function handle(res) {
  if (!res.ok) throw new Error(await res.text() || `Request failed: ${res.status}`)
  return res.json()
}

export const likesApi = {
  like: (postId) =>
    fetch(`/api/posts/${postId}/like`, { method: 'POST', credentials: 'include' }).then(handle),
  unlike: (postId) =>
    fetch(`/api/posts/${postId}/like`, { method: 'DELETE', credentials: 'include' }).then(handle),
}
