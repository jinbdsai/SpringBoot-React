import { useState } from 'react'

export default function LoginForm({ onSubmit, onGoRegister, onCancel }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    if (!username.trim() || !password) {
      setError('아이디와 비밀번호를 입력해주세요.')
      return
    }
    setError('')
    setSubmitting(true)
    try {
      await onSubmit(username.trim(), password)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-card">
      <h2 className="auth-card__title">로그인</h2>
      <form onSubmit={submit}>
        {error && <div className="auth-card__error">{error}</div>}
        <div className="auth-card__field">
          <label htmlFor="login-username">아이디</label>
          <input
            id="login-username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            autoFocus
          />
        </div>
        <div className="auth-card__field">
          <label htmlFor="login-password">비밀번호</label>
          <input
            id="login-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </div>
        <button type="submit" className="btn btn--primary auth-card__submit" disabled={submitting}>
          {submitting ? '로그인 중...' : '로그인'}
        </button>
      </form>
      <div className="auth-card__links">
        <button type="button" className="auth-card__link" onClick={onGoRegister}>
          회원가입
        </button>
        <button type="button" className="auth-card__link" onClick={onCancel}>
          취소
        </button>
      </div>
      <p className="auth-card__hint">
        기본 계정: <code>admin / admin</code>
      </p>
    </div>
  )
}
