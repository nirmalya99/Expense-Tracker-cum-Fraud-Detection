import { useEffect, useId, useMemo, useRef, useState } from 'react'

function normalizeOptions(options) {
  return (options || []).map((o) => (typeof o === 'string' ? { value: o, label: o } : o))
}

export function Select({ value, onChange, options, placeholder = 'Select…', className = '' }) {
  const id = useId()
  const listboxId = `${id}-listbox`
  const normalized = useMemo(() => normalizeOptions(options), [options])

  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(() => Math.max(0, normalized.findIndex((o) => o.value === value)))

  const rootRef = useRef(null)
  const buttonRef = useRef(null)
  const menuRef = useRef(null)

  const selected = normalized.find((o) => o.value === value) || null

  useEffect(() => {
    setActiveIndex(Math.max(0, normalized.findIndex((o) => o.value === value)))
  }, [value, normalized])

  useEffect(() => {
    function onDocMouseDown(e) {
      if (!rootRef.current) return
      if (!rootRef.current.contains(e.target)) setOpen(false)
    }
    function onDocKeyDown(e) {
      if (e.key === 'Escape') {
        setOpen(false)
        buttonRef.current?.focus()
      }
    }
    document.addEventListener('mousedown', onDocMouseDown)
    document.addEventListener('keydown', onDocKeyDown)
    return () => {
      document.removeEventListener('mousedown', onDocMouseDown)
      document.removeEventListener('keydown', onDocKeyDown)
    }
  }, [])

  useEffect(() => {
    if (open) {
      // Focus menu for immediate keyboard navigation
      queueMicrotask(() => menuRef.current?.focus())
    }
  }, [open])

  function commit(nextValue) {
    onChange?.(nextValue)
    setOpen(false)
    buttonRef.current?.focus()
  }

  function onButtonKeyDown(e) {
    if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      e.preventDefault()
      setOpen(true)
      setActiveIndex((idx) => {
        const next = e.key === 'ArrowDown' ? idx + 1 : idx - 1
        return Math.min(Math.max(next, 0), normalized.length - 1)
      })
      return
    }

    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      setOpen((v) => !v)
      return
    }
  }

  function onListKeyDown(e) {
    if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      e.preventDefault()
      setActiveIndex((idx) => {
        const next = e.key === 'ArrowDown' ? idx + 1 : idx - 1
        return Math.min(Math.max(next, 0), normalized.length - 1)
      })
      return
    }
    if (e.key === 'Home') {
      e.preventDefault()
      setActiveIndex(0)
      return
    }
    if (e.key === 'End') {
      e.preventDefault()
      setActiveIndex(normalized.length - 1)
      return
    }
    if (e.key === 'Enter') {
      e.preventDefault()
      const opt = normalized[activeIndex]
      if (opt) commit(opt.value)
    }
  }

  return (
    <div ref={rootRef} className={`cselect ${className}`}>
      <button
        ref={buttonRef}
        type="button"
        className="select cselect__button"
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-controls={listboxId}
        onClick={() => setOpen((v) => !v)}
        onKeyDown={onButtonKeyDown}
      >
        <span className={`cselect__value ${selected ? '' : 'cselect__value--placeholder'}`}>
          {selected ? selected.label : placeholder}
        </span>
        <span className="cselect__chevron" aria-hidden="true">
          ▾
        </span>
      </button>

      {open ? (
        <div
          id={listboxId}
          ref={menuRef}
          className="cselect__menu"
          role="listbox"
          tabIndex={-1}
          aria-activedescendant={`${id}-opt-${activeIndex}`}
          onKeyDown={onListKeyDown}
        >
          {normalized.map((o, idx) => {
            const isSelected = o.value === value
            const isActive = idx === activeIndex
            return (
              <div
                key={o.value ?? idx}
                id={`${id}-opt-${idx}`}
                role="option"
                aria-selected={isSelected}
                className={`cselect__option ${isSelected ? 'is-selected' : ''} ${isActive ? 'is-active' : ''}`}
                onMouseEnter={() => setActiveIndex(idx)}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => commit(o.value)}
              >
                {o.label}
              </div>
            )
          })}
        </div>
      ) : null}
    </div>
  )
}

