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

export const commentsApi = {
  list: (postId) => fetch(`/api/posts/${postId}/comments`, { credentials: 'include' }).then(handle),
  create: (postId, { content, parentId }) =>
    fetch(`/api/posts/${postId}/comments`, json('POST', { content, parentId })).then(handle),
  update: (commentId, content) => fetch(`/api/comments/${commentId}`, json('PUT', { content })).then(handle),
  remove: (commentId) => fetch(`/api/comments/${commentId}`, json('DELETE')).then(handle),
}
