import { type ReactNode, useEffect, useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { getJson } from '../api/client.ts'
import { useAuth } from '../auth/AuthContext.tsx'

type Profile = { role?: { name: string } }

/** Shop routes (cart, orders, etc.) — admins are redirected to /admin. */
export function RequireCustomer({ children }: { children: ReactNode }) {
  const { token } = useAuth()
  const location = useLocation()
  const [loading, setLoading] = useState(true)
  const [isAdmin, setIsAdmin] = useState(false)

  useEffect(() => {
    if (!token) {
      setLoading(false)
      setIsAdmin(false)
      return
    }

    let cancelled = false
    ;(async () => {
      setLoading(true)
      try {
        const profile = await getJson<Profile>('/api/profile', { token })
        if (!cancelled) setIsAdmin(profile.role?.name === 'ADMIN')
      } catch {
        if (!cancelled) setIsAdmin(false)
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [token])

  if (!token) {
    return <Navigate to="/signin" replace state={{ from: location.pathname }} />
  }

  if (loading) {
    return <p className="text-sm text-muted-foreground">Checking access…</p>
  }

  if (isAdmin) {
    return <Navigate to="/admin" replace />
  }

  return children
}
