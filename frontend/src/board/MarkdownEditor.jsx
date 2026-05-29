import { useRef, useState } from 'react'
import MarkdownView from './MarkdownView'
import { mediaApi } from '../api/media'

export default function MarkdownEditor({ value, onChange, placeholder, rows = 12 }) {
  const [tab, setTab] = useState('write')
  const [uploading, setUploading] = useState(false)
  const fileInputRef = useRef(null)
  const textareaRef = useRef(null)

  const insertAtCursor = (text) => {
    const ta = textareaRef.current
    if (!ta) {
      onChange(value + text)
      return
    }
    const start = ta.selectionStart
    const end = ta.selectionEnd
    const newValue = value.substring(0, start) + text + value.substring(end)
    onChange(newValue)
    requestAnimationFrame(() => {
      ta.focus()
      ta.selectionStart = ta.selectionEnd = start + text.length
    })
  }

  const handleFileSelected = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const { url } = await mediaApi.upload(file)
      insertAtCursor(`\n![](${url})\n`)
    } catch (err) {
      alert('이미지 업로드 실패: ' + err.message)
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  const handlePaste = async (e) => {
    const items = e.clipboardData?.items
    if (!items) return
    for (const item of items) {
      if (item.type.startsWith('image/')) {
        e.preventDefault()
        const file = item.getAsFile()
        setUploading(true)
        try {
          const { url } = await mediaApi.upload(file)
          insertAtCursor(`\n![](${url})\n`)
        } catch (err) {
          alert('이미지 업로드 실패: ' + err.message)
        } finally {
          setUploading(false)
        }
        return
      }
    }
  }

  const wrapSelection = (left, right = left) => {
    const ta = textareaRef.current
    if (!ta) return
    const start = ta.selectionStart
    const end = ta.selectionEnd
    const selected = value.substring(start, end)
    const newValue = value.substring(0, start) + left + selected + right + value.substring(end)
    onChange(newValue)
    requestAnimationFrame(() => {
      ta.focus()
      ta.selectionStart = start + left.length
      ta.selectionEnd = end + left.length
    })
  }

  return (
    <div className="md-editor">
      <div className="md-editor__toolbar">
        <div className="md-editor__tabs">
          <button
            type="button"
            className={`md-editor__tab ${tab === 'write' ? 'is-active' : ''}`}
            onClick={() => setTab('write')}
          >
            ✍️ 작성
          </button>
          <button
            type="button"
            className={`md-editor__tab ${tab === 'preview' ? 'is-active' : ''}`}
            onClick={() => setTab('preview')}
          >
            👁️ 미리보기
          </button>
        </div>
        {tab === 'write' && (
          <div className="md-editor__buttons">
            <button type="button" onClick={() => wrapSelection('**')} title="Bold">B</button>
            <button type="button" onClick={() => wrapSelection('*')} title="Italic"><i>I</i></button>
            <button type="button" onClick={() => wrapSelection('`')} title="Code">{'</>'}</button>
            <button type="button" onClick={() => insertAtCursor('\n- ')} title="List">• 리스트</button>
            <button type="button" onClick={() => insertAtCursor('\n> ')} title="Quote">"</button>
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={uploading}
              title="이미지 업로드"
            >
              {uploading ? '⏳' : '🖼️ 이미지'}
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              hidden
              onChange={handleFileSelected}
            />
          </div>
        )}
      </div>

      {tab === 'write' ? (
        <textarea
          ref={textareaRef}
          className="md-editor__textarea"
          rows={rows}
          placeholder={placeholder || '마크다운으로 작성하세요. 이미지는 붙여넣기(Ctrl+V) 또는 🖼️ 버튼.'}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onPaste={handlePaste}
        />
      ) : (
        <div className="md-editor__preview">
          {value.trim() ? <MarkdownView content={value} /> : <div className="md-editor__empty">미리볼 내용이 없습니다.</div>}
        </div>
      )}
    </div>
  )
}
