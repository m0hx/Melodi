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

type Order = {
  id: number
  orderNumber: string
  orderType: string
  status: string
  totalAmount: number
  trackingId?: string | null
  createdAt: string
  user?: { fullName: string; email: string } | null
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)
}

function formatDate(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

export function AdminOrdersPage() {
  const { token } = useAuth()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) return
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await getJson<Order[]>('/api/admin/orders', { token })
        if (!cancelled) setOrders(Array.isArray(data) ? data : [])
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load orders')
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
      <Button variant="ghost" size="sm" asChild className="-ml-2 w-fit">
        <Link to="/admin">← Back to admin</Link>
      </Button>

      <div className="space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">All orders</h1>
        <p className="text-sm text-muted-foreground">Customer orders across the shop.</p>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading orders…</p>
      ) : orders.length === 0 ? (
        <Card className="ui-surface">
          <CardContent className="py-8 text-center text-sm text-muted-foreground">
            No orders yet.
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {orders.map((order) => (
            <Card key={order.id} className="ui-surface">
              <CardHeader className="flex flex-row items-start justify-between gap-4 space-y-0">
                <div className="space-y-1">
                  <CardTitle className="text-base">{order.orderNumber}</CardTitle>
                  <CardDescription>
                    {formatDate(order.createdAt)} · {order.orderType} · {order.status}
                    {order.user ? ` · ${order.user.fullName} (${order.user.email})` : ''}
                  </CardDescription>
                </div>
                <p className="shrink-0 text-sm font-medium">
                  {formatMoney(Number(order.totalAmount))}
                </p>
              </CardHeader>
              <CardContent className="flex flex-wrap items-center gap-3">
                {order.trackingId ? (
                  <p className="text-xs text-muted-foreground">Tracking: {order.trackingId}</p>
                ) : null}
                <Button size="sm" variant="outline" asChild>
                  <Link to={`/admin/orders/${order.id}`}>View details</Link>
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
