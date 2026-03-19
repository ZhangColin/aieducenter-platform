'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@aieducenter/shared/auth-store'

interface AuthGuardProps {
  children: React.ReactNode
}

export function AuthGuard({ children }: AuthGuardProps) {
  const router = useRouter()
  const { token, isAuthenticated } = useAuthStore()
  const [hydrated, setHydrated] = useState(useAuthStore.persist.hasHydrated())
  const [ready, setReady] = useState(false)

  // Wait for persist hydration
  useEffect(() => {
    if (!hydrated) {
      const unsubscribe = useAuthStore.persist.onFinishHydration(() => {
        setHydrated(true)
      })
      return unsubscribe
    }
  }, [hydrated])

  // Auth logic after hydration
  useEffect(() => {
    if (!hydrated) return

    if (!token) {
      router.replace('/login')
      return
    }

    if (isAuthenticated) {
      setReady(true)
      return
    }

    // token exists but not authenticated - fetch profile
    let cancelled = false

    const fetchProfile = async () => {
      try {
        const response = await fetch('/api/account/profile', {
          headers: { Authorization: `Bearer ${token}` },
        })

        if (response.status === 401) {
          // authErrorMiddleware already handles logout + redirect
          return
        }

        if (!response.ok) {
          throw new Error(`Unexpected response: ${response.status}`)
        }

        const data = await response.json()
        if (cancelled) return
        const { login } = useAuthStore.getState()
        login(token, {
          userId: data.data.userId,
          nickname: data.data.nickname,
          avatar: data.data.avatar,
        })
        setReady(true)
      } catch {
        if (cancelled) return
        const { logout } = useAuthStore.getState()
        logout()
        router.replace('/login')
      }
    }

    fetchProfile()
    return () => {
      cancelled = true
    }
  }, [hydrated, token, isAuthenticated, router])

  if (!hydrated || !ready) {
    return <div data-testid="auth-loading" />
  }

  return <>{children}</>
}
