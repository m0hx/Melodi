import { useEffect, useMemo, useState } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.tsx'
import { getJson } from '../api/client.ts'
import { Button } from '@/components/ui/button'
import { IconLabel, icons, navIcon, withIcon } from '@/components/icons.tsx'

function isActivePath(pathname: string, href: string) {
  return href === '/' ? pathname === '/' : pathname.startsWith(href)
}

type Profile = { role?: { name: string } }

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

  const links = useMemo(() => {
    const shopLinks = isAdmin
      ? []
      : [
          { href: '/cart', label: 'Cart' },
          { href: '/wishlist', label: 'Wishlist' },
          { href: '/orders', label: 'Orders' },
          { href: '/rentals', label: 'Rentals' },
        ]
    return [
      { href: '/instruments', label: 'Instruments' },
      ...(token ? [...shopLinks, { href: '/profile', label: 'Profile' }] : []),
      ...(isAdmin ? [{ href: '/admin', label: 'Admin' }] : []),
    ]
  }, [token, isAdmin])

  const year = new Date().getFullYear()

  return (
    <div className="melodi-app-shell flex min-h-svh flex-col">
      <header className="sticky top-0 z-50 border-b border-border/80 bg-background/80 backdrop-blur-md">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3 md:px-6">
          <div className="flex items-center gap-4">
            <Link
              to="/"
              className="flex items-center gap-2 font-heading text-lg font-semibold tracking-tight text-foreground transition-colors hover:text-primary"
              onClick={() => setMobileOpen(false)}
            >
              <img
                src="/favicon-32x32.png"
                alt=""
                width={32}
                height={32}
                className="h-8 w-8 shrink-0 rounded-sm"
              />
              Melodi
            </Link>
            <nav className="hidden items-center gap-1 sm:flex">
              {links.map((l) => {
                const icon = navIcon(l.href)
                return (
                <Button
                  key={l.href}
                  variant={isActivePath(location.pathname, l.href) ? 'secondary' : 'ghost'}
                  size="sm"
                  className={withIcon}
                  asChild
                >
                  <Link to={l.href}>
                    {icon ? <IconLabel icon={icon}>{l.label}</IconLabel> : l.label}
                  </Link>
                </Button>
              )})}
            </nav>
          </div>

          <nav className="flex shrink-0 items-center gap-2">
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className={`sm:hidden ${withIcon}`}
              aria-expanded={mobileOpen}
              aria-controls="mobile-nav"
              onClick={() => setMobileOpen((v) => !v)}
            >
              <IconLabel icon={icons.bars}>Menu</IconLabel>
            </Button>
            {token ? (
              <Button type="button" variant="outline" size="sm" className={withIcon} onClick={logout}>
                <IconLabel icon={icons.signOut}>Sign out</IconLabel>
              </Button>
            ) : (
              <>
                <Button variant="outline" size="sm" className={withIcon} asChild>
                  <Link to="/signin">
                    <IconLabel icon={icons.signIn}>Sign in</IconLabel>
                  </Link>
                </Button>
                <Button size="sm" className={withIcon} asChild>
                  <Link to="/signup">
                    <IconLabel icon={icons.signUp}>Sign up</IconLabel>
                  </Link>
                </Button>
              </>
            )}
          </nav>
        </div>

        {mobileOpen ? (
          <div id="mobile-nav" className="border-t border-border/70 bg-background/70 sm:hidden">
            <div className="mx-auto flex max-w-6xl flex-col gap-2 px-4 py-3 md:px-6">
              {links.map((l) => {
                const icon = navIcon(l.href)
                return (
                <Button
                  key={l.href}
                  variant={isActivePath(location.pathname, l.href) ? 'secondary' : 'ghost'}
                  className={`justify-start ${withIcon}`}
                  asChild
                >
                  <Link to={l.href} onClick={() => setMobileOpen(false)}>
                    {icon ? <IconLabel icon={icon}>{l.label}</IconLabel> : l.label}
                  </Link>
                </Button>
              )})}
            </div>
          </div>
        ) : null}
      </header>

      <main className="flex-1">
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
