import { type ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.tsx'

export function RequireAuth({ children }: { children: ReactNode }) {
  const { token } = useAuth()
  const location = useLocation()
  if (!token) {
    return <Navigate to="/signin" replace state={{ from: location.pathname }} />
  }
  return children
}
