import { useState } from 'react'

export default function PostForm({ initial, currentUser, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    title: initial?.title ?? '',
    content: initial?.content ?? '',
  })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const isEdit = Boolean(initial)
  const author = isEdit ? initial.author : currentUser?.username

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const submit = async (e) => {
    e.preventDefault()
    if (!form.title.trim() || !form.content.trim()) {
      setError('제목과 내용을 모두 입력해주세요.')
      return
    }
    setError('')
    setSubmitting(true)
    try {
      await onSubmit(form)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="post-form" onSubmit={submit}>
      <h2 className="post-form__title">{isEdit ? '글 수정' : '새 글 작성'}</h2>

      {error && <div className="post-form__error">{error}</div>}

      <div className="post-form__field">
        <label>작성자</label>
        <input type="text" value={author ?? ''} disabled />
      </div>

      <div className="post-form__field">
        <label htmlFor="title">제목</label>
        <input
          id="title"
          name="title"
          type="text"
          placeholder="제목을 입력하세요"
          value={form.title}
          onChange={onChange}
          maxLength={200}
        />
      </div>

      <div className="post-form__field">
        <label htmlFor="content">내용</label>
        <textarea
          id="content"
          name="content"
          placeholder="내용을 입력하세요"
          rows={12}
          value={form.content}
          onChange={onChange}
        />
      </div>

      <div className="post-form__actions">
        <button type="button" className="btn" onClick={onCancel}>
          취소
        </button>
        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? '저장 중...' : isEdit ? '수정 완료' : '등록'}
        </button>
      </div>
    </form>
  )
}
