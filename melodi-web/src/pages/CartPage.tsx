import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { deleteJson, getJson, patchJson, postJson } from '../api/client.ts'
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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

type Instrument = { id: number; name: string }

type CartItem = {
  id: number
  mode: string
  quantity: number
  rentalStartDate?: string | null
  rentalEndDate?: string | null
  instrument: Instrument
}

type OrderDetail = {
  order: { id: number }
}

function formatDate(iso: string) {
  try {
    return new Date(iso).toLocaleDateString()
  } catch {
    return iso
  }
}

export function CartPage() {
  const { token } = useAuth()
  const navigate = useNavigate()
  const [items, setItems] = useState<CartItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [checkingOut, setCheckingOut] = useState(false)

  const loadCart = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError(null)
    try {
      const data = await getJson<CartItem[]>('/api/cart', { token })
      setItems(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load cart')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    void loadCart()
  }, [loadCart])

  async function updateQty(item: CartItem, quantity: number) {
    if (!token || quantity < 1) return
    setBusyId(item.id)
    setError(null)
    try {
      await patchJson(`/api/cart/items/${item.id}`, { quantity }, { token })
      await loadCart()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Update failed')
    } finally {
      setBusyId(null)
    }
  }

  async function removeItem(id: number) {
    if (!token) return
    setBusyId(id)
    setError(null)
    try {
      await deleteJson(`/api/cart/items/${id}`, { token })
      await loadCart()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Remove failed')
    } finally {
      setBusyId(null)
    }
  }

  async function placeOrder() {
    if (!token) return
    setCheckingOut(true)
    setError(null)
    try {
      const detail = await postJson<OrderDetail>('/api/orders/checkout', {}, { token })
      navigate('/checkout', { state: { orderId: detail.order.id } })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Checkout failed')
    } finally {
      setCheckingOut(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="page-intro space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Cart</h1>
        <p className="text-sm text-muted-foreground">Review items before checkout.</p>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading cart…</p>
      ) : items.length === 0 ? (
        <Card className="ui-surface">
          <CardContent className="py-8 text-center text-sm text-muted-foreground">
            Your cart is empty.{' '}
            <Link to="/instruments" className="text-primary underline-offset-4 hover:underline">
              Browse instruments
            </Link>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="space-y-3">
            {items.map((item) => (
              <Card key={item.id} className="ui-surface">
                <CardHeader className="pb-2">
                  <CardTitle className="text-base">{item.instrument.name}</CardTitle>
                  <CardDescription>
                    {item.mode} · qty {item.quantity}
                    {item.mode === 'RENT' && item.rentalStartDate && item.rentalEndDate
                      ? ` · ${formatDate(item.rentalStartDate)} to ${formatDate(item.rentalEndDate)}`
                      : ''}
                  </CardDescription>
                </CardHeader>
                <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-end">
                  <div className="space-y-2">
                    <Label htmlFor={`qty-${item.id}`}>Quantity</Label>
                    <Input
                      id={`qty-${item.id}`}
                      type="number"
                      min={1}
                      className="w-24"
                      value={item.quantity}
                      disabled={busyId === item.id}
                      onChange={(e) => {
                        const n = Number(e.target.value)
                        if (n >= 1) void updateQty(item, n)
                      }}
                    />
                  </div>
                  <Button
                    type="button"
                    variant="destructive"
                    size="sm"
                    className={withIcon}
                    disabled={busyId === item.id}
                    onClick={() => void removeItem(item.id)}
                  >
                    <IconLabel icon={icons.trash}>Remove</IconLabel>
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
          <Button className={`w-full sm:w-auto ${withIcon}`} disabled={checkingOut} onClick={() => void placeOrder()}>
            <IconLabel icon={icons.bag}>
              {checkingOut ? 'Placing order…' : 'Place order'}
            </IconLabel>
          </Button>
        </>
      )}
    </div>
  )
}
