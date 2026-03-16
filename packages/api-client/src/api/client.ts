/**
 * API 客户端
 *
 * 基于 openapi-fetch，提供 token 注入和自动刷新功能
 */
import createClient from 'openapi-fetch'
import type { paths } from './schema'
import { useAuthStore } from '@aieducenter/shared/auth-store'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
const REFRESH_PATH = '/api/v1/auth/refresh'

// 中间件 1: 注入 access token
const authMiddleware = {
  async onRequest({ request }: { request: Request }) {
    const token = useAuthStore.getState().accessToken
    if (token) {
      request.headers.set('Authorization', `Bearer ${token}`)
    }
    return request
  },
}

// 中间件 2: 统一响应处理，401 时刷新 token
const refreshMiddleware = {
  async onResponse({ response }: { response: Response }) {
    if (response.status === 401 && !response.url.includes(REFRESH_PATH)) {
      const { refreshAccessTokenOnce } = await import('../auth/refresh')
      const refreshed = await refreshAccessTokenOnce()
      if (!refreshed && typeof window !== 'undefined') {
        window.location.href = '/login?reason=token_expired'
      }
    }
    return response
  },
}

// 导出中间件供测试使用
export const __middlewares__ = [authMiddleware, refreshMiddleware]

// 创建基础客户端
export const api = createClient<paths>({
  baseUrl: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

api.use(authMiddleware)
api.use(refreshMiddleware)
