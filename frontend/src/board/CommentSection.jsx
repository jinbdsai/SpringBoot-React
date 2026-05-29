import { useEffect, useMemo, useState } from 'react'
import { commentsApi } from '../api/comments'
import CommentItem from './CommentItem'

export default function CommentSection({ postId, currentUser, onCountChange }) {
  const [comments, setComments] = useState([])
  const [draft, setDraft] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      const data = await commentsApi.list(postId)
      setComments(data)
      onCountChange?.(data.filter((c) => !c.deleted).length)
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [postId])

  // 부모/자식 그룹핑 (대댓글은 parentId 가 1차 부모를 가리킴)
  const { tops, repliesByParent } = useMemo(() => {
    const tops = []
    const repliesByParent = new Map()
    for (const c of comments) {
      if (c.parentId == null) tops.push(c)
      else {
        if (!repliesByParent.has(c.parentId)) repliesByParent.set(c.parentId, [])
        repliesByParent.get(c.parentId).push(c)
      }
    }
    return { tops, repliesByParent }
  }, [comments])

  const handleCreate = async () => {
    if (!draft.trim()) return
    if (!currentUser) {
      setError('로그인이 필요합니다.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      await commentsApi.create(postId, { content: draft.trim(), parentId: null })
      setDraft('')
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleReply = async (parentId, content) => {
    try {
      await commentsApi.create(postId, { content, parentId })
      await load()
    } catch (e) {
      setError(e.message)
    }
  }

  const handleUpdate = async (commentId, content) => {
    try {
      await commentsApi.update(commentId, content)
      await load()
    } catch (e) {
      setError(e.message)
    }
  }

  const handleDelete = async (commentId) => {
    if (!confirm('댓글을 삭제하시겠습니까?')) return
    try {
      await commentsApi.remove(commentId)
      await load()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <section className="comments">
      <h3 className="comments__title">💬 댓글 {tops.length + [...repliesByParent.values()].flat().filter((c) => !c.deleted).length}</h3>

      {error && <div className="comments__error">{error}</div>}

      {currentUser ? (
        <div className="comments__form">
          <textarea
            rows={3}
            placeholder="댓글을 입력하세요"
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
          />
          <div className="comments__form-actions">
            <button type="button" className="btn btn--primary" onClick={handleCreate} disabled={submitting || !draft.trim()}>
              {submitting ? '등록 중...' : '댓글 등록'}
            </button>
          </div>
        </div>
      ) : (
        <div className="comments__login-hint">댓글을 작성하려면 로그인이 필요합니다.</div>
      )}

      <div className="comments__list">
        {tops.length === 0 ? (
          <div className="comments__empty">첫 댓글을 남겨보세요.</div>
        ) : (
          tops.map((c) => (
            <CommentItem
              key={c.id}
              comment={c}
              replies={repliesByParent.get(c.id) || []}
              currentUser={currentUser}
              onReply={handleReply}
              onUpdate={handleUpdate}
              onDelete={handleDelete}
            />
          ))
        )}
      </div>
    </section>
  )
}
