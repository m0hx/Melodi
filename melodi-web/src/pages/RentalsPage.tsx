import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getJson, postJson } from '../api/client.ts'
import { useAuth } from '../auth/AuthContext.tsx'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

type Instrument = { id: number; name: string }

type Rental = {
  id: number
  instrument: Instrument
  startsAt: string
  endsAt: string
  returnedAt?: string | null
  status: string
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

function canReturn(status: string) {
  return status === 'ACTIVE' || status === 'OVERDUE'
}

export function RentalsPage() {
  const { token } = useAuth()
  const [rentals, setRentals] = useState<Rental[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  const loadRentals = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError(null)
    try {
      const data = await getJson<Rental[]>('/api/rentals', { token })
      setRentals(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load rentals')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    void loadRentals()
  }, [loadRentals])

  async function returnRental(id: number) {
    if (!token) return
    setBusyId(id)
    setError(null)
    try {
      await postJson(`/api/rentals/${id}/return`, {}, { token })
      await loadRentals()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Return failed')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">My rentals</h1>
        <p className="text-sm text-muted-foreground">Active and past instrument rentals.</p>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading rentals…</p>
      ) : rentals.length === 0 ? (
        <Card className="ui-surface">
          <CardContent className="py-8 text-center text-sm text-muted-foreground">
            No rentals yet.{' '}
            <Link to="/instruments" className="text-primary underline-offset-4 hover:underline">
              Rent an instrument
            </Link>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {rentals.map((rental) => (
            <Card key={rental.id} className="ui-surface">
              <CardHeader className="flex flex-row items-start justify-between gap-4 space-y-0">
                <div className="space-y-1">
                  <CardTitle className="text-base">{rental.instrument.name}</CardTitle>
                  <CardDescription>
                    {formatDate(rental.startsAt)} to {formatDate(rental.endsAt)} · {rental.status}
                    {rental.returnedAt ? ` · returned ${formatDate(rental.returnedAt)}` : ''}
                  </CardDescription>
                </div>
              </CardHeader>
              <CardContent className="flex flex-wrap gap-2">
                <Button size="sm" variant="outline" asChild>
                  <Link to={`/instruments/${rental.instrument.id}`}>View instrument</Link>
                </Button>
                {canReturn(rental.status) ? (
                  <Button
                    size="sm"
                    disabled={busyId === rental.id}
                    onClick={() => void returnRental(rental.id)}
                  >
                    {busyId === rental.id ? 'Returning…' : 'Mark returned'}
                  </Button>
                ) : null}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
