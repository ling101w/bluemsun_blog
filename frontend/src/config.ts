const FALLBACK_API_BASE_URL = '/api'
const FALLBACK_API_ORIGIN =
  typeof window !== 'undefined' && window.location.origin ? window.location.origin : ''

const hasProtocol = (url: string) => /^https?:\/\//i.test(url)

const normalizeBaseUrl = (url: string) => {
  if (!url) return FALLBACK_API_BASE_URL
  const trimmed = url.trim()

  if (hasProtocol(trimmed)) {
    const normalized = trimmed.replace(/\/+$/, '')
    return normalized.endsWith('/api') ? normalized : `${normalized}/api`
  }

  const prefixed = trimmed.startsWith('/') ? trimmed : `/${trimmed}`
  const sanitized = prefixed.replace(/\/+$/, '')
  return sanitized || FALLBACK_API_BASE_URL
}

const rawApiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').trim()
const resolvedApiBaseUrl = rawApiBaseUrl || FALLBACK_API_BASE_URL

export const API_BASE_URL = normalizeBaseUrl(resolvedApiBaseUrl)

export const API_ORIGIN = (() => {
  if (hasProtocol(resolvedApiBaseUrl)) {
    try {
      return new URL(resolvedApiBaseUrl).origin
    } catch {
      return FALLBACK_API_ORIGIN
    }
  }

  return FALLBACK_API_ORIGIN
})()
