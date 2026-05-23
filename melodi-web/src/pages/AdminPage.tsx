import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  deleteJson,
  getJson,
  instrumentImageUrl,
  postJson,
  putJson,
} from '../api/client.ts'
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

type Tab = 'instruments' | 'categories' | 'brands'

type Profile = {
  fullName: string
  email: string
  role?: { name: string }
}

type Category = {
  id: number
  name: string
  description?: string | null
  imagePath?: string | null
}

type Brand = {
  id: number
  name: string
  country?: string | null
  description?: string | null
}

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
  category?: { id: number; name: string } | null
  brand?: { id: number; name: string } | null
}

const STATUSES = ['AVAILABLE', 'UNAVAILABLE', 'DISCONTINUED', 'HIDDEN'] as const
const CONDITIONS = ['NEW', 'USED', 'REFURBISHED'] as const

const emptyInstrumentForm = {
  name: '',
  description: '',
  categoryId: '',
  brandId: '',
  purchasePrice: '',
  rentalPricePerDay: '',
  purchaseStock: '0',
  rentalStock: '0',
  condition: 'NEW',
  status: 'AVAILABLE',
}

export function AdminPage() {
  const { token } = useAuth()
  const [tab, setTab] = useState<Tab>('instruments')
  const [profile, setProfile] = useState<Profile | null>(null)
  const [categories, setCategories] = useState<Category[]>([])
  const [brands, setBrands] = useState<Brand[]>([])
  const [instruments, setInstruments] = useState<Instrument[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [msg, setMsg] = useState<string | null>(null)

  const [categoryForm, setCategoryForm] = useState({ name: '', description: '' })
  const [editingCategoryId, setEditingCategoryId] = useState<number | null>(null)

  const [brandForm, setBrandForm] = useState({ name: '', country: '', description: '' })
  const [editingBrandId, setEditingBrandId] = useState<number | null>(null)

  const [instrumentForm, setInstrumentForm] = useState(emptyInstrumentForm)
  const [editingInstrumentId, setEditingInstrumentId] = useState<number | null>(null)
  const [statusFilter, setStatusFilter] = useState<string>('all')

  const loadAll = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError(null)
    try {
      const [profileData, categoriesData, brandsData, instrumentsData] = await Promise.all([
        getJson<Profile>('/api/profile', { token }),
        getJson<Category[]>('/api/categories', { token }),
        getJson<Brand[]>('/api/brands', { token }),
        getJson<Instrument[]>('/api/instruments', { token }),
      ])
      setProfile(profileData)
      setCategories(Array.isArray(categoriesData) ? categoriesData : [])
      setBrands(Array.isArray(brandsData) ? brandsData : [])
      setInstruments(Array.isArray(instrumentsData) ? instrumentsData : [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load admin data')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    void loadAll()
  }, [loadAll])

  const filteredInstruments =
    statusFilter === 'all'
      ? instruments
      : instruments.filter((i) => i.status?.toUpperCase() === statusFilter)

  const hiddenCount = instruments.filter((i) => i.status?.toUpperCase() === 'HIDDEN').length

  function flash(message: string) {
    setMsg(message)
    setError(null)
  }

  async function onSaveCategory(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setError(null)
    try {
      const body = {
        name: categoryForm.name.trim(),
        description: categoryForm.description.trim() || null,
        imagePath: null,
      }
      if (editingCategoryId != null) {
        await putJson<Category>(`/api/categories/${editingCategoryId}`, body, { token })
        flash('Category updated.')
      } else {
        await postJson<Category>('/api/categories', body, { token })
        flash('Category created.')
      }
      setCategoryForm({ name: '', description: '' })
      setEditingCategoryId(null)
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Category save failed')
    }
  }

  async function onDeleteCategory(id: number) {
    if (!token || !window.confirm('Delete this category?')) return
    setError(null)
    try {
      await deleteJson(`/api/categories/${id}`, { token })
      flash('Category deleted.')
      if (editingCategoryId === id) {
        setEditingCategoryId(null)
        setCategoryForm({ name: '', description: '' })
      }
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed')
    }
  }

  async function onSaveBrand(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setError(null)
    try {
      const body = {
        name: brandForm.name.trim(),
        country: brandForm.country.trim() || null,
        description: brandForm.description.trim() || null,
      }
      if (editingBrandId != null) {
        await putJson<Brand>(`/api/brands/${editingBrandId}`, body, { token })
        flash('Brand updated.')
      } else {
        await postJson<Brand>('/api/brands', body, { token })
        flash('Brand created.')
      }
      setBrandForm({ name: '', country: '', description: '' })
      setEditingBrandId(null)
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Brand save failed')
    }
  }

  async function onDeleteBrand(id: number) {
    if (!token || !window.confirm('Delete this brand?')) return
    setError(null)
    try {
      await deleteJson(`/api/brands/${id}`, { token })
      flash('Brand deleted.')
      if (editingBrandId === id) {
        setEditingBrandId(null)
        setBrandForm({ name: '', country: '', description: '' })
      }
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed')
    }
  }

  function startEditInstrument(item: Instrument) {
    setEditingInstrumentId(item.id)
    setInstrumentForm({
      name: item.name,
      description: item.description ?? '',
      categoryId: String(item.category?.id ?? ''),
      brandId: String(item.brand?.id ?? ''),
      purchasePrice: String(item.purchasePrice),
      rentalPricePerDay: String(item.rentalPricePerDay),
      purchaseStock: String(item.purchaseStock),
      rentalStock: String(item.rentalStock),
      condition: item.condition ?? 'NEW',
      status: item.status ?? 'AVAILABLE',
    })
    setTab('instruments')
  }

  async function onSaveInstrument(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setError(null)
    const categoryId = Number(instrumentForm.categoryId)
    const brandId = Number(instrumentForm.brandId)
    if (!instrumentForm.name.trim() || !categoryId || !brandId) {
      setError('Name, category, and brand are required.')
      return
    }
    try {
      const body = {
        name: instrumentForm.name.trim(),
        description: instrumentForm.description.trim() || null,
        category: { id: categoryId },
        brand: { id: brandId },
        purchasePrice: Number(instrumentForm.purchasePrice),
        rentalPricePerDay: Number(instrumentForm.rentalPricePerDay),
        purchaseStock: Number(instrumentForm.purchaseStock),
        rentalStock: Number(instrumentForm.rentalStock),
        condition: instrumentForm.condition,
        status: instrumentForm.status,
      }
      if (editingInstrumentId != null) {
        await putJson<Instrument>(`/api/instruments/${editingInstrumentId}`, body, { token })
        flash('Instrument updated.')
      } else {
        await postJson<Instrument>('/api/instruments', body, { token })
        flash('Instrument created.')
      }
      setInstrumentForm(emptyInstrumentForm)
      setEditingInstrumentId(null)
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Instrument save failed')
    }
  }

  async function onDeleteInstrument(id: number) {
    if (!token || !window.confirm('Delete this instrument?')) return
    setError(null)
    try {
      await deleteJson(`/api/instruments/${id}`, { token })
      flash('Instrument deleted.')
      if (editingInstrumentId === id) {
        setEditingInstrumentId(null)
        setInstrumentForm(emptyInstrumentForm)
      }
      await loadAll()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed')
    }
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Admin</h1>
        <p className="text-sm text-muted-foreground">
          {profile
            ? `${profile.fullName} · ${profile.email} · ${profile.role?.name ?? 'ADMIN'}`
            : null}
        </p>
        {!loading ? (
          <p className="text-sm text-muted-foreground">
            {instruments.length} instruments ({hiddenCount} hidden) · {categories.length}{' '}
            categories · {brands.length} brands
          </p>
        ) : null}
      </div>

      <div className="flex flex-wrap gap-2">
        <Button
          type="button"
          size="sm"
          variant={tab === 'instruments' ? 'default' : 'outline'}
          onClick={() => setTab('instruments')}
        >
          Instruments
        </Button>
        <Button
          type="button"
          size="sm"
          variant={tab === 'categories' ? 'default' : 'outline'}
          onClick={() => setTab('categories')}
        >
          Categories
        </Button>
        <Button
          type="button"
          size="sm"
          variant={tab === 'brands' ? 'default' : 'outline'}
          onClick={() => setTab('brands')}
        >
          Brands
        </Button>
        <Button size="sm" variant="outline" asChild>
          <Link to="/admin/orders">All orders</Link>
        </Button>
        <Button size="sm" variant="outline" asChild>
          <Link to="/admin/rentals">All rentals</Link>
        </Button>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}
      {msg ? (
        <p className="text-sm text-foreground" role="status">
          {msg}
        </p>
      ) : null}

      {loading ? (
        <p className="text-sm text-muted-foreground">Loading…</p>
      ) : tab === 'categories' ? (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card className="ui-surface">
            <CardHeader>
              <CardTitle className="text-base">
                {editingCategoryId != null ? 'Edit category' : 'New category'}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <form className="space-y-3" onSubmit={onSaveCategory}>
                <div className="space-y-2">
                  <Label htmlFor="cat-name">Name</Label>
                  <Input
                    id="cat-name"
                    value={categoryForm.name}
                    onChange={(e) => setCategoryForm((f) => ({ ...f, name: e.target.value }))}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="cat-desc">Description</Label>
                  <Input
                    id="cat-desc"
                    value={categoryForm.description}
                    onChange={(e) =>
                      setCategoryForm((f) => ({ ...f, description: e.target.value }))
                    }
                  />
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button type="submit">
                    {editingCategoryId != null ? 'Save changes' : 'Create category'}
                  </Button>
                  {editingCategoryId != null ? (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => {
                        setEditingCategoryId(null)
                        setCategoryForm({ name: '', description: '' })
                      }}
                    >
                      Cancel
                    </Button>
                  ) : null}
                </div>
              </form>
            </CardContent>
          </Card>
          <Card className="ui-surface">
            <CardHeader>
              <CardTitle className="text-base">All categories</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {categories.map((c) => (
                <div
                  key={c.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-border/60 px-3 py-2 text-sm"
                >
                  <div>
                    <p className="font-medium">{c.name}</p>
                    {c.description ? (
                      <p className="text-xs text-muted-foreground">{c.description}</p>
                    ) : null}
                  </div>
                  <div className="flex gap-2">
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={() => {
                        setEditingCategoryId(c.id)
                        setCategoryForm({
                          name: c.name,
                          description: c.description ?? '',
                        })
                      }}
                    >
                      Edit
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={() => void onDeleteCategory(c.id)}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      ) : tab === 'brands' ? (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card className="ui-surface">
            <CardHeader>
              <CardTitle className="text-base">
                {editingBrandId != null ? 'Edit brand' : 'New brand'}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <form className="space-y-3" onSubmit={onSaveBrand}>
                <div className="space-y-2">
                  <Label htmlFor="brand-name">Name</Label>
                  <Input
                    id="brand-name"
                    value={brandForm.name}
                    onChange={(e) => setBrandForm((f) => ({ ...f, name: e.target.value }))}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="brand-country">Country</Label>
                  <Input
                    id="brand-country"
                    value={brandForm.country}
                    onChange={(e) => setBrandForm((f) => ({ ...f, country: e.target.value }))}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="brand-desc">Description</Label>
                  <Input
                    id="brand-desc"
                    value={brandForm.description}
                    onChange={(e) =>
                      setBrandForm((f) => ({ ...f, description: e.target.value }))
                    }
                  />
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button type="submit">
                    {editingBrandId != null ? 'Save changes' : 'Create brand'}
                  </Button>
                  {editingBrandId != null ? (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => {
                        setEditingBrandId(null)
                        setBrandForm({ name: '', country: '', description: '' })
                      }}
                    >
                      Cancel
                    </Button>
                  ) : null}
                </div>
              </form>
            </CardContent>
          </Card>
          <Card className="ui-surface">
            <CardHeader>
              <CardTitle className="text-base">All brands</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {brands.map((b) => (
                <div
                  key={b.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-border/60 px-3 py-2 text-sm"
                >
                  <div>
                    <p className="font-medium">{b.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {[b.country, b.description].filter(Boolean).join(' · ') || '—'}
                    </p>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={() => {
                        setEditingBrandId(b.id)
                        setBrandForm({
                          name: b.name,
                          country: b.country ?? '',
                          description: b.description ?? '',
                        })
                      }}
                    >
                      Edit
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={() => void onDeleteBrand(b.id)}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      ) : (
        <div className="space-y-6">
          <Card className="ui-surface">
            <CardHeader>
              <CardTitle className="text-base">
                {editingInstrumentId != null ? 'Edit instrument' : 'New instrument'}
              </CardTitle>
              <CardDescription>
                After saving, open the instrument page to upload a product image.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form className="grid gap-3 sm:grid-cols-2" onSubmit={onSaveInstrument}>
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="inst-name">Name</Label>
                  <Input
                    id="inst-name"
                    value={instrumentForm.name}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, name: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="inst-desc">Description</Label>
                  <Input
                    id="inst-desc"
                    value={instrumentForm.description}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, description: e.target.value }))
                    }
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-cat">Category</Label>
                  <select
                    id="inst-cat"
                    className="h-9 w-full rounded-md border border-border/70 bg-background px-3 text-sm"
                    value={instrumentForm.categoryId}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, categoryId: e.target.value }))
                    }
                    required
                  >
                    <option value="">Select…</option>
                    {categories.map((c) => (
                      <option key={c.id} value={String(c.id)}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-brand">Brand</Label>
                  <select
                    id="inst-brand"
                    className="h-9 w-full rounded-md border border-border/70 bg-background px-3 text-sm"
                    value={instrumentForm.brandId}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, brandId: e.target.value }))
                    }
                    required
                  >
                    <option value="">Select…</option>
                    {brands.map((b) => (
                      <option key={b.id} value={String(b.id)}>
                        {b.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-buy">Purchase price</Label>
                  <Input
                    id="inst-buy"
                    type="number"
                    min={0}
                    step="0.01"
                    value={instrumentForm.purchasePrice}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, purchasePrice: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-rent">Rental / day</Label>
                  <Input
                    id="inst-rent"
                    type="number"
                    min={0}
                    step="0.01"
                    value={instrumentForm.rentalPricePerDay}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, rentalPricePerDay: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-pstock">Purchase stock</Label>
                  <Input
                    id="inst-pstock"
                    type="number"
                    min={0}
                    value={instrumentForm.purchaseStock}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, purchaseStock: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-rstock">Rental stock</Label>
                  <Input
                    id="inst-rstock"
                    type="number"
                    min={0}
                    value={instrumentForm.rentalStock}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, rentalStock: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-condition">Condition</Label>
                  <select
                    id="inst-condition"
                    className="h-9 w-full rounded-md border border-border/70 bg-background px-3 text-sm"
                    value={instrumentForm.condition}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, condition: e.target.value }))
                    }
                  >
                    {CONDITIONS.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="inst-status">Status</Label>
                  <select
                    id="inst-status"
                    className="h-9 w-full rounded-md border border-border/70 bg-background px-3 text-sm"
                    value={instrumentForm.status}
                    onChange={(e) =>
                      setInstrumentForm((f) => ({ ...f, status: e.target.value }))
                    }
                  >
                    {STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="flex flex-wrap gap-2 sm:col-span-2">
                  <Button type="submit">
                    {editingInstrumentId != null ? 'Save instrument' : 'Create instrument'}
                  </Button>
                  {editingInstrumentId != null ? (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => {
                        setEditingInstrumentId(null)
                        setInstrumentForm(emptyInstrumentForm)
                      }}
                    >
                      Cancel
                    </Button>
                  ) : null}
                </div>
              </form>
            </CardContent>
          </Card>

          <Card className="ui-surface">
            <CardHeader className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <CardTitle className="text-base">All instruments</CardTitle>
              <select
                className="h-9 rounded-md border border-border/70 bg-background px-3 text-sm"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="all">All statuses</option>
                {STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </CardHeader>
            <CardContent className="space-y-2">
              {filteredInstruments.map((item) => (
                <div
                  key={item.id}
                  className="flex flex-col gap-3 rounded-lg border border-border/60 p-3 sm:flex-row sm:items-center"
                >
                  {item.imageName ? (
                    <img
                      src={instrumentImageUrl(item.id)}
                      alt=""
                      className="h-16 w-16 shrink-0 rounded-md border border-border/60 object-cover"
                    />
                  ) : (
                    <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-md border border-dashed border-border/60 bg-muted/20 text-[10px] text-muted-foreground">
                      No img
                    </div>
                  )}
                  <div className="min-w-0 flex-1 text-sm">
                    <p className="font-medium">{item.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {item.brand?.name} · {item.category?.name} · {item.status}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Buy stock {item.purchaseStock} · Rent stock {item.rentalStock}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <Button size="sm" variant="outline" asChild>
                      <Link to={`/instruments/${item.id}`}>Open</Link>
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={() => startEditInstrument(item)}
                    >
                      Edit
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      variant="outline"
                      onClick={() => void onDeleteInstrument(item.id)}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              ))}
              {filteredInstruments.length === 0 ? (
                <p className="text-sm text-muted-foreground">No instruments match this filter.</p>
              ) : null}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  )
}
