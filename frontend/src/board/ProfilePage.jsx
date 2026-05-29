import { useEffect, useState } from 'react'
import { usersApi } from '../api/users'
import { categoryLabel } from './CategorySelector'

export default function ProfilePage({ username, onBack, onSelectPost }) {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    setError('')
    usersApi
      .profile(username)
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [username])

  if (loading) return <div className="profile">불러오는 중...</div>
  if (error) return <div className="profile profile__error">{error}</div>
  if (!data) return null

  const { user, postCount, totalLikes, totalViews, joinedAt, posts } = data

  return (
    <div className="profile">
      <header className="profile__header">
        <div className="profile__avatar">{user.username[0]?.toUpperCase()}</div>
        <div>
          <h2 className="profile__name">{user.username}</h2>
          <p className="profile__joined">가입일: {new Date(joinedAt).toLocaleDateString('ko-KR')}</p>
        </div>
      </header>

      <div className="profile__stats">
        <Stat label="작성글" value={postCount} />
        <Stat label="받은 좋아요" value={totalLikes} />
        <Stat label="총 조회" value={totalViews} />
      </div>

      <section className="profile__posts">
        <h3 className="profile__section-title">📝 {user.username}님의 글</h3>
        {posts.length === 0 ? (
          <div className="profile__empty">작성한 글이 없습니다.</div>
        ) : (
          <ul className="profile__post-list">
            {posts.map((p) => (
              <li key={p.id} className="profile__post-item" onClick={() => onSelectPost(p.id)}>
                <span className="profile__post-cat">{categoryLabel(p.category)}</span>
                <span className="profile__post-title">{p.title}</span>
                <span className="profile__post-meta">
                  ❤️ {p.likeCount} · 💬 {p.commentCount} · 👁 {p.viewCount}
                </span>
                <span className="profile__post-date">{new Date(p.createdAt).toLocaleDateString('ko-KR')}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <div className="profile__footer">
        <button className="btn" onClick={onBack}>목록으로</button>
      </div>
    </div>
  )
}

function Stat({ label, value }) {
  return (
    <div className="stat">
      <div className="stat__value">{value}</div>
      <div className="stat__label">{label}</div>
    </div>
  )
}
