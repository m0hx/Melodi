import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { getJson, patchJson, putJson } from '../api/client.ts'
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

type Profile = {
  fullName: string
  email: string
  phone?: string | null
  address?: string | null
  role?: { name: string }
}

type MessageResponse = { message: string }

export function ProfilePage() {
  const { token } = useAuth()
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [address, setAddress] = useState('')
  const [role, setRole] = useState('')

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [changingPw, setChangingPw] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [profileMsg, setProfileMsg] = useState<string | null>(null)
  const [pwMsg, setPwMsg] = useState<string | null>(null)

  const loadProfile = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError(null)
    try {
      const data = await getJson<Profile>('/api/profile', { token })
      setFullName(data.fullName ?? '')
      setEmail(data.email ?? '')
      setPhone(data.phone ?? '')
      setAddress(data.address ?? '')
      setRole(data.role?.name ?? '')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load profile')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    void loadProfile()
  }, [loadProfile])

  async function onSaveProfile(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setSaving(true)
    setProfileMsg(null)
    setError(null)
    try {
      await patchJson<Profile>(
        '/api/profile',
        { fullName, phone, address },
        { token },
      )
      setProfileMsg('Profile updated.')
      await loadProfile()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Update failed')
    } finally {
      setSaving(false)
    }
  }

  async function onChangePassword(e: FormEvent) {
    e.preventDefault()
    if (!token) return
    setChangingPw(true)
    setPwMsg(null)
    setError(null)
    try {
      const res = await putJson<MessageResponse>(
        '/auth/users/change-password',
        { currentPassword, newPassword },
        { token },
      )
      setPwMsg(res.message)
      setCurrentPassword('')
      setNewPassword('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Password change failed')
    } finally {
      setChangingPw(false)
    }
  }

  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading profile…</p>
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Profile</h1>
        <p className="text-sm text-muted-foreground">
          {email}
          {role ? ` · ${role}` : ''}
        </p>
      </div>

      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : null}

      <Card className="ui-surface">
        <CardHeader>
          <CardTitle className="text-base">Your details</CardTitle>
          <CardDescription>Update name, phone, and address.</CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={onSaveProfile}>
            <div className="space-y-2">
              <Label htmlFor="profile-name">Full name</Label>
              <Input
                id="profile-name"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="profile-phone">Phone</Label>
              <Input
                id="profile-phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="profile-address">Address</Label>
              <Input
                id="profile-address"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />
            </div>
            {profileMsg ? (
              <p className="text-sm text-foreground" role="status">
                {profileMsg}
              </p>
            ) : null}
            <Button type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save profile'}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card className="ui-surface">
        <CardHeader>
          <CardTitle className="text-base">Change password</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={onChangePassword}>
            <div className="space-y-2">
              <Label htmlFor="current-password">Current password</Label>
              <Input
                id="current-password"
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="new-password">New password</Label>
              <Input
                id="new-password"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
            </div>
            {pwMsg ? (
              <p className="text-sm text-foreground" role="status">
                {pwMsg}
              </p>
            ) : null}
            <Button type="submit" disabled={changingPw}>
              {changingPw ? 'Updating…' : 'Change password'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
