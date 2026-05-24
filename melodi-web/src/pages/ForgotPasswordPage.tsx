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

type MessageResponse = { message: string }

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setMessage(null)
    setLoading(true)
    try {
      const data = await postJson<MessageResponse>('/auth/users/resetPassword', {
        emailAddress: email.trim(),
      })
      setMessage(data.message)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="ui-surface mx-auto w-full max-w-md">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl font-semibold tracking-tight">Forgot password</CardTitle>
        <CardDescription>
          Enter your email and we will send a reset link if an account exists.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {message ? (
          <div className="space-y-4">
            <p
              className="ui-notice"
              role="status"
            >
              {message}
            </p>
            <Button className={`w-full ${withIcon}`} asChild>
              <Link to="/signin">
                <IconLabel icon={icons.signIn}>Back to sign in</IconLabel>
              </Link>
            </Button>
          </div>
        ) : (
          <form className="space-y-4" onSubmit={onSubmit}>
            <div className="space-y-2">
              <Label htmlFor="forgot-email">Email</Label>
              <Input
                id="forgot-email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            {error ? (
              <p className="text-sm text-destructive" role="alert">
                {error}
              </p>
            ) : null}
            <Button type="submit" className={`w-full ${withIcon}`} disabled={loading}>
              <IconLabel icon={icons.key}>
                {loading ? 'Sending…' : 'Send reset link'}
              </IconLabel>
            </Button>
          </form>
        )}
      </CardContent>
      <CardFooter className="flex flex-col border-t border-border/60 bg-transparent py-4">
        <p className="text-center text-sm text-muted-foreground">
          Remember your password?{' '}
          <Link to="/signin" className="font-medium text-primary underline-offset-4 hover:underline">
            Sign in
          </Link>
        </p>
      </CardFooter>
    </Card>
  )
}
