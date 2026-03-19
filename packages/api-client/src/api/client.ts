/**
 * API 客户端
 *
 * 基于 openapi-fetch，提供 token 注入和错误处理功能
 */
import createClient from 'openapi-fetch'
import type { paths } from './schema'
import { useAuthStore } from '@aieducenter/shared/auth-store'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

// 中间件 1: 注入 access token
const authMiddleware = {
  async onRequest({ request }: { request: Request }) {
    const token = useAuthStore.getState().token
    if (token) {
      request.headers.set('Authorization', `Bearer ${token}`)
    }
    return request
  },
}

// 中间件 2: 处理认证错误，401 时清空会话并重定向
const authErrorMiddleware = {
  async onResponse({ response }: { response: Response }) {
    if (response.status === 401) {
      if (typeof window !== 'undefined') {
        useAuthStore.getState().logout()
        window.location.href = '/login?reason=session_expired'
      }
    }
    return response
  },
}

// 导出中间件供测试使用
export const __middlewares__ = [authMiddleware, authErrorMiddleware]

// 创建基础客户端
export const api = createClient<paths>({
  baseUrl: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

api.use(authMiddleware)
api.use(authErrorMiddleware)
