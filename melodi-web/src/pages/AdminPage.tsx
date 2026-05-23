import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
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

type Profile = {
  fullName: string
  email: string
  role?: { name: string }
}

type Instrument = { id: number; status?: string | null }
type Category = { id: number; name: string }
type Brand = { id: number; name: string }

export function AdminPage() {
  const { token } = useAuth()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [counts, setCounts] = useState({ instruments: 0, hidden: 0, categories: 0, brands: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const [profileData, instruments, categories, brands] = await Promise.all([
          getJson<Profile>('/api/profile', { token }),
          getJson<Instrument[]>('/api/instruments', { token }),
          getJson<Category[]>('/api/categories', { token }),
          getJson<Brand[]>('/api/brands', { token }),
        ])
        if (cancelled) return
        setProfile(profileData)
        const instrumentList = Array.isArray(instruments) ? instruments : []
        setCounts({
          instruments: instrumentList.length,
          hidden: instrumentList.filter((i) => i.status?.toUpperCase() === 'HIDDEN').length,
          categories: Array.isArray(categories) ? categories.length : 0,
          brands: Array.isArray(brands) ? brands.length : 0,
        })
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load admin data')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token])

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Admin</h1>
        <p className="text-sm text-muted-foreground">
          {profile
            ? `${profile.fullName} · ${profile.email} · ${profile.role?.name ?? 'ADMIN'}`
            : null}
        </p>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading…</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          <Card className="ui-surface">
            <CardHeader>
              <CardTitle className="text-base">Catalog</CardTitle>
              <CardDescription>
                {counts.instruments} instruments ({counts.hidden} hidden) · {counts.categories}{' '}
                categories · {counts.brands} brands
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-wrap gap-2">
              <Button size="sm" variant="outline" asChild>
                <Link to="/instruments">View catalog</Link>
              </Button>
            </CardContent>
          </Card>

          <Card className="ui-surface">
            <CardHeader>
              <CardTitle className="text-base">Shop operations</CardTitle>
              <CardDescription>Orders, rentals, and customer accounts.</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-wrap gap-2">
              <Button size="sm" variant="outline" asChild>
                <Link to="/orders">Orders</Link>
              </Button>
              <Button size="sm" variant="outline" asChild>
                <Link to="/rentals">Rentals</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  )
}
