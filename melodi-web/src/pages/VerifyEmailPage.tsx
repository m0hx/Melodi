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
            <Button variant="outline" asChild className={withIcon}>
              <Link to="/signup">
                <IconLabel icon={icons.signUp}>Back to sign up</IconLabel>
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
