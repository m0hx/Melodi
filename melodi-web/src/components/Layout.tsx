import { useEffect, useMemo, useState } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.tsx'
import { getJson } from '../api/client.ts'
import { Button } from '@/components/ui/button'

function isActivePath(pathname: string, href: string) {
  return href === '/' ? pathname === '/' : pathname.startsWith(href)
}

type Profile = { role: { name: string } }

export function Layout() {
  const { token, logout } = useAuth()
  const location = useLocation()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [isAdmin, setIsAdmin] = useState(false)

  useEffect(() => {
    let cancelled = false
    setIsAdmin(false)
    if (!token) return
    ;(async () => {
      try {
        const profile = await getJson<Profile>('/api/profile', { token })
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
    setMobileOpen(false)
  }, [location.pathname])

  const links = useMemo(
    () => [
      { href: '/instruments', label: 'Instruments' },
      ...(token
        ? [
            { href: '/cart', label: 'Cart' },
            { href: '/wishlist', label: 'Wishlist' },
            { href: '/orders', label: 'Orders' },
            { href: '/rentals', label: 'Rentals' },
            { href: '/profile', label: 'Profile' },
          ]
        : []),
      ...(isAdmin ? [{ href: '/admin', label: 'Admin' }] : []),
    ],
    [token, isAdmin],
  )

  const year = new Date().getFullYear()

  return (
    <div className="flex min-h-svh flex-col">
      <header className="sticky top-0 z-50 border-b border-border/80 bg-background/80 backdrop-blur-md">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3 md:px-6">
          <div className="flex items-center gap-4">
            <Link
              to="/"
              className="font-heading text-lg font-semibold tracking-tight text-foreground transition-colors hover:text-primary"
              onClick={() => setMobileOpen(false)}
            >
              Melodi
            </Link>
            <nav className="hidden items-center gap-1 sm:flex">
              {links.map((l) => (
                <Button
                  key={l.href}
                  variant={isActivePath(location.pathname, l.href) ? 'secondary' : 'ghost'}
                  size="sm"
                  asChild
                >
                  <Link to={l.href}>{l.label}</Link>
                </Button>
              ))}
            </nav>
          </div>

          <nav className="flex shrink-0 items-center gap-2">
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className="sm:hidden"
              aria-expanded={mobileOpen}
              aria-controls="mobile-nav"
              onClick={() => setMobileOpen((v) => !v)}
            >
              Menu
            </Button>
            {token ? (
              <Button type="button" variant="outline" size="sm" onClick={logout}>
                Sign out
              </Button>
            ) : (
              <>
                <Button variant="outline" size="sm" asChild>
                  <Link to="/signin">Sign in</Link>
                </Button>
                <Button size="sm" asChild>
                  <Link to="/signup">Sign up</Link>
                </Button>
              </>
            )}
          </nav>
        </div>

        {mobileOpen ? (
          <div id="mobile-nav" className="border-t border-border/70 bg-background/70 sm:hidden">
            <div className="mx-auto flex max-w-6xl flex-col gap-2 px-4 py-3 md:px-6">
              {links.map((l) => (
                <Button
                  key={l.href}
                  variant={isActivePath(location.pathname, l.href) ? 'secondary' : 'ghost'}
                  className="justify-start"
                  asChild
                >
                  <Link to={l.href} onClick={() => setMobileOpen(false)}>
                    {l.label}
                  </Link>
                </Button>
              ))}
            </div>
          </div>
        ) : null}
      </header>

      <main className="flex-1 bg-background bg-[radial-gradient(ellipse_100%_50%_at_50%_-20%,rgb(255_255_255/0.04),transparent_55%)]">
        <div className="mx-auto w-full max-w-5xl px-4 py-10 md:px-6 md:py-14">
          <Outlet />
        </div>
      </main>

      <footer className="border-t border-border/70 bg-background/70">
        <div className="mx-auto flex max-w-6xl flex-col gap-2 px-4 py-8 md:flex-row md:items-center md:justify-between md:px-6">
          <div className="space-y-1">
            <p className="font-heading text-base font-semibold tracking-tight text-foreground">
              Melodi Instruments
            </p>
            <p className="text-sm text-muted-foreground">
              Your stage starts here
            </p>
          </div>
          <p className="text-xs text-muted-foreground">
            © {year} Melodi Instruments
          </p>
        </div>
      </footer>
    </div>
  )
}
