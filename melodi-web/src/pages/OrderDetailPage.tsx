import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { getJson, postJson } from '../api/client.ts'
import { useAuth } from '../auth/AuthContext.tsx'
import { Button } from '@/components/ui/button'
import { IconLabel, icons, withIcon } from '@/components/icons.tsx'
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
  user?: { id: number; fullName: string; email: string } | null
}

type OrderItem = {
  id: number
  mode: string
  quantity: number
  unitPrice: number
  rentalDays?: number | null
  instrument: { id: number; name: string }
}

type Payment = {
  paymentMethod: string
  paymentStatus: string
  paidAt?: string | null
}

type OrderDetail = {
  order: Order
  items: OrderItem[]
  payment: Payment | null
}

type LocationState = { message?: string }

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

export function OrderDetailPage() {
  const { token } = useAuth()
  const { orderId: idParam } = useParams<{ orderId: string }>()
  const location = useLocation()
  const flash = (location.state as LocationState | null)?.message
  const orderId = idParam != null ? Number(idParam) : NaN

  const [detail, setDetail] = useState<OrderDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [cancelling, setCancelling] = useState(false)

  const load = useCallback(async () => {
    if (!token || Number.isNaN(orderId)) return
    setLoading(true)
    setError(null)
    try {
      const data = await getJson<OrderDetail>(`/api/orders/${orderId}`, { token })
      setDetail(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load order')
    } finally {
      setLoading(false)
    }
  }, [token, orderId])

  useEffect(() => {
    void load()
  }, [load])

  async function cancelOrder() {
    if (!token || Number.isNaN(orderId)) return
    setCancelling(true)
    setError(null)
    try {
      await postJson(`/api/orders/${orderId}/cancel`, {}, { token })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Cancel failed')
    } finally {
      setCancelling(false)
    }
  }

  if (Number.isNaN(orderId) || orderId < 1) {
    return <p className="text-sm text-destructive">Invalid order link.</p>
  }

  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading order…</p>
  }

  if (error && !detail) {
    return (
      <Card className="ui-surface">
        <CardHeader>
          <CardTitle className="text-base">Could not load order</CardTitle>
          <CardDescription>{error}</CardDescription>
        </CardHeader>
        <CardContent>
          <Button variant="outline" asChild className={withIcon}>
            <Link to="/orders">
              <IconLabel icon={icons.arrowLeft}>Back to orders</IconLabel>
            </Link>
          </Button>
        </CardContent>
      </Card>
    )
  }

  if (!detail) return null

  const { order, items, payment } = detail

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" asChild className={`-ml-2 w-fit ${withIcon}`}>
        <Link to="/orders">
          <IconLabel icon={icons.arrowLeft}>Back to orders</IconLabel>
        </Link>
      </Button>

      {flash ? (
        <p
          className="ui-notice"
          role="status"
        >
          {flash}
        </p>
      ) : null}

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      <div className="page-intro space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">{order.orderNumber}</h1>
        <p className="text-sm text-muted-foreground">
          {formatDate(order.createdAt)} · {order.orderType} · {order.status}
        </p>
        {order.trackingId ? (
          <p className="text-sm text-muted-foreground">Tracking: {order.trackingId}</p>
        ) : null}
      </div>

      <Card className="ui-surface">
        <CardHeader>
          <CardTitle className="text-base">Items</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          {items.map((line) => (
            <div key={line.id} className="flex justify-between gap-4">
              <span className="text-muted-foreground">
                <Link
                  to={`/instruments/${line.instrument.id}`}
                  className="text-foreground hover:underline"
                >
                  {line.instrument.name}
                </Link>{' '}
                ({line.mode}) × {line.quantity}
              </span>
              <span>{formatMoney(Number(line.unitPrice) * line.quantity)}</span>
            </div>
          ))}
          <p className="border-t border-border/60 pt-2 font-medium">
            Total {formatMoney(Number(order.totalAmount))}
          </p>
        </CardContent>
      </Card>

      {payment ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">Payment</CardTitle>
            <CardDescription>
              {payment.paymentMethod} · {payment.paymentStatus}
              {payment.paidAt ? ` · ${formatDate(payment.paidAt)}` : ''}
            </CardDescription>
          </CardHeader>
        </Card>
      ) : null}

      <div className="flex flex-wrap gap-2">
        {order.status === 'PENDING_PAYMENT' ? (
          <>
            <Button asChild className={withIcon}>
              <Link to="/checkout" state={{ orderId: order.id }}>
                <IconLabel icon={icons.creditCard}>Pay now</IconLabel>
              </Link>
            </Button>
            <Button variant="outline" className={withIcon} disabled={cancelling} onClick={() => void cancelOrder()}>
              <IconLabel icon={icons.xmark}>
                {cancelling ? 'Cancelling…' : 'Cancel order'}
              </IconLabel>
            </Button>
          </>
        ) : null}
      </div>
    </div>
  )
}
