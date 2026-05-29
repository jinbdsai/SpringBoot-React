import { useState } from 'react'
import MarkdownView from './MarkdownView'
import LikeButton from './LikeButton'
import CommentSection from './CommentSection'
import { categoryLabel } from './CategorySelector'

export default function PostDetail({ post: initialPost, currentUser, onBack, onEdit, onDelete, onSelectAuthor }) {
  const [post, setPost] = useState(initialPost)

  if (!post) return null
  const isOwner = currentUser && post.author === currentUser.username

  const onLikeChange = ({ likedByMe, likeCount }) => {
    setPost({ ...post, likedByMe, likeCount })
  }

  return (
    <article className="post-detail">
      <header className="post-detail__header">
        <div className="post-detail__cat">{categoryLabel(post.category)}</div>
        <h2 className="post-detail__title">{post.title}</h2>
        <div className="post-detail__meta">
          <button className="post-detail__author-link" onClick={() => onSelectAuthor?.(post.author)}>
            {post.author}
          </button>
          <span className="post-detail__sep">·</span>
          <span>{formatDateTime(post.createdAt)}</span>
          <span className="post-detail__sep">·</span>
          <span>조회 {post.viewCount ?? 0}</span>
          <span className="post-detail__sep">·</span>
          <span>댓글 {post.commentCount ?? 0}</span>
        </div>
        {post.tags && post.tags.length > 0 && (
          <div className="post-detail__tags">
            {post.tags.map((t) => (
              <span key={t} className="tag-chip tag-chip--readonly">#{t}</span>
            ))}
          </div>
        )}
      </header>

      <div className="post-detail__body">
        <MarkdownView content={post.content} />
      </div>

      <div className="post-detail__like-row">
        <LikeButton
          postId={post.id}
          likedByMe={post.likedByMe}
          likeCount={post.likeCount ?? 0}
          currentUser={currentUser}
          onChange={onLikeChange}
        />
      </div>

      <footer className="post-detail__footer">
        <button className="btn" onClick={onBack}>목록</button>
        {isOwner && (
          <div className="post-detail__actions">
            <button className="btn" onClick={() => onEdit(post)}>수정</button>
            <button className="btn btn--danger" onClick={() => onDelete(post.id)}>삭제</button>
          </div>
        )}
      </footer>

      <CommentSection
        postId={post.id}
        currentUser={currentUser}
        onCountChange={(n) => setPost((p) => ({ ...p, commentCount: n }))}
      />
    </article>
  )
}

function formatDateTime(iso) {
  return new Date(iso).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}
