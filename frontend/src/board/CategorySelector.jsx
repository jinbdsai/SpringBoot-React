export const CATEGORIES = [
  { value: 'FREE', label: '자유', emoji: '💬' },
  { value: 'QUESTION', label: '질문', emoji: '❓' },
  { value: 'INFO', label: '정보', emoji: '📌' },
  { value: 'CHAT', label: '잡담', emoji: '🗨️' },
]

export function categoryLabel(value) {
  const cat = CATEGORIES.find((c) => c.value === value)
  return cat ? `${cat.emoji} ${cat.label}` : value
}

export default function CategorySelector({ value, onChange }) {
  return (
    <div className="category-selector">
      {CATEGORIES.map((c) => (
        <button
          key={c.value}
          type="button"
          className={`category-chip ${value === c.value ? 'is-active' : ''}`}
          onClick={() => onChange(c.value)}
        >
          {c.emoji} {c.label}
        </button>
      ))}
    </div>
  )
}
