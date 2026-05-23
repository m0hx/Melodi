import { type ReactNode, useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { getJson } from '../api/client.ts'
import { useAuth } from '../auth/AuthContext.tsx'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

type Profile = { role?: { name: string } }

export function RequireAdmin({ children }: { children: ReactNode }) {
  const { token } = useAuth()
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
    return <Navigate to="/signin" replace state={{ from: '/admin' }} />
  }

  if (loading) {
    return <p className="text-sm text-muted-foreground">Checking access…</p>
  }

  if (!isAdmin) {
    return (
      <Card className="ui-surface mx-auto max-w-md">
        <CardHeader>
          <CardTitle className="text-base">Access denied</CardTitle>
          <CardDescription>Admin access is required for this area.</CardDescription>
        </CardHeader>
        <CardContent>
          <Button variant="outline" asChild>
            <Link to="/">Back home</Link>
          </Button>
        </CardContent>
      </Card>
    )
  }

  return children
}
