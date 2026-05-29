import { useState } from 'react'
import MarkdownEditor from './MarkdownEditor'
import CategorySelector from './CategorySelector'
import TagInput from './TagInput'

export default function PostForm({ initial, currentUser, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    title: initial?.title ?? '',
    content: initial?.content ?? '',
    category: initial?.category ?? 'FREE',
    tags: initial?.tags ?? [],
  })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const isEdit = Boolean(initial)
  const author = isEdit ? initial.author : currentUser?.username

  const setField = (k, v) => setForm({ ...form, [k]: v })

  const submit = async (e) => {
    e.preventDefault()
    if (!form.title.trim() || !form.content.trim()) {
      setError('제목과 내용을 입력해주세요.')
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
        <label>카테고리</label>
        <CategorySelector value={form.category} onChange={(v) => setField('category', v)} />
      </div>

      <div className="post-form__field">
        <label htmlFor="title">제목</label>
        <input
          id="title"
          type="text"
          placeholder="제목"
          value={form.title}
          onChange={(e) => setField('title', e.target.value)}
          maxLength={200}
        />
      </div>

      <div className="post-form__field">
        <label>태그</label>
        <TagInput value={form.tags} onChange={(v) => setField('tags', v)} />
      </div>

      <div className="post-form__field">
        <label htmlFor="content">내용 (마크다운)</label>
        <MarkdownEditor
          value={form.content}
          onChange={(v) => setField('content', v)}
          placeholder="마크다운으로 작성하세요. 이미지는 Ctrl+V 로 붙여넣을 수 있어요."
          rows={14}
        />
      </div>

      <div className="post-form__actions">
        <button type="button" className="btn" onClick={onCancel}>취소</button>
        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? '저장 중...' : isEdit ? '수정 완료' : '등록'}
        </button>
      </div>
    </form>
  )
}
