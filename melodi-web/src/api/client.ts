function apiBase(): string {
  const base = import.meta.env.VITE_API_URL
  if (base == null || base === '') {
    throw new Error('VITE_API_URL is not set')
  }
  return String(base).replace(/\/$/, '')
}

function authHeaders(token?: string | null): HeadersInit {
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function parseResponse(res: Response): Promise<unknown> {
  const text = await res.text()
  if (!text) return null
  try {
    return JSON.parse(text) as unknown
  } catch {
    return text
  }
}

function asErrorMessage(parsed: unknown, status: number): string {
  return typeof parsed === 'object' &&
    parsed !== null &&
    'message' in parsed &&
    typeof (parsed as { message: unknown }).message === 'string'
    ? (parsed as { message: string }).message
    : typeof parsed === 'string'
      ? parsed
      : `Request failed (${status})`
}

export async function postJson<T>(
  path: string,
  body: unknown,
  opts?: { token?: string | null },
): Promise<T> {
  const p = path.startsWith('/') ? path : `/${path}`
  const res = await fetch(`${apiBase()}${p}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders(opts?.token) },
    body: JSON.stringify(body),
  })
  const parsed = await parseResponse(res)
  if (!res.ok) {
    throw new Error(asErrorMessage(parsed, res.status))
  }
  return parsed as T
}

export async function getJson<T>(
  path: string,
  opts?: { token?: string | null },
): Promise<T> {
  const p = path.startsWith('/') ? path : `/${path}`
  const res = await fetch(`${apiBase()}${p}`, {
    method: 'GET',
    headers: { ...authHeaders(opts?.token) },
  })
  const parsed = await parseResponse(res)
  if (!res.ok) {
    throw new Error(asErrorMessage(parsed, res.status))
  }
  return parsed as T
}

export async function patchJson<T>(
  path: string,
  body: unknown,
  opts?: { token?: string | null },
): Promise<T> {
  const p = path.startsWith('/') ? path : `/${path}`
  const res = await fetch(`${apiBase()}${p}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...authHeaders(opts?.token) },
    body: JSON.stringify(body),
  })
  const parsed = await parseResponse(res)
  if (!res.ok) {
    throw new Error(asErrorMessage(parsed, res.status))
  }
  return parsed as T
}

export async function deleteJson<T>(
  path: string,
  opts?: { token?: string | null },
): Promise<T> {
  const p = path.startsWith('/') ? path : `/${path}`
  const res = await fetch(`${apiBase()}${p}`, {
    method: 'DELETE',
    headers: { ...authHeaders(opts?.token) },
  })
  const parsed = await parseResponse(res)
  if (!res.ok) {
    throw new Error(asErrorMessage(parsed, res.status))
  }
  return parsed as T
}
