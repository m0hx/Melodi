function apiBase(): string {
  const base = import.meta.env.VITE_API_URL
  if (base == null || base === '') {
    throw new Error('VITE_API_URL is not set')
  }
  return String(base).replace(/\/$/, '')
}

export function profileImageUrl(userId: number, cacheBust?: string | number) {
  const url = `${apiBase()}/api/profile/${userId}/image`
  return cacheBust != null ? `${url}?v=${cacheBust}` : url
}

export function instrumentImageUrl(instrumentId: number, cacheBust?: string | number) {
  const url = `${apiBase()}/api/instruments/${instrumentId}/image`
  return cacheBust != null ? `${url}?v=${cacheBust}` : url
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

export async function putJson<T>(
  path: string,
  body: unknown,
  opts?: { token?: string | null },
): Promise<T> {
  const p = path.startsWith('/') ? path : `/${path}`
  const res = await fetch(`${apiBase()}${p}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...authHeaders(opts?.token) },
    body: JSON.stringify(body),
  })
  const parsed = await parseResponse(res)
  if (!res.ok) {
    throw new Error(asErrorMessage(parsed, res.status))
  }
  return parsed as T
}

/** PUT multipart form with field name `image` (matches Postman collection). */
export async function putImage(
  path: string,
  file: File,
  opts?: { token?: string | null },
): Promise<string> {
  const p = path.startsWith('/') ? path : `/${path}`
  const form = new FormData()
  form.append('image', file)
  const res = await fetch(`${apiBase()}${p}`, {
    method: 'PUT',
    headers: { ...authHeaders(opts?.token) },
    body: form,
  })
  const parsed = await parseResponse(res)
  if (!res.ok) {
    throw new Error(asErrorMessage(parsed, res.status))
  }
  return typeof parsed === 'string' ? parsed : 'Image updated'
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
