import { type FormEvent, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
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

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setMessage(null)

    if (!token) {
      setError('Missing reset token. Use the link from your email.')
      return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    setLoading(true)
    try {
      const data = await postJson<MessageResponse>(
        `/auth/users/resetPassword?token=${encodeURIComponent(token)}`,
        { password },
      )
      setMessage(data.message)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Reset failed')
    } finally {
      setLoading(false)
    }
  }

  if (!token && !message) {
    return (
      <Card className="ui-surface mx-auto w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-base">Invalid reset link</CardTitle>
          <CardDescription>Request a new password reset email.</CardDescription>
        </CardHeader>
        <CardContent>
          <Button asChild className={withIcon}>
            <Link to="/forgot-password">
              <IconLabel icon={icons.key}>Forgot password</IconLabel>
            </Link>
          </Button>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="ui-surface mx-auto w-full max-w-md">
      <CardHeader className="space-y-1">
        <CardTitle className="text-2xl font-semibold tracking-tight">Reset password</CardTitle>
        <CardDescription>Choose a new password for your account.</CardDescription>
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
                <IconLabel icon={icons.signIn}>Sign in</IconLabel>
              </Link>
            </Button>
          </div>
        ) : (
          <form className="space-y-4" onSubmit={onSubmit}>
            <div className="space-y-2">
              <Label htmlFor="reset-password">New password</Label>
              <Input
                id="reset-password"
                type="password"
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="reset-confirm">Confirm password</Label>
              <Input
                id="reset-confirm"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
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
                {loading ? 'Updating…' : 'Update password'}
              </IconLabel>
            </Button>
          </form>
        )}
      </CardContent>
      <CardFooter className="flex flex-col border-t border-border/60 bg-transparent py-4">
        <p className="text-center text-sm text-muted-foreground">
          <Link to="/signin" className="font-medium text-primary underline-offset-4 hover:underline">
            Back to sign in
          </Link>
        </p>
      </CardFooter>
    </Card>
  )
}
