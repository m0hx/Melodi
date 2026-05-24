import { type FormEvent, useState } from 'react'
import { Link } from 'react-router-dom'
import { postJson } from '../api/client.ts'
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

type RegisterResponse = {
  id: number
  email: string
  fullName: string
}

export function SignUpPage() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSuccess(null)
    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }
    setLoading(true)
    try {
      await postJson<RegisterResponse>('/auth/users/register', {
        name,
        email,
        password,
      })
      setSuccess(
        `Account created. We sent a verification link to ${email}. Open your inbox, verify your email, then sign in.`,
      )
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sign up failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="ui-surface mx-auto w-full max-w-md">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl font-semibold tracking-tight">
          Create account
        </CardTitle>
        <CardDescription>
          Register with your name, email, and password.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {success ? (
          <div className="space-y-4">
            <p
              className="ui-notice"
              role="status"
            >
              {success}
            </p>
            <Button className={`w-full ${withIcon}`} asChild>
              <Link to="/signin">
                <IconLabel icon={icons.signIn}>Go to sign in</IconLabel>
              </Link>
            </Button>
          </div>
        ) : (
          <form className="space-y-4" onSubmit={onSubmit}>
            <div className="space-y-2">
              <Label htmlFor="signup-name">Full name</Label>
              <Input
                id="signup-name"
                type="text"
                autoComplete="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                aria-invalid={error ? true : undefined}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="signup-email">Email</Label>
              <Input
                id="signup-email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                aria-invalid={error ? true : undefined}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="signup-password">Password</Label>
              <Input
                id="signup-password"
                type="password"
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                aria-invalid={error ? true : undefined}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="signup-confirm-password">Confirm password</Label>
              <Input
                id="signup-confirm-password"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
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
              <IconLabel icon={icons.signUp}>
                {loading ? 'Creating account…' : 'Create account'}
              </IconLabel>
            </Button>
          </form>
        )}
      </CardContent>
      <CardFooter className="flex flex-col border-t border-border/60 bg-transparent py-4">
        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{' '}
          <Link
            to="/signin"
            className="font-medium text-primary underline-offset-4 hover:underline"
          >
            Sign in
          </Link>
        </p>
      </CardFooter>
    </Card>
  )
}
