import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface AuthUser {
  userId: string  // Long ID 转字符串，防止 JS Number 精度丢失
  nickname: string
  avatar: string | null
}

export interface AuthState {
  token: string | null
  user: AuthUser | null
  isAuthenticated: boolean
  login: (token: string, user: AuthUser) => void
  logout: () => void
  setUser: (user: AuthUser) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      login: (token, user) => set({ token, user, isAuthenticated: true }),
      logout: () => set({ token: null, user: null, isAuthenticated: false }),
      setUser: (user) => set({ user }),
    }),
    {
      name: 'aieducenter-auth',
      partialize: (state) => ({ token: state.token }),  // 只持久化 token
    }
  )
)
