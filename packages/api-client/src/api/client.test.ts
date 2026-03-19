/**
 * client.ts 单元测试
 *
 * 测试 API 客户端的核心行为：
 * 1. 401 响应时触发 logout 并重定向到登录页
 * 2. 有 token 时注入 Authorization header
 * 3. 无 token 时不注入 Authorization header
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { __middlewares__ } from './client'
import { useAuthStore } from '@aieducenter/shared/auth-store'

// 中间件类型
interface AuthMiddleware {
  onRequest: (args: { request: Request }) => Promise<Request>
}

interface AuthErrorMiddleware {
  onResponse: (args: { response: Response }) => Promise<Response>
}

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 重置 auth store
    useAuthStore.setState({ token: null, user: null, isAuthenticated: false })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('given_401_response_when_api_call_then_logout_called_and_redirect_to_login', async () => {
    // Arrange
    const logoutSpy = vi.spyOn(useAuthStore.getState(), 'logout').mockImplementation(() => {})
    const locationSpy = vi.spyOn(window.location, 'href', 'set').mockImplementation(() => {})
    const authErrorMiddleware = __middlewares__[1] as AuthErrorMiddleware

    const response = new Response(null, { status: 401 })

    // Act
    await authErrorMiddleware.onResponse({ response })

    // Assert
    expect(logoutSpy).toHaveBeenCalled()
    expect(locationSpy).toHaveBeenCalledWith('/login?reason=session_expired')
  })

  it('given_valid_token_when_api_call_then_authorization_header_set', async () => {
    // Arrange
    const token = 'test-access-token'
    useAuthStore.setState({ token, user: null, isAuthenticated: false })
    const authMiddleware = __middlewares__[0] as AuthMiddleware

    // Act
    const request = new Request('http://test-api/api/v1/test')
    const modifiedRequest = await authMiddleware.onRequest({ request })

    // Assert
    expect(modifiedRequest.headers.get('Authorization')).toBe(`Bearer ${token}`)
  })

  it('given_no_token_when_api_call_then_no_authorization_header', async () => {
    // Arrange
    useAuthStore.setState({ token: null, user: null, isAuthenticated: false })
    const authMiddleware = __middlewares__[0] as AuthMiddleware

    // Act
    const request = new Request('http://test-api/api/v1/test')
    const modifiedRequest = await authMiddleware.onRequest({ request })

    // Assert
    expect(modifiedRequest.headers.get('Authorization')).toBeNull()
  })
})
