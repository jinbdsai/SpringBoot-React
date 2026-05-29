export default function PostDetail({ post, currentUser, onBack, onEdit, onDelete }) {
  if (!post) return null

  const isOwner = currentUser && post.author === currentUser.username

  const formatDate = (iso) =>
    new Date(iso).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })

  return (
    <article className="post-detail">
      <header className="post-detail__header">
        <h2 className="post-detail__title">{post.title}</h2>
        <div className="post-detail__meta">
          <span className="post-detail__author">{post.author}</span>
          <span className="post-detail__sep">·</span>
          <span>{formatDate(post.createdAt)}</span>
          <span className="post-detail__sep">·</span>
          <span>조회 {post.viewCount ?? 0}</span>
        </div>
      </header>

      <div className="post-detail__body">{post.content}</div>

      <footer className="post-detail__footer">
        <button className="btn" onClick={onBack}>
          목록
        </button>
        {isOwner && (
          <div className="post-detail__actions">
            <button className="btn" onClick={() => onEdit(post)}>
              수정
            </button>
            <button className="btn btn--danger" onClick={() => onDelete(post.id)}>
              삭제
            </button>
          </div>
        )}
      </footer>
    </article>
  )
}
