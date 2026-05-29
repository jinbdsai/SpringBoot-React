import { useState } from 'react'

export default function RegisterForm({ onSubmit, onGoLogin, onCancel }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    if (!username.trim() || !password) {
      setError('아이디와 비밀번호를 입력해주세요.')
      return
    }
    if (password !== passwordConfirm) {
      setError('비밀번호 확인이 일치하지 않습니다.')
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
      <h2 className="auth-card__title">회원가입</h2>
      <form onSubmit={submit}>
        {error && <div className="auth-card__error">{error}</div>}
        <div className="auth-card__field">
          <label htmlFor="reg-username">아이디</label>
          <input
            id="reg-username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            placeholder="3~30자, 영문/숫자/언더스코어"
            autoFocus
          />
        </div>
        <div className="auth-card__field">
          <label htmlFor="reg-password">비밀번호</label>
          <input
            id="reg-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            placeholder="4자 이상"
          />
        </div>
        <div className="auth-card__field">
          <label htmlFor="reg-password-confirm">비밀번호 확인</label>
          <input
            id="reg-password-confirm"
            type="password"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
            autoComplete="new-password"
          />
        </div>
        <button type="submit" className="btn btn--primary auth-card__submit" disabled={submitting}>
          {submitting ? '가입 중...' : '회원가입'}
        </button>
      </form>
      <div className="auth-card__links">
        <button type="button" className="auth-card__link" onClick={onGoLogin}>
          이미 계정이 있어요
        </button>
        <button type="button" className="auth-card__link" onClick={onCancel}>
          취소
        </button>
      </div>
    </div>
  )
}
