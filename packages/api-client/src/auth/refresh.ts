/**
 * Token 刷新逻辑
 *
 * 提供串行化的 token 刷新功能，防止并发 401 时多次刷新
 */
import { useAuthStore } from '@aieducenter/shared/auth-store'

/** 刷新端点路径，导出供 client.ts 使用 */
export const REFRESH_ENDPOINT = '/api/v1/auth/refresh'

let refreshPromise: Promise<boolean> | null = null

/** 后端刷新接口响应格式 */
interface RefreshResponse {
  data: {
    token: string
    expireAt: string
  }
}

/**
 * 刷新 access token（串行化，防止并发 401 时多次刷新）
 *
 * @returns 刷新是否成功
 */
export async function refreshAccessTokenOnce(): Promise<boolean> {
  // 如果已有刷新进行中，返回相同的 promise
  if (refreshPromise) {
    return refreshPromise
  }

  const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

  refreshPromise = (async () => {
    try {
      const response = await fetch(`${API_URL}${REFRESH_ENDPOINT}`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
      })

      if (!response.ok) return false

      const data = (await response.json()) as RefreshResponse
      useAuthStore.getState().setAccessToken(data.data.token)
      return true
    } catch (error) {
      // 记录错误便于调试，同时保持返回值语义
      console.error('[refreshAccessTokenOnce] Token refresh failed:', error)
      return false
    } finally {
      // 刷新完成后清空，允许下次刷新
      refreshPromise = null
    }
  })()

  return refreshPromise
}
