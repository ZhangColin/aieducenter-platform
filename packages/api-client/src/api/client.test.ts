/**
 * client.ts 单元测试
 *
 * 测试 API 客户端的核心行为：
 * 1. 注入 access token 到请求头
 * 2. 401 响应时触发 token 刷新
 * 3. 刷新成功后重定向到登录（可选）
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { __middlewares__ } from './client'
import { useAuthStore } from '@aieducenter/shared/auth-store'

// 中间件类型
interface AuthMiddleware {
  onRequest: (args: { request: Request }) => Promise<Request>
}

interface RefreshMiddleware {
  onResponse: (args: { response: Response }) => Promise<Response>
}

// 创建 mock 函数
const mockRefreshAccessTokenOnce = vi.fn()

// 模拟 refresh 模块
vi.mock('../auth/refresh', () => ({
  refreshAccessTokenOnce: () => mockRefreshAccessTokenOnce(),
  REFRESH_ENDPOINT: '/api/v1/auth/refresh'
}))

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 重置 auth store - 使用 clearAccessToken
    useAuthStore.getState().clearAccessToken()
    // 重置 refresh mock
    mockRefreshAccessTokenOnce.mockReset()
  })

  describe('Token 注入', () => {
    it('given_access_token_exists_when_making_request_then_includes_authorization_header', async () => {
      // Arrange
      const token = 'test-access-token'
      useAuthStore.getState().setAccessToken(token)
      const authMiddleware = __middlewares__[0] as AuthMiddleware

      // Act
      const request = new Request('http://test-api/api/v1/test')
      const modifiedRequest = await authMiddleware.onRequest({ request })

      // Assert
      expect(modifiedRequest.headers.get('Authorization')).toBe(`Bearer ${token}`)
    })

    it('given_no_access_token_when_making_request_then_no_authorization_header', async () => {
      // Arrange
      useAuthStore.getState().clearAccessToken()
      const authMiddleware = __middlewares__[0] as AuthMiddleware

      // Act
      const request = new Request('http://test-api/api/v1/test')
      const modifiedRequest = await authMiddleware.onRequest({ request })

      // Assert
      expect(modifiedRequest.headers.get('Authorization')).toBeNull()
    })
  })

  describe('401 响应处理', () => {
    it('given_401_response_when_not_refresh_endpoint_then_attempts_refresh', async () => {
      // Arrange
      mockRefreshAccessTokenOnce.mockResolvedValueOnce(true)
      const refreshMiddleware = __middlewares__[1] as RefreshMiddleware

      const response = new Response(null, { status: 401 })
      Object.defineProperty(response, 'url', {
        value: 'http://test-api/api/v1/some-endpoint',
        writable: false
      })

      // Act
      await refreshMiddleware.onResponse({ response })

      // Assert
      expect(mockRefreshAccessTokenOnce).toHaveBeenCalled()
    })

    it('given_401_response_when_refresh_endpoint_then_skips_refresh', async () => {
      // Arrange
      mockRefreshAccessTokenOnce.mockResolvedValueOnce(true)
      const refreshMiddleware = __middlewares__[1] as RefreshMiddleware

      const response = new Response(null, { status: 401 })
      Object.defineProperty(response, 'url', {
        value: 'http://test-api/api/v1/auth/refresh',
        writable: false
      })

      // Act
      await refreshMiddleware.onResponse({ response })

      // Assert
      expect(mockRefreshAccessTokenOnce).not.toHaveBeenCalled()
    })

    it('given_401_response_and_refresh_fails_when_in_browser_then_redirects_to_login', async () => {
      // Arrange
      mockRefreshAccessTokenOnce.mockResolvedValueOnce(false)
      const refreshMiddleware = __middlewares__[1] as RefreshMiddleware

      const response = new Response(null, { status: 401 })
      Object.defineProperty(response, 'url', {
        value: 'http://test-api/api/v1/some-endpoint',
        writable: false
      })

      // 模拟 location.href setter
      const locationSpy = vi.spyOn(window.location, 'href', 'set').mockImplementation(() => {})

      // Act
      await refreshMiddleware.onResponse({ response })

      // Assert
      expect(locationSpy).toHaveBeenCalledWith('/login?reason=token_expired')

      locationSpy.mockRestore()
    })

    it('given_401_response_and_refresh_fails_when_not_in_browser_then_no_redirect', async () => {
      // Arrange
      mockRefreshAccessTokenOnce.mockResolvedValueOnce(false)
      const refreshMiddleware = __middlewares__[1] as RefreshMiddleware

      const response = new Response(null, { status: 401 })
      Object.defineProperty(response, 'url', {
        value: 'http://test-api/api/v1/some-endpoint',
        writable: false
      })

      const locationSpy = vi.spyOn(window.location, 'href', 'set').mockImplementation(() => {})

      // 模拟非浏览器环境
      const originalWindow = global.window
      // @ts-ignore
      delete global.window

      // Act
      await refreshMiddleware.onResponse({ response })

      // Assert - location.href 不应被调用
      expect(locationSpy).not.toHaveBeenCalled()

      // 恢复
      locationSpy.mockRestore()
      global.window = originalWindow
    })
  })
})
