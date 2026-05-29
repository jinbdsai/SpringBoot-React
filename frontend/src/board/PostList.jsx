import { useMemo, useState } from 'react'

const PAGE_SIZE = 10

export default function PostList({ posts, loading, onSelect, onWrite }) {
  const [keyword, setKeyword] = useState('')
  const [field, setField] = useState('title')
  const [page, setPage] = useState(1)

  const filtered = useMemo(() => {
    if (!keyword.trim()) return posts
    const k = keyword.trim().toLowerCase()
    return posts.filter((p) => {
      if (field === 'title') return p.title.toLowerCase().includes(k)
      if (field === 'author') return p.author.toLowerCase().includes(k)
      return p.title.toLowerCase().includes(k) || p.content.toLowerCase().includes(k)
    })
  }, [posts, keyword, field])

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const currentPage = Math.min(page, totalPages)
  const startIdx = (currentPage - 1) * PAGE_SIZE
  const visible = filtered.slice(startIdx, startIdx + PAGE_SIZE)

  const formatDate = (iso) => {
    const d = new Date(iso)
    const today = new Date()
    const sameDay =
      d.getFullYear() === today.getFullYear() &&
      d.getMonth() === today.getMonth() &&
      d.getDate() === today.getDate()
    if (sameDay) {
      return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    }
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(
      d.getDate()
    ).padStart(2, '0')}`
  }

  return (
    <div className="post-list">
      <div className="post-list__toolbar">
        <div className="post-list__search">
          <select value={field} onChange={(e) => setField(e.target.value)}>
            <option value="title">제목</option>
            <option value="author">작성자</option>
            <option value="title_content">제목+내용</option>
          </select>
          <input
            type="text"
            placeholder="검색어를 입력하세요"
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value)
              setPage(1)
            }}
          />
        </div>
        <button className="btn btn--primary" onClick={onWrite}>
          ✏️ 글쓰기
        </button>
      </div>

      <table className="post-table">
        <thead>
          <tr>
            <th className="col-num">번호</th>
            <th className="col-title">제목</th>
            <th className="col-author">작성자</th>
            <th className="col-date">작성일</th>
            <th className="col-views">조회</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={5} className="post-table__empty">
                불러오는 중...
              </td>
            </tr>
          ) : visible.length === 0 ? (
            <tr>
              <td colSpan={5} className="post-table__empty">
                {keyword ? '검색 결과가 없습니다.' : '등록된 글이 없습니다.'}
              </td>
            </tr>
          ) : (
            visible.map((p, idx) => (
              <tr key={p.id} className="post-table__row" onClick={() => onSelect(p.id)}>
                <td className="col-num">{filtered.length - startIdx - idx}</td>
                <td className="col-title">
                  <span className="post-table__title">{p.title}</span>
                </td>
                <td className="col-author">{p.author}</td>
                <td className="col-date">{formatDate(p.createdAt)}</td>
                <td className="col-views">{p.viewCount ?? 0}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      {totalPages > 1 && (
        <div className="pagination">
          <button
            className="pagination__btn"
            disabled={currentPage === 1}
            onClick={() => setPage(currentPage - 1)}
          >
            ‹
          </button>
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
            <button
              key={p}
              className={`pagination__btn ${p === currentPage ? 'is-active' : ''}`}
              onClick={() => setPage(p)}
            >
              {p}
            </button>
          ))}
          <button
            className="pagination__btn"
            disabled={currentPage === totalPages}
            onClick={() => setPage(currentPage + 1)}
          >
            ›
          </button>
        </div>
      )}
    </div>
  )
}
