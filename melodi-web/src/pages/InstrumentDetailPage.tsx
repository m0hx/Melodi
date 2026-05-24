import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { deleteJson, getJson, instrumentImageUrl, postJson, putImage } from '../api/client.ts'
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

function dateToInstant(date: string, endOfDay = false): string {
  const time = endOfDay ? 'T23:59:59' : 'T00:00:00'
  return new Date(`${date}${time}`).toISOString()
}

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
  imageName?: string | null
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

function normalizeStatus(status?: string | null) {
  return status?.toUpperCase() ?? ''
}

function isCartAllowed(status?: string | null) {
  return normalizeStatus(status) === 'AVAILABLE'
}

export function InstrumentDetailPage() {
  const { token } = useAuth()
  const { instrumentId: idParam } = useParams<{ instrumentId: string }>()
  const instrumentId = idParam != null ? Number(idParam) : NaN

  const [instrument, setInstrument] = useState<Instrument | null>(null)
  const [reviews, setReviews] = useState<Review[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [cartMode, setCartMode] = useState<'BUY' | 'RENT'>('BUY')
  const [cartQty, setCartQty] = useState(1)
  const [rentStart, setRentStart] = useState('')
  const [rentEnd, setRentEnd] = useState('')
  const [cartMsg, setCartMsg] = useState<string | null>(null)
  const [cartErr, setCartErr] = useState<string | null>(null)
  const [addingCart, setAddingCart] = useState(false)

  const [wishlistMsg, setWishlistMsg] = useState<string | null>(null)
  const [wishlistErr, setWishlistErr] = useState<string | null>(null)
  const [addingWishlist, setAddingWishlist] = useState(false)

  const [isAdmin, setIsAdmin] = useState(false)
  const [imageVersion, setImageVersion] = useState(0)
  const [imageMsg, setImageMsg] = useState<string | null>(null)
  const [uploadingImage, setUploadingImage] = useState(false)
  const [removingImage, setRemovingImage] = useState(false)
  const imageInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    let cancelled = false
    if (!token) {
      setIsAdmin(false)
      return
    }
    ;(async () => {
      try {
        const profile = await getJson<{ role?: { name: string } }>('/api/profile', { token })
        if (!cancelled) setIsAdmin(profile.role?.name === 'ADMIN')
      } catch {
        if (!cancelled) setIsAdmin(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [token])

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

  async function addToCart() {
    if (!token) return
    setCartErr(null)
    setCartMsg(null)
    setAddingCart(true)
    try {
      const body: Record<string, unknown> = {
        instrumentId,
        mode: cartMode,
        quantity: cartQty,
      }
      if (cartMode === 'RENT') {
        if (!rentStart || !rentEnd) {
          setCartErr('Pick rental start and end dates')
          return
        }
        body.rentalStartDate = dateToInstant(rentStart, false)
        body.rentalEndDate = dateToInstant(rentEnd, true)
      }
      await postJson('/api/cart/items', body, { token })
      setCartMsg('Added to cart.')
    } catch (err) {
      setCartErr(err instanceof Error ? err.message : 'Could not add to cart')
    } finally {
      setAddingCart(false)
    }
  }

  async function addToWishlist() {
    if (!token) return
    setWishlistErr(null)
    setWishlistMsg(null)
    setAddingWishlist(true)
    try {
      await postJson('/api/wishlist/items', { instrumentId }, { token })
      setWishlistMsg('Saved to wishlist.')
    } catch (err) {
      setWishlistErr(err instanceof Error ? err.message : 'Could not save to wishlist')
    } finally {
      setAddingWishlist(false)
    }
  }

  async function reloadInstrument() {
    const data = await getJson<Instrument>(`/api/instruments/${instrumentId}`)
    setInstrument(data)
  }

  async function onUploadInstrumentImage(file: File) {
    if (!token) return
    setUploadingImage(true)
    setImageMsg(null)
    try {
      const msg = await putImage(`/api/instruments/${instrumentId}/image`, file, { token })
      setImageMsg(msg)
      setImageVersion(Date.now())
      await reloadInstrument()
    } catch (err) {
      setImageMsg(err instanceof Error ? err.message : 'Upload failed')
    } finally {
      setUploadingImage(false)
      if (imageInputRef.current) imageInputRef.current.value = ''
    }
  }

  async function onRemoveInstrumentImage() {
    if (!token) return
    setRemovingImage(true)
    setImageMsg(null)
    try {
      await deleteJson<string>(`/api/instruments/${instrumentId}/image`, { token })
      setImageMsg('Instrument image removed.')
      setImageVersion(Date.now())
      await reloadInstrument()
    } catch (err) {
      setImageMsg(err instanceof Error ? err.message : 'Remove failed')
    } finally {
      setRemovingImage(false)
    }
  }

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
          <Button asChild variant="outline" className={withIcon}>
            <Link to="/instruments">
              <IconLabel icon={icons.arrowLeft}>Back to catalog</IconLabel>
            </Link>
          </Button>
        </CardContent>
      </Card>
    )
  }

  const status = normalizeStatus(instrument.status)
  const cartAllowed = isCartAllowed(instrument.status)

  return (
    <div className="space-y-8">
      {instrument.imageName ? (
        <img
          src={instrumentImageUrl(instrument.id, imageVersion)}
          alt=""
          className="max-h-80 w-full rounded-xl border border-border/60 object-cover"
        />
      ) : (
        <div className="flex max-h-48 min-h-40 w-full items-center justify-center rounded-xl border border-dashed border-border/60 bg-muted/20 text-sm text-muted-foreground">
          No product image
        </div>
      )}

      {isAdmin ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">Product image (admin)</CardTitle>
            <CardDescription>Upload or replace the catalog photo for this instrument.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            <input
              ref={imageInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={(e) => {
                const file = e.target.files?.[0]
                if (file) void onUploadInstrumentImage(file)
              }}
            />
            <Button
              type="button"
              variant="outline"
              className={withIcon}
              disabled={uploadingImage}
              onClick={() => imageInputRef.current?.click()}
            >
              <IconLabel icon={icons.image}>
                {uploadingImage ? 'Uploading…' : instrument.imageName ? 'Change image' : 'Upload image'}
              </IconLabel>
            </Button>
            {instrument.imageName ? (
              <Button
                type="button"
                variant="outline"
                className={withIcon}
                disabled={removingImage}
                onClick={() => void onRemoveInstrumentImage()}
              >
                <IconLabel icon={icons.trash}>
                  {removingImage ? 'Removing…' : 'Remove image'}
                </IconLabel>
              </Button>
            ) : null}
            {imageMsg ? <p className="w-full text-sm text-muted-foreground">{imageMsg}</p> : null}
          </CardContent>
        </Card>
      ) : null}

      <div className="page-intro flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-2">
          <Button variant="ghost" size="sm" asChild className={`-ml-2 w-fit ${withIcon}`}>
            <Link to="/instruments">
              <IconLabel icon={icons.arrowLeft}>Back to catalog</IconLabel>
            </Link>
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
        <div className="ui-surface shrink-0 px-4 py-3 text-sm">
          <p className="font-medium text-foreground">Buy {formatMoney(instrument.purchasePrice)}</p>
          <p className="text-muted-foreground">
            Rent {formatMoney(instrument.rentalPricePerDay)}/day
          </p>
          <p className="mt-2 text-xs text-muted-foreground">
            {instrument.purchaseStock} available to buy · {instrument.rentalStock} available to rent
          </p>
          <p className="text-xs text-muted-foreground">Status: {instrument.status ?? 'N/A'}</p>
          {!cartAllowed ? (
            <p className="mt-2 text-xs text-muted-foreground">
              Not available to buy or rent right now. Save it to your wishlist and check back later.
            </p>
          ) : null}
        </div>
      </div>

      {!isAdmin ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">Add to cart</CardTitle>
            <CardDescription>Sign in required to save items in your cart.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {!token ? (
              <Button asChild className={withIcon}>
                <Link to="/signin" state={{ from: `/instruments/${instrumentId}` }}>
                  <IconLabel icon={icons.signIn}>Sign in to add to cart</IconLabel>
                </Link>
              </Button>
            ) : !cartAllowed ? (
              <p className="text-sm text-muted-foreground">
                This instrument is {status.toLowerCase()}. You can save it to your wishlist below.
              </p>
            ) : (
              <>
                <div className="flex flex-wrap gap-2">
                  <Button
                    type="button"
                    size="sm"
                    variant={cartMode === 'BUY' ? 'default' : 'outline'}
                    onClick={() => setCartMode('BUY')}
                  >
                    Buy
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    variant={cartMode === 'RENT' ? 'default' : 'outline'}
                    onClick={() => setCartMode('RENT')}
                  >
                    Rent
                  </Button>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cart-qty">Quantity</Label>
                  <Input
                    id="cart-qty"
                    type="number"
                    min={1}
                    className="w-24"
                    value={cartQty}
                    onChange={(e) => setCartQty(Math.max(1, Number(e.target.value) || 1))}
                  />
                </div>
                {cartMode === 'RENT' ? (
                  <div className="grid gap-3 sm:grid-cols-2">
                    <div className="space-y-2">
                      <Label htmlFor="rent-start">Start date</Label>
                      <Input
                        id="rent-start"
                        type="date"
                        value={rentStart}
                        onChange={(e) => setRentStart(e.target.value)}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="rent-end">End date</Label>
                      <Input
                        id="rent-end"
                        type="date"
                        value={rentEnd}
                        onChange={(e) => setRentEnd(e.target.value)}
                      />
                    </div>
                  </div>
                ) : null}
                {cartErr ? (
                  <p className="text-sm text-destructive" role="alert">
                    {cartErr}
                  </p>
                ) : null}
                {cartMsg ? (
                  <p className="text-sm text-foreground" role="status">
                    {cartMsg}{' '}
                    <Link to="/cart" className="text-primary underline-offset-4 hover:underline">
                      View cart
                    </Link>
                  </p>
                ) : null}
                <div className="flex flex-wrap gap-2">
                  <Button type="button" className={withIcon} disabled={addingCart} onClick={() => void addToCart()}>
                    <IconLabel icon={icons.cartPlus}>
                      {addingCart ? 'Adding…' : 'Add to cart'}
                    </IconLabel>
                  </Button>
                  <Button variant="outline" asChild className={withIcon}>
                    <Link to="/cart">
                      <IconLabel icon={icons.cart}>Go to cart</IconLabel>
                    </Link>
                  </Button>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      ) : null}

      {!isAdmin ? (
        <Card className="ui-surface">
          <CardHeader>
            <CardTitle className="text-base">Wishlist</CardTitle>
            <CardDescription>
              Save this instrument for later — including discontinued or temporarily unavailable
              items.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {!token ? (
              <Button asChild variant="outline" className={withIcon}>
                <Link to="/signin" state={{ from: `/instruments/${instrumentId}` }}>
                  <IconLabel icon={icons.signIn}>Sign in to save</IconLabel>
                </Link>
              </Button>
            ) : (
              <>
                {wishlistErr ? (
                  <p className="text-sm text-destructive" role="alert">
                    {wishlistErr}
                  </p>
                ) : null}
                {wishlistMsg ? (
                  <p className="text-sm text-foreground" role="status">
                    {wishlistMsg}{' '}
                    <Link to="/wishlist" className="text-primary underline-offset-4 hover:underline">
                      View wishlist
                    </Link>
                  </p>
                ) : null}
                <div className="flex flex-wrap gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    className={withIcon}
                    disabled={addingWishlist}
                    onClick={() => void addToWishlist()}
                  >
                    <IconLabel icon={icons.heart}>
                      {addingWishlist ? 'Saving…' : 'Save to wishlist'}
                    </IconLabel>
                  </Button>
                  <Button variant="ghost" asChild className={withIcon}>
                    <Link to="/wishlist">
                      <IconLabel icon={icons.heart}>Go to wishlist</IconLabel>
                    </Link>
                  </Button>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      ) : null}

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
