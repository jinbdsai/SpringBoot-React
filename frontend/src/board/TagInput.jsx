import { useState } from 'react'

export default function TagInput({ value, onChange, max = 5 }) {
  const [draft, setDraft] = useState('')

  const addTag = (raw) => {
    const t = raw.trim().replace(/^#/, '').toLowerCase().replace(/\s+/g, '_').slice(0, 30)
    if (!t) return
    if (value.includes(t)) return
    if (value.length >= max) return
    onChange([...value, t])
  }

  const onKeyDown = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault()
      addTag(draft)
      setDraft('')
    } else if (e.key === 'Backspace' && draft === '' && value.length > 0) {
      onChange(value.slice(0, -1))
    }
  }

  return (
    <div className="tag-input">
      <div className="tag-input__list">
        {value.map((t) => (
          <span key={t} className="tag-chip">
            #{t}
            <button type="button" className="tag-chip__remove" onClick={() => onChange(value.filter((v) => v !== t))}>
              ×
            </button>
          </span>
        ))}
        <input
          className="tag-input__input"
          type="text"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={onKeyDown}
          onBlur={() => { if (draft) { addTag(draft); setDraft('') } }}
          placeholder={value.length >= max ? `최대 ${max}개` : '태그 입력 후 Enter (최대 ' + max + '개)'}
          disabled={value.length >= max}
        />
      </div>
    </div>
  )
}
