import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { deleteJson, getJson } from '../api/client.ts'
import { useAuth } from '../auth/AuthContext.tsx'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

type Instrument = {
  id: number
  name: string
  purchasePrice?: number
  rentalPricePerDay?: number
  status?: string | null
}

type WishlistItem = {
  id: number
  instrument: Instrument
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)
}

export function WishlistPage() {
  const { token } = useAuth()
  const [items, setItems] = useState<WishlistItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  const loadWishlist = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError(null)
    try {
      const data = await getJson<WishlistItem[]>('/api/wishlist', { token })
      setItems(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load wishlist')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    void loadWishlist()
  }, [loadWishlist])

  async function removeItem(id: number) {
    if (!token) return
    setBusyId(id)
    setError(null)
    try {
      await deleteJson(`/api/wishlist/items/${id}`, { token })
      await loadWishlist()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Remove failed')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Wishlist</h1>
        <p className="text-sm text-muted-foreground">Instruments you want to revisit later.</p>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading wishlist…</p>
      ) : items.length === 0 ? (
        <Card className="ui-surface">
          <CardContent className="py-8 text-center text-sm text-muted-foreground">
            Your wishlist is empty.{' '}
            <Link to="/instruments" className="text-primary underline-offset-4 hover:underline">
              Browse instruments
            </Link>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {items.map((item) => (
            <Card key={item.id} className="ui-surface">
              <CardHeader className="flex flex-row items-start justify-between gap-4 space-y-0">
                <div className="space-y-1">
                  <CardTitle className="text-base">{item.instrument.name}</CardTitle>
                  <CardDescription>
                    {item.instrument.status ? `${item.instrument.status} · ` : ''}
                    {item.instrument.purchasePrice != null
                      ? `Buy ${formatMoney(item.instrument.purchasePrice)}`
                      : 'View details for pricing'}
                    {item.instrument.rentalPricePerDay != null
                      ? ` · Rent ${formatMoney(item.instrument.rentalPricePerDay)}/day`
                      : ''}
                  </CardDescription>
                </div>
              </CardHeader>
              <CardContent className="flex flex-wrap gap-2">
                <Button size="sm" variant="outline" asChild>
                  <Link to={`/instruments/${item.instrument.id}`}>View</Link>
                </Button>
                <Button
                  size="sm"
                  variant="destructive"
                  disabled={busyId === item.id}
                  onClick={() => void removeItem(item.id)}
                >
                  Remove
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
