import { useMemo, useState } from 'react'
import { CATEGORIES, categoryLabel } from './CategorySelector'

const PAGE_SIZE = 10

const SORTS = [
  { value: 'latest', label: '최신순' },
  { value: 'popular', label: '인기순' },
  { value: 'views', label: '조회순' },
  { value: 'comments', label: '댓글순' },
]

export default function PostList({
  posts, loading, onSelect, onWrite, onSelectAuthor,
  category, setCategory, sort, setSort,
  onSearch, searchActive,
}) {
  const [keyword, setKeyword] = useState('')
  const [field, setField] = useState('title')
  const [page, setPage] = useState(1)

  const filtered = useMemo(() => {
    if (!keyword.trim()) return posts
    const k = keyword.trim().toLowerCase()
    return posts.filter((p) => {
      if (field === 'title') return p.title.toLowerCase().includes(k)
      if (field === 'author') return p.author.toLowerCase().includes(k)
      if (field === 'tag') return (p.tags || []).some((t) => t.toLowerCase().includes(k))
      return p.title.toLowerCase().includes(k) || (p.content || '').toLowerCase().includes(k)
    })
  }, [posts, keyword, field])

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const currentPage = Math.min(page, totalPages)
  const startIdx = (currentPage - 1) * PAGE_SIZE
  const visible = filtered.slice(startIdx, startIdx + PAGE_SIZE)

  const formatDate = (iso) => {
    const d = new Date(iso)
    const today = new Date()
    if (d.toDateString() === today.toDateString()) {
      return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    }
    return `${d.getMonth() + 1}.${d.getDate()}`
  }

  return (
    <div className="post-list">
      <div className="post-list__categories">
        <button
          type="button"
          className={`category-chip ${!category ? 'is-active' : ''}`}
          onClick={() => setCategory(null)}
        >
          전체
        </button>
        {CATEGORIES.map((c) => (
          <button
            key={c.value}
            type="button"
            className={`category-chip ${category === c.value ? 'is-active' : ''}`}
            onClick={() => setCategory(c.value)}
          >
            {c.emoji} {c.label}
          </button>
        ))}
      </div>

      <div className="post-list__toolbar">
        <div className="post-list__search">
          <select value={field} onChange={(e) => setField(e.target.value)}>
            <option value="title">제목</option>
            <option value="author">작성자</option>
            <option value="tag">태그</option>
            <option value="title_content">제목+내용</option>
          </select>
          <input
            type="text"
            placeholder="입력: 현재 목록 필터 / 엔터: 전체 검색"
            value={keyword}
            onChange={(e) => { setKeyword(e.target.value); setPage(1) }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') onSearch?.(keyword.trim())
            }}
          />
          {searchActive && (
            <button
              type="button"
              className="btn"
              onClick={() => { setKeyword(''); onSearch?.('') }}
              title="전체 검색 해제"
            >
              ✕
            </button>
          )}
        </div>
        <div className="post-list__sort">
          <select value={sort} onChange={(e) => setSort(e.target.value)}>
            {SORTS.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
          </select>
        </div>
        <button className="btn btn--primary" onClick={onWrite}>✏️ 글쓰기</button>
      </div>

      <table className="post-table">
        <thead>
          <tr>
            <th className="col-num">#</th>
            <th className="col-cat">분류</th>
            <th className="col-title">제목</th>
            <th className="col-author">작성자</th>
            <th className="col-date">작성</th>
            <th className="col-counts">❤️</th>
            <th className="col-counts">💬</th>
            <th className="col-views">👁</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={8} className="post-table__empty">불러오는 중...</td></tr>
          ) : visible.length === 0 ? (
            <tr><td colSpan={8} className="post-table__empty">{keyword ? '검색 결과 없음' : '등록된 글이 없습니다.'}</td></tr>
          ) : (
            visible.map((p, idx) => (
              <tr key={p.id} className="post-table__row">
                <td className="col-num">{filtered.length - startIdx - idx}</td>
                <td className="col-cat">{categoryLabel(p.category)}</td>
                <td className="col-title" onClick={() => onSelect(p.id)}>
                  <span className="post-table__title">{p.title}</span>
                  {p.tags && p.tags.length > 0 && (
                    <span className="post-table__tags">
                      {p.tags.slice(0, 3).map((t) => <span key={t} className="post-table__tag">#{t}</span>)}
                    </span>
                  )}
                </td>
                <td className="col-author">
                  <button className="post-table__author" onClick={() => onSelectAuthor?.(p.author)}>{p.author}</button>
                </td>
                <td className="col-date">{formatDate(p.createdAt)}</td>
                <td className="col-counts">{p.likeCount ?? 0}</td>
                <td className="col-counts">{p.commentCount ?? 0}</td>
                <td className="col-views">{p.viewCount ?? 0}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      {totalPages > 1 && (
        <div className="pagination">
          <button className="pagination__btn" disabled={currentPage === 1} onClick={() => setPage(currentPage - 1)}>‹</button>
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
            <button
              key={p}
              className={`pagination__btn ${p === currentPage ? 'is-active' : ''}`}
              onClick={() => setPage(p)}
            >
              {p}
            </button>
          ))}
          <button className="pagination__btn" disabled={currentPage === totalPages} onClick={() => setPage(currentPage + 1)}>›</button>
        </div>
      )}
    </div>
  )
}
