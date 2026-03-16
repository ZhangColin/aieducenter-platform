/**
 * 认证状态管理
 *
 * 临时占位实现，将在 Epic 02 完整实现
 */
import { create } from 'zustand'

export interface AuthState {
  accessToken: string | null
  setAccessToken: (token: string | null) => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  setAccessToken: (token) => set({ accessToken: token }),
}))
