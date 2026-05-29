import { useState } from 'react'

export default function CommentItem({ comment, replies, currentUser, onReply, onUpdate, onDelete }) {
  const [editing, setEditing] = useState(false)
  const [editText, setEditText] = useState(comment.content)
  const [replying, setReplying] = useState(false)
  const [replyText, setReplyText] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const isOwner = currentUser && currentUser.username === comment.author
  const isDeleted = comment.deleted

  const handleUpdate = async () => {
    if (!editText.trim()) return
    setSubmitting(true)
    try {
      await onUpdate(comment.id, editText.trim())
      setEditing(false)
    } finally {
      setSubmitting(false)
    }
  }

  const handleReply = async () => {
    if (!replyText.trim()) return
    setSubmitting(true)
    try {
      await onReply(comment.id, replyText.trim())
      setReplyText('')
      setReplying(false)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="comment">
      <div className="comment__head">
        <span className="comment__author">{comment.author}</span>
        <span className="comment__date">{formatDate(comment.createdAt)}</span>
        {comment.updatedAt !== comment.createdAt && <span className="comment__edited">(수정됨)</span>}
      </div>

      {editing ? (
        <div className="comment__edit">
          <textarea
            rows={3}
            value={editText}
            onChange={(e) => setEditText(e.target.value)}
          />
          <div className="comment__edit-actions">
            <button type="button" className="btn" onClick={() => setEditing(false)}>취소</button>
            <button type="button" className="btn btn--primary" onClick={handleUpdate} disabled={submitting}>저장</button>
          </div>
        </div>
      ) : (
        <div className={`comment__body ${isDeleted ? 'comment__body--deleted' : ''}`}>
          {isDeleted ? <em>삭제된 댓글입니다.</em> : comment.content}
        </div>
      )}

      {!isDeleted && !editing && (
        <div className="comment__actions">
          {currentUser && (
            <button type="button" className="comment__action" onClick={() => setReplying(!replying)}>
              ↩ 답글
            </button>
          )}
          {isOwner && (
            <>
              <button type="button" className="comment__action" onClick={() => setEditing(true)}>수정</button>
              <button type="button" className="comment__action comment__action--danger" onClick={() => onDelete(comment.id)}>삭제</button>
            </>
          )}
        </div>
      )}

      {replying && (
        <div className="comment__reply-form">
          <textarea
            rows={2}
            placeholder={`@${comment.author}님에게 답글...`}
            value={replyText}
            onChange={(e) => setReplyText(e.target.value)}
          />
          <div className="comment__edit-actions">
            <button type="button" className="btn" onClick={() => setReplying(false)}>취소</button>
            <button type="button" className="btn btn--primary" onClick={handleReply} disabled={submitting || !replyText.trim()}>등록</button>
          </div>
        </div>
      )}

      {replies && replies.length > 0 && (
        <div className="comment__replies">
          {replies.map((r) => (
            <CommentItem
              key={r.id}
              comment={r}
              replies={[]}
              currentUser={currentUser}
              onReply={onReply}
              onUpdate={onUpdate}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </div>
  )
}

function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const now = Date.now()
  const diff = (now - d.getTime()) / 1000
  if (diff < 60) return '방금 전'
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`
  if (diff < 604800) return `${Math.floor(diff / 86400)}일 전`
  return d.toLocaleString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
