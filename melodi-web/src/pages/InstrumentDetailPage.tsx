import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getJson } from '../api/client.ts'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

type Category = { id: number; name: string }
type Brand = { id: number; name: string }

type Instrument = {
  id: number
  name: string
  description?: string | null
  purchasePrice: number
  rentalPricePerDay: number
  purchaseStock: number
  rentalStock: number
  condition?: string | null
  status?: string | null
  category?: Category | null
  brand?: Brand | null
}

type Review = {
  id: number
  rating: number
  title?: string | null
  body?: string | null
  reviewerName: string
  createdAt: string
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(value)
}

function formatDate(iso: string) {
  try {
    return new Date(iso).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  } catch {
    return iso
  }
}

export function InstrumentDetailPage() {
  const { instrumentId: idParam } = useParams<{ instrumentId: string }>()
  const instrumentId = idParam != null ? Number(idParam) : NaN

  const [instrument, setInstrument] = useState<Instrument | null>(null)
  const [reviews, setReviews] = useState<Review[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      if (Number.isNaN(instrumentId) || instrumentId < 1) {
        setLoading(false)
        setError('Invalid instrument link.')
        return
      }

      setLoading(true)
      setError(null)
      setInstrument(null)
      setReviews([])

      try {
        const [instrumentData, reviewsData] = await Promise.all([
          getJson<Instrument>(`/api/instruments/${instrumentId}`),
          getJson<Review[]>(`/api/instruments/${instrumentId}/reviews`),
        ])
        if (cancelled) return
        setInstrument(instrumentData)
        setReviews(Array.isArray(reviewsData) ? reviewsData : [])
      } catch (err) {
        if (cancelled) return
        setError(err instanceof Error ? err.message : 'Failed to load instrument')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [instrumentId])

  if (loading) {
    return (
      <Card className="ui-surface">
        <CardHeader>
          <div className="h-6 w-1/2 rounded bg-muted/40" />
          <div className="mt-2 h-4 w-1/3 rounded bg-muted/30" />
        </CardHeader>
      </Card>
    )
  }

  if (error || !instrument) {
    return (
      <Card className="ui-surface">
        <CardHeader>
          <CardTitle className="text-base">Could not load instrument</CardTitle>
          <CardDescription>{error ?? 'Not found.'}</CardDescription>
        </CardHeader>
        <CardContent>
          <Button asChild variant="outline">
            <Link to="/instruments">Back to catalog</Link>
          </Button>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-2">
          <Button variant="ghost" size="sm" asChild className="-ml-2 w-fit">
            <Link to="/instruments">← Back to catalog</Link>
          </Button>
          <h1 className="font-heading text-3xl font-semibold tracking-tight md:text-4xl">
            {instrument.name}
          </h1>
          <p className="text-sm text-muted-foreground">
            {instrument.brand?.name ?? 'Unknown brand'}
            {instrument.category?.name ? ` · ${instrument.category.name}` : ''}
            {instrument.condition ? ` · ${instrument.condition}` : ''}
          </p>
        </div>
        <div className="ui-surface rounded-xl border border-border/60 px-4 py-3 text-sm">
          <p className="font-medium text-foreground">Buy {formatMoney(instrument.purchasePrice)}</p>
          <p className="text-muted-foreground">
            Rent {formatMoney(instrument.rentalPricePerDay)}/day
          </p>
          <p className="mt-2 text-xs text-muted-foreground">
            {instrument.purchaseStock} available to buy · {instrument.rentalStock} available to rent
          </p>
          <p className="text-xs text-muted-foreground">Status: {instrument.status ?? 'N/A'}</p>
        </div>
      </div>

      {instrument.description ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">About</CardTitle>
          </CardHeader>
          <CardContent className="text-sm leading-relaxed text-muted-foreground">
            {instrument.description}
          </CardContent>
        </Card>
      ) : null}

      <section className="space-y-4">
        <div className="space-y-1">
          <h2 className="font-heading text-xl font-semibold tracking-tight">Reviews</h2>
          <p className="text-sm text-muted-foreground">
            {reviews.length === 0
              ? 'No reviews yet.'
              : `${reviews.length} review${reviews.length === 1 ? '' : 's'}`}
          </p>
        </div>

        {reviews.length > 0 ? (
          <div className="grid gap-3">
            {reviews.map((r) => (
              <Card key={r.id} className="ui-surface">
                <CardHeader className="space-y-1 pb-2">
                  <CardTitle className="text-base">
                    {r.title?.trim() || `${r.rating} out of 5`}
                  </CardTitle>
                  <CardDescription>
                    {r.reviewerName} · {r.rating}/5 · {formatDate(r.createdAt)}
                  </CardDescription>
                </CardHeader>
                {r.body ? (
                  <CardContent className="pt-0 text-sm text-muted-foreground">
                    {r.body}
                  </CardContent>
                ) : null}
              </Card>
            ))}
          </div>
        ) : (
          <Card className="ui-surface">
            <CardContent className="py-6 text-sm text-muted-foreground">
              Be the first to review after you buy this instrument.
            </CardContent>
          </Card>
        )}
      </section>
    </div>
  )
}
