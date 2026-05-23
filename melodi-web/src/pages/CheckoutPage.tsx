import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

type Order = {
  id: number
  orderNumber: string
  status: string
  totalAmount: number
  trackingId?: string | null
}

type OrderItem = {
  id: number
  mode: string
  quantity: number
  unitPrice: number
  instrument: { id: number; name: string }
}

type Payment = {
  paymentMethod: string
  paymentStatus: string
}

type OrderDetail = {
  order: Order
  items: OrderItem[]
  payment: Payment | null
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)
}

export function CheckoutPage() {
  const { token } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const orderId = (location.state as { orderId?: number } | null)?.orderId

  const [detail, setDetail] = useState<OrderDetail | null>(null)
  const [paymentMethod, setPaymentMethod] = useState('CARD')
  const [loading, setLoading] = useState(true)
  const [paying, setPaying] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token || !orderId) {
      setLoading(false)
      return
    }
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await getJson<OrderDetail>(`/api/orders/${orderId}`, { token })
        if (!cancelled) setDetail(data)
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Failed to load order')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token, orderId])

  async function confirmPayment() {
    if (!token || !orderId) return
    setPaying(true)
    setError(null)
    try {
      await postJson<OrderDetail>(
        `/api/orders/${orderId}/confirm-payment`,
        { paymentMethod: paymentMethod.trim() || 'CARD' },
        { token },
      )
      navigate(`/orders/${orderId}`, {
        state: { message: 'Payment confirmed. Thank you!' },
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Payment failed')
    } finally {
      setPaying(false)
    }
  }

  if (!orderId) {
    return (
      <Card className="ui-surface">
        <CardContent className="py-8 text-center text-sm text-muted-foreground">
          No order to pay.{' '}
          <Link to="/cart" className="text-primary underline-offset-4 hover:underline">
            Go to cart
          </Link>
        </CardContent>
      </Card>
    )
  }

  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading checkout…</p>
  }

  if (error && !detail) {
    return (
      <Card className="ui-surface">
        <CardHeader>
          <CardTitle className="text-base">Checkout error</CardTitle>
          <CardDescription>{error}</CardDescription>
        </CardHeader>
      </Card>
    )
  }

  if (!detail) return null

  const { order, items } = detail

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Checkout</h1>
        <p className="text-sm text-muted-foreground">
          Order {order.orderNumber} · {formatMoney(Number(order.totalAmount))}
        </p>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      <Card className="ui-surface">
        <CardHeader>
          <CardTitle className="text-base">Order summary</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          {items.map((line) => (
            <div key={line.id} className="flex justify-between gap-4">
              <span className="text-muted-foreground">
                {line.instrument.name} ({line.mode}) × {line.quantity}
              </span>
              <span>{formatMoney(Number(line.unitPrice) * line.quantity)}</span>
            </div>
          ))}
          <p className="border-t border-border/60 pt-2 font-medium">
            Total {formatMoney(Number(order.totalAmount))}
          </p>
        </CardContent>
      </Card>

      {order.status === 'PENDING_PAYMENT' ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">Simulated payment</CardTitle>
            <CardDescription>No real charge. Confirm to complete the order.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="payment-method">Payment method</Label>
              <Input
                id="payment-method"
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
                placeholder="CARD"
              />
            </div>
            <Button className="w-full" disabled={paying} onClick={() => void confirmPayment()}>
              {paying ? 'Confirming…' : 'Confirm payment'}
            </Button>
          </CardContent>
        </Card>
      ) : (
        <Button asChild>
          <Link to={`/orders/${order.id}`}>View order</Link>
        </Button>
      )}
    </div>
  )
}
