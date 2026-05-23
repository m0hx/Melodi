import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
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

function formatMoney(value: number) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(value)
}

export function InstrumentsPage() {
  const [instruments, setInstruments] = useState<Instrument[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | 'all'>('all')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const [instrumentsData, categoriesData] = await Promise.all([
          getJson<Instrument[]>('/api/instruments'),
          getJson<Category[]>('/api/categories'),
        ])
        if (cancelled) return
        setInstruments(Array.isArray(instrumentsData) ? instrumentsData : [])
        setCategories(Array.isArray(categoriesData) ? categoriesData : [])
      } catch (err) {
        if (cancelled) return
        setError(err instanceof Error ? err.message : 'Failed to load instruments')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const filtered = useMemo(() => {
    const visible = instruments.filter((i) => i.status?.toUpperCase() !== 'HIDDEN')
    if (selectedCategoryId === 'all') return visible
    return visible.filter((i) => i.category?.id === selectedCategoryId)
  }, [instruments, selectedCategoryId])

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="space-y-1">
          <p className="text-xs font-medium tracking-[0.18em] text-muted-foreground uppercase">
            Browse
          </p>
          <h1 className="font-heading text-3xl font-semibold tracking-tight text-foreground md:text-4xl">
            Instruments
          </h1>
          <p className="max-w-2xl text-sm text-muted-foreground md:text-base">
            Buy or rent from our catalog.
          </p>
        </div>

        <div className="flex flex-col gap-2 sm:items-end">
          <label className="text-xs font-medium text-muted-foreground" htmlFor="category-filter">
            Filter by category
          </label>
          <select
            id="category-filter"
            className="h-9 w-full rounded-md border border-border/70 bg-background px-3 text-sm text-foreground shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring sm:w-56"
            value={selectedCategoryId === 'all' ? 'all' : String(selectedCategoryId)}
            onChange={(e) => {
              const v = e.target.value
              setSelectedCategoryId(v === 'all' ? 'all' : Number(v))
            }}
          >
            <option value="all">All categories</option>
            {categories.map((c) => (
              <option key={c.id} value={String(c.id)}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">Could not load instruments</CardTitle>
            <CardDescription>{error}</CardDescription>
          </CardHeader>
          <CardContent>
            <Button asChild variant="outline">
              <Link to="/">Back home</Link>
            </Button>
          </CardContent>
        </Card>
      ) : null}

      {loading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, idx) => (
            <Card key={idx} className="ui-surface">
              <CardHeader className="space-y-2">
                <div className="h-4 w-2/3 rounded bg-muted/40" />
                <div className="h-3 w-1/2 rounded bg-muted/30" />
              </CardHeader>
              <CardContent>
                <div className="h-20 rounded bg-muted/20" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((item) => (
            <Card key={item.id} className="ui-surface flex flex-col">
              <CardHeader className="space-y-1">
                <CardTitle className="text-lg leading-snug">{item.name}</CardTitle>
                <CardDescription>
                  {item.brand?.name ?? 'Unknown brand'}
                  {item.category?.name ? ` · ${item.category.name}` : ''}
                </CardDescription>
              </CardHeader>
              <CardContent className="mt-auto space-y-3">
                <div className="text-sm text-muted-foreground">
                  <p>Buy {formatMoney(item.purchasePrice)}</p>
                  <p>Rent {formatMoney(item.rentalPricePerDay)}/day</p>
                  <p className="mt-1 text-xs">
                    Stock: {item.purchaseStock} buy · {item.rentalStock} rent
                  </p>
                </div>
                {item.description ? (
                  <p className="line-clamp-2 text-sm text-muted-foreground">
                    {item.description}
                  </p>
                ) : null}
                <Button className="w-full" asChild>
                  <Link to={`/instruments/${item.id}`}>View details</Link>
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {!loading && !error && filtered.length === 0 ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">No instruments found</CardTitle>
            <CardDescription>Try a different category filter.</CardDescription>
          </CardHeader>
        </Card>
      ) : null}
    </div>
  )
}
