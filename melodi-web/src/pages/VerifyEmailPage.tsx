import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getJson } from '../api/client.ts'
import { Button } from '@/components/ui/button'
import { IconLabel, icons, withIcon } from '@/components/icons.tsx'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

type MessageResponse = { message: string }

type CachedVerify = { status: 'ok' | 'error'; message: string }

function cacheKey(token: string) {
  return `melodi-verify-${token}`
}

function readCache(token: string): CachedVerify | null {
  try {
    const raw = sessionStorage.getItem(cacheKey(token))
    if (!raw) return null
    return JSON.parse(raw) as CachedVerify
  } catch {
    return null
  }
}

function writeCache(token: string, value: CachedVerify) {
  sessionStorage.setItem(cacheKey(token), JSON.stringify(value))
}

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!token) {
      setLoading(false)
      setError('Missing verification token. Use the link from your email.')
      return
    }

    const cached = readCache(token)
    if (cached?.status === 'ok') {
      setMessage(cached.message)
      setError(null)
      setLoading(false)
      return
    }

    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      setMessage(null)
      try {
        const data = await getJson<MessageResponse>(
          `/auth/users/register/verify?token=${encodeURIComponent(token)}`,
        )
        const text = data.message || 'Email verified successfully. You can log in now.'
        writeCache(token, { status: 'ok', message: text })
        if (!cancelled) {
          setMessage(text)
          setError(null)
        }
      } catch (err) {
        const ok = readCache(token)
        if (ok?.status === 'ok') {
          if (!cancelled) {
            setMessage(ok.message)
            setError(null)
          }
          return
        }
        const msg =
          err instanceof Error
            ? err.message
            : 'Verification failed'
        if (!cancelled) {
          setError(msg)
          setMessage(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [token])

  const description = loading
    ? 'Confirming your Melodi account…'
    : error
      ? 'We could not verify this link.'
      : 'Your email is verified. You can sign in now.'

  return (
    <Card className="ui-surface mx-auto w-full max-w-md">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl font-semibold tracking-tight">Verify email</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {loading ? (
          <p className="text-sm text-muted-foreground">Verifying…</p>
        ) : error ? (
          <>
            <p className="text-sm text-destructive" role="alert">
              {error}
            </p>
            <p className="text-sm text-muted-foreground">
              If you already verified, try signing in. Otherwise request a new link from sign up.
            </p>
            <Button variant="outline" asChild className={withIcon}>
              <Link to="/signin">
                <IconLabel icon={icons.signIn}>Sign in</IconLabel>
              </Link>
            </Button>
          </>
        ) : (
          <>
            <p className="ui-notice" role="status">
              {message}
            </p>
            <Button className={`w-full ${withIcon}`} asChild>
              <Link to="/signin">
                <IconLabel icon={icons.signIn}>Sign in</IconLabel>
              </Link>
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  )
}
