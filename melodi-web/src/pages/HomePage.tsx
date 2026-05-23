import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { getJson } from '../api/client.ts'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { useAuth } from '../auth/AuthContext.tsx'

type Profile = { role?: { name: string } }

type LocationState = { message?: string }

export function HomePage() {
  const { token } = useAuth()
  const location = useLocation()
  const flash = (location.state as LocationState | null)?.message
  const [isAdmin, setIsAdmin] = useState(false)

  useEffect(() => {
    let cancelled = false
    setIsAdmin(false)
    if (!token) return
    ;(async () => {
      try {
        const profile = await getJson<Profile>('/api/profile', { token })
        if (!cancelled) setIsAdmin(profile.role?.name === 'ADMIN')
      } catch {
        if (!cancelled) setIsAdmin(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token])

  return (
    <div className="space-y-10">
      {flash ? (
        <p
          className="rounded-lg border border-border/60 bg-muted/40 px-4 py-3 text-sm text-foreground"
          role="status"
        >
          {flash}
        </p>
      ) : null}
      <section className="piano-hero space-y-4 rounded-2xl border border-border/60 px-6 py-10 md:px-10 md:py-12">
        <p className="text-xs font-medium tracking-[0.2em] text-muted-foreground uppercase">
          Melodi Instruments
        </p>
        <h1 className="font-heading max-w-2xl text-3xl font-semibold tracking-tight text-foreground md:text-4xl">
          Your stage starts here
        </h1>
        <p className="max-w-xl text-base leading-relaxed text-muted-foreground md:text-lg">
          Buy or rent quality instruments. Browse the catalog, manage your cart,
          and track orders and rentals in one place.
        </p>

        <div className="flex flex-col gap-3 sm:flex-row sm:flex-wrap">
          <Button asChild>
            <Link to="/instruments">Browse instruments</Link>
          </Button>
          {token ? (
            isAdmin ? (
              <Button variant="outline" asChild>
                <Link to="/admin">Admin panel</Link>
              </Button>
            ) : (
              <Button variant="outline" asChild>
                <Link to="/orders">My orders</Link>
              </Button>
            )
          ) : (
            <Button variant="outline" asChild>
              <Link to="/signup">Create account</Link>
            </Button>
          )}
        </div>
      </section>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <Card className="ui-surface">
          <CardHeader className="space-y-1">
            <CardTitle className="text-base">Buy</CardTitle>
            <CardDescription>Purchase instruments with secure checkout.</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            Add items to your cart and confirm payment when you are ready.
          </CardContent>
        </Card>

        <Card className="ui-surface">
          <CardHeader className="space-y-1">
            <CardTitle className="text-base">Rent</CardTitle>
            <CardDescription>Short-term rentals with return tracking.</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            Pick rental dates in cart and manage active rentals from your account.
          </CardContent>
        </Card>

        <Card className="ui-surface">
          <CardHeader className="space-y-1">
            <CardTitle className="text-base">Reviews</CardTitle>
            <CardDescription>Share feedback after a confirmed order.</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            Rate instruments you have purchased. Honest reviews help everyone.
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
