import { useEffect, useState } from 'react'
import { postsApi } from '../api/posts'
import { authApi } from '../api/auth'
import PostList from './PostList'
import PostDetail from './PostDetail'
import PostForm from './PostForm'
import ProfilePage from './ProfilePage'
import LoginForm from '../auth/LoginForm'
import RegisterForm from '../auth/RegisterForm'
import './Board.css'

export default function Board() {
  const [view, setView] = useState('list')
  const [pendingAction, setPendingAction] = useState(null)
  const [posts, setPosts] = useState([])
  const [selected, setSelected] = useState(null)
  const [editing, setEditing] = useState(null)
  const [profileUsername, setProfileUsername] = useState(null)
  const [currentUser, setCurrentUser] = useState(null)
  const [authChecked, setAuthChecked] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [category, setCategory] = useState(null)
  const [sort, setSort] = useState('latest')

  const loadPosts = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await postsApi.list({ category, sort })
      setPosts(data)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    ;(async () => {
      try {
        const me = await authApi.me()
        setCurrentUser(me)
      } catch (e) {/* ignore */} finally {
        setAuthChecked(true)
      }
      await loadPosts()
    })()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (view === 'list') loadPosts()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [category, sort])

  const goList = async () => {
    setSelected(null); setEditing(null); setProfileUsername(null); setPendingAction(null)
    setView('list')
    await loadPosts()
  }

  const goDetail = async (id) => {
    try {
      const post = await postsApi.get(id)
      setSelected(post); setView('detail')
    } catch (e) { setError(e.message) }
  }

  const goProfile = (username) => {
    setProfileUsername(username); setView('profile')
  }

  const requireAuth = (action) => {
    if (currentUser) action()
    else { setPendingAction(() => action); setView('login') }
  }

  const goWrite = () => requireAuth(() => { setEditing(null); setView('write') })
  const goEdit = (post) => requireAuth(() => { setEditing(post); setView('edit') })
  const goLogin = () => { setPendingAction(null); setView('login') }
  const goRegister = () => setView('register')

  const handleCreate = async (form) => {
    await postsApi.create(form); await goList()
  }

  const handleUpdate = async (form) => {
    const updated = await postsApi.update(editing.id, form)
    setSelected(updated); setEditing(null); setView('detail')
    await loadPosts()
  }

  const handleDelete = async (id) => {
    requireAuth(async () => {
      if (!confirm('정말 삭제하시겠습니까?')) return
      try { await postsApi.remove(id); await goList() } catch (e) { setError(e.message) }
    })
  }

  const handleLogin = async (username, password) => {
    const user = await authApi.login(username, password)
    setCurrentUser(user); setError('')
    if (pendingAction) { const a = pendingAction; setPendingAction(null); a() } else setView('list')
  }

  const handleRegister = async (username, password) => {
    const user = await authApi.register(username, password)
    setCurrentUser(user); setError('')
    if (pendingAction) { const a = pendingAction; setPendingAction(null); a() } else setView('list')
  }

  const handleLogout = async () => {
    try { await authApi.logout() } catch (e) {/* ignore */}
    setCurrentUser(null); await goList()
  }

  return (
    <div className="board-page">
      <header className="board-page__header">
        <div className="board-page__header-row">
          <div>
            <h1 className="board-page__title" onClick={goList} role="button">💬 자유 게시판</h1>
            <p className="board-page__subtitle">자유롭게 이야기를 나눠보세요</p>
          </div>
          {authChecked && (
            <div className="board-page__user">
              {currentUser ? (
                <>
                  <button className="board-page__welcome-btn" onClick={() => goProfile(currentUser.username)}>
                    <strong>{currentUser.username}</strong>님
                  </button>
                  <button className="btn" onClick={handleLogout}>로그아웃</button>
                </>
              ) : (
                <>
                  <button className="btn" onClick={goLogin}>로그인</button>
                  <button className="btn btn--primary" onClick={goRegister}>회원가입</button>
                </>
              )}
            </div>
          )}
        </div>
      </header>

      {error && <div className="board-page__error">{error}</div>}

      <main className="board-page__main">
        {view === 'list' && (
          <PostList
            posts={posts} loading={loading}
            onSelect={goDetail} onWrite={goWrite} onSelectAuthor={goProfile}
            category={category} setCategory={setCategory}
            sort={sort} setSort={setSort}
          />
        )}
        {view === 'detail' && (
          <PostDetail post={selected} currentUser={currentUser}
            onBack={goList} onEdit={goEdit} onDelete={handleDelete} onSelectAuthor={goProfile} />
        )}
        {view === 'write' && (
          <PostForm currentUser={currentUser} onSubmit={handleCreate} onCancel={goList} />
        )}
        {view === 'edit' && (
          <PostForm initial={editing} currentUser={currentUser}
            onSubmit={handleUpdate}
            onCancel={() => { setEditing(null); setView('detail') }} />
        )}
        {view === 'profile' && profileUsername && (
          <ProfilePage username={profileUsername} onBack={goList} onSelectPost={goDetail} />
        )}
        {view === 'login' && (
          <LoginForm onSubmit={handleLogin} onGoRegister={goRegister} onCancel={goList} />
        )}
        {view === 'register' && (
          <RegisterForm onSubmit={handleRegister} onGoLogin={goLogin} onCancel={goList} />
        )}
      </main>

      <footer className="board-page__footer">총 {posts.length}개의 글</footer>
    </div>
  )
}
