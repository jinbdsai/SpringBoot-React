import { useEffect, useRef, useState } from 'react'
import { notificationsApi } from '../api/notifications'

const POLL_INTERVAL_MS = 30_000

export default function NotificationBell({ currentUser, onSelectPost }) {
  const [unreadCount, setUnreadCount] = useState(0)
  const [open, setOpen] = useState(false)
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const dropdownRef = useRef(null)

  const fetchCount = async () => {
    if (!currentUser) { setUnreadCount(0); return }
    try {
      const { count } = await notificationsApi.unreadCount()
      setUnreadCount(count)
    } catch (e) { /* ignore */ }
  }

  const fetchList = async () => {
    setLoading(true)
    try {
      const data = await notificationsApi.list()
      setItems(data)
    } catch (e) { /* ignore */ } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchCount()
    if (!currentUser) return
    const id = setInterval(fetchCount, POLL_INTERVAL_MS)
    return () => clearInterval(id)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser])

  useEffect(() => {
    if (!open) return
    const onClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', onClickOutside)
    return () => document.removeEventListener('mousedown', onClickOutside)
  }, [open])

  const toggle = async () => {
    const next = !open
    setOpen(next)
    if (next) await fetchList()
  }

  const handleClickItem = async (n) => {
    if (!n.isRead) {
      try { await notificationsApi.markRead(n.id) } catch (e) { /* ignore */ }
    }
    setOpen(false)
    await fetchCount()
    onSelectPost?.(n.postId)
  }

  const handleMarkAllRead = async () => {
    try {
      await notificationsApi.markAllRead()
      await fetchCount()
      await fetchList()
    } catch (e) { /* ignore */ }
  }

  const formatTime = (iso) => {
    const d = new Date(iso)
    const diffMin = Math.floor((Date.now() - d.getTime()) / 60_000)
    if (diffMin < 1) return '방금'
    if (diffMin < 60) return `${diffMin}분 전`
    const diffHr = Math.floor(diffMin / 60)
    if (diffHr < 24) return `${diffHr}시간 전`
    const diffDay = Math.floor(diffHr / 24)
    if (diffDay < 7) return `${diffDay}일 전`
    return `${d.getMonth() + 1}.${d.getDate()}`
  }

  if (!currentUser) return null

  return (
    <div className="notification-bell" ref={dropdownRef}>
      <button type="button" className="notification-bell__btn" onClick={toggle} aria-label="알림">
        🔔
        {unreadCount > 0 && (
          <span className="notification-bell__badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
        )}
      </button>

      {open && (
        <div className="notification-bell__dropdown">
          <div className="notification-bell__header">
            <strong>알림</strong>
            {items.some((n) => !n.isRead) && (
              <button className="notification-bell__read-all" onClick={handleMarkAllRead}>
                모두 읽음
              </button>
            )}
          </div>
          <ul className="notification-bell__list">
            {loading ? (
              <li className="notification-bell__empty">불러오는 중...</li>
            ) : items.length === 0 ? (
              <li className="notification-bell__empty">새로운 알림이 없습니다.</li>
            ) : (
              items.map((n) => (
                <li
                  key={n.id}
                  className={`notification-bell__item ${n.isRead ? 'is-read' : ''}`}
                  onClick={() => handleClickItem(n)}
                >
                  <span className="notification-bell__icon">{n.type === 'LIKE' ? '❤️' : '💬'}</span>
                  <span className="notification-bell__message">{n.message}</span>
                  <span className="notification-bell__time">{formatTime(n.createdAt)}</span>
                </li>
              ))
            )}
          </ul>
        </div>
      )}
    </div>
  )
}
