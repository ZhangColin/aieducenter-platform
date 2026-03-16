/**
 * refresh.ts 单元测试
 *
 * 测试 token 刷新逻辑的核心行为：
 * 1. 成功刷新 token 并更新 store
 * 2. 刷新失败时返回 false
 * 3. 并发请求时串行化刷新（只发起一次网络请求）
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { refreshAccessTokenOnce } from './refresh'
import { useAuthStore } from '@aieducenter/shared/auth-store'

// 模拟 fetch
const mockFetch = vi.fn()
global.fetch = mockFetch

// 模拟 API_URL
process.env.NEXT_PUBLIC_API_URL = 'http://test-api'

describe('refreshAccessTokenOnce', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 重置 auth store
    useAuthStore.getState().setAccessToken('old-token')
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('成功刷新 token', () => {
    it('given_valid_refresh_response_when_refresh_then_updates_store_and_returns_true', async () => {
      // Arrange
      const newToken = 'new-access-token'
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          data: { token: newToken, expireAt: '2025-12-31T23:59:59Z' }
        })
      })

      // Act
      const result = await refreshAccessTokenOnce()

      // Assert
      expect(result).toBe(true)
      expect(useAuthStore.getState().accessToken).toBe(newToken)
      expect(mockFetch).toHaveBeenCalledWith(
        'http://test-api/api/v1/auth/refresh',
        {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' }
        }
      )
    })
  })

  describe('刷新失败', () => {
    it('given_401_response_when_refresh_then_returns_false', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce({
        ok: false
      })

      // Act
      const result = await refreshAccessTokenOnce()

      // Assert
      expect(result).toBe(false)
      expect(useAuthStore.getState().accessToken).toBe('old-token')
    })

    it('given_network_error_when_refresh_then_returns_false', async () => {
      // Arrange
      mockFetch.mockRejectedValueOnce(new Error('Network error'))

      // Act
      const result = await refreshAccessTokenOnce()

      // Assert
      expect(result).toBe(false)
    })
  })

  describe('串行化刷新（防止并发）', () => {
    it('given_concurrent_requests_when_refresh_then_only_one_fetch_call', async () => {
      // Arrange
      let resolveFetch: (value: any) => void
      const fetchPromise = new Promise((resolve) => {
        resolveFetch = resolve
      })

      // 第一次调用返回一个挂起的 promise
      mockFetch.mockReturnValueOnce(fetchPromise as any)

      // Act - 同时发起多个刷新请求
      const promise1 = refreshAccessTokenOnce()
      const promise2 = refreshAccessTokenOnce()
      const promise3 = refreshAccessTokenOnce()

      // Assert - 在第一次请求完成前，只有一次 fetch 调用
      expect(mockFetch).toHaveBeenCalledTimes(1)

      // 完成第一次请求
      resolveFetch!({
        ok: true,
        json: async () => ({
          data: { token: 'new-token', expireAt: '2025-12-31T23:59:59Z' }
        })
      })

      // 等待所有请求完成
      const [result1, result2, result3] = await Promise.all([
        promise1,
        promise2,
        promise3
      ])

      // Assert - 所有请求都返回相同结果
      expect(result1).toBe(true)
      expect(result2).toBe(true)
      expect(result3).toBe(true)

      // Assert - 仍然只有一次 fetch 调用
      expect(mockFetch).toHaveBeenCalledTimes(1)
    })

    it('given_second_refresh_after_first_completes_when_refresh_then_makes_new_fetch_call', async () => {
      // Arrange
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          data: { token: 'first-token', expireAt: '2025-12-31T23:59:59Z' }
        })
      })

      // Act - 第一次刷新
      await refreshAccessTokenOnce()

      // 第二次刷新
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          data: { token: 'second-token', expireAt: '2025-12-31T23:59:59Z' }
        })
      })
      await refreshAccessTokenOnce()

      // Assert - 两次独立的 fetch 调用
      expect(mockFetch).toHaveBeenCalledTimes(2)
    })
  })
})
