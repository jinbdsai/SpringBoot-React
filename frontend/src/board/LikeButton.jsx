import { useState } from 'react'
import { likesApi } from '../api/likes'

export default function LikeButton({ postId, likedByMe, likeCount, currentUser, onChange }) {
  const [busy, setBusy] = useState(false)

  const toggle = async () => {
    if (!currentUser) {
      alert('로그인이 필요합니다.')
      return
    }
    setBusy(true)
    try {
      if (likedByMe) {
        await likesApi.unlike(postId)
        onChange({ likedByMe: false, likeCount: Math.max(0, likeCount - 1) })
      } else {
        await likesApi.like(postId)
        onChange({ likedByMe: true, likeCount: likeCount + 1 })
      }
    } catch (e) {
      alert(e.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <button
      type="button"
      className={`like-btn ${likedByMe ? 'is-liked' : ''}`}
      onClick={toggle}
      disabled={busy}
    >
      <span className="like-btn__icon">{likedByMe ? '❤️' : '🤍'}</span>
      <span className="like-btn__count">{likeCount}</span>
    </button>
  )
}
