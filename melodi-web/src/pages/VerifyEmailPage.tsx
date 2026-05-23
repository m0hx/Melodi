import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getJson } from '../api/client.ts'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

type MessageResponse = { message: string }

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

    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      setMessage(null)
      try {
        const data = await getJson<MessageResponse>(
          `/auth/users/register/verify?token=${encodeURIComponent(token)}`,
        )
        if (!cancelled) setMessage(data.message)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Verification failed')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [token])

  return (
    <Card className="ui-surface mx-auto w-full max-w-md">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl font-semibold tracking-tight">Verify email</CardTitle>
        <CardDescription>Confirming your Melodi account.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {loading ? (
          <p className="text-sm text-muted-foreground">Verifying…</p>
        ) : error ? (
          <>
            <p className="text-sm text-destructive" role="alert">
              {error}
            </p>
            <Button variant="outline" asChild>
              <Link to="/signup">Back to sign up</Link>
            </Button>
          </>
        ) : (
          <>
            <p className="rounded-lg border border-border/60 bg-muted/40 px-3 py-3 text-sm text-foreground" role="status">
              {message}
            </p>
            <Button className="w-full" asChild>
              <Link to="/signin">Sign in</Link>
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  )
}
