import { type FormEvent, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { getJson, postJson } from '../api/client.ts'
import { useAuth } from '../auth/AuthContext.tsx'
import { Button } from '@/components/ui/button'
import { IconLabel, icons, withIcon } from '@/components/icons.tsx'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

type LoginResponse = { message: string }

type LocationState = { message?: string }

function isLoginError(message: string) {
  return message.startsWith('Error')
}

export function SignInPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { setToken } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [info] = useState<string | null>(
    () => (location.state as LocationState | null)?.message ?? null,
  )
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const data = await postJson<LoginResponse>('/auth/users/login', {
        email,
        password,
      })
      if (isLoginError(data.message)) {
        setError(data.message.replace(/^Error\s*:\s*/i, '').trim() || data.message)
        return
      }
      setToken(data.message)
      let destination = '/'
      try {
        const profile = await getJson<{ role?: { name: string } }>('/api/profile', {
          token: data.message,
        })
        if (profile.role?.name === 'ADMIN') destination = '/admin'
      } catch {
        /* keep home */
      }
      navigate(destination, { state: { message: 'Signed in successfully.' } })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sign in failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="ui-surface mx-auto w-full max-w-md">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl font-semibold tracking-tight">
          Sign in
        </CardTitle>
        <CardDescription>
          Enter your email and password to continue.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={onSubmit}>
          {info ? (
            <p
              className="ui-notice"
              role="status"
            >
              {info}
            </p>
          ) : null}
          <div className="space-y-2">
            <Label htmlFor="signin-email">Email</Label>
            <Input
              id="signin-email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              aria-invalid={error ? true : undefined}
            />
          </div>
          <div className="space-y-2">
            <div className="flex items-center justify-between gap-2">
              <Label htmlFor="signin-password">Password</Label>
              <Link
                to="/forgot-password"
                className="text-xs font-medium text-primary underline-offset-4 hover:underline"
              >
                Forgot password?
              </Link>
            </div>
            <Input
              id="signin-password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              aria-invalid={error ? true : undefined}
            />
          </div>
          {error ? (
            <p className="text-sm text-destructive" role="alert">
              {error}
            </p>
          ) : null}
          <Button type="submit" className={`w-full ${withIcon}`} disabled={loading}>
            <IconLabel icon={icons.signIn}>{loading ? 'Signing in…' : 'Sign in'}</IconLabel>
          </Button>
        </form>
      </CardContent>
      <CardFooter className="flex flex-col border-t border-border/60 bg-transparent py-4">
        <p className="text-center text-sm text-muted-foreground">
          No account?{' '}
          <Link
            to="/signup"
            className="font-medium text-primary underline-offset-4 hover:underline"
          >
            Sign up
          </Link>
        </p>
      </CardFooter>
    </Card>
  )
}
