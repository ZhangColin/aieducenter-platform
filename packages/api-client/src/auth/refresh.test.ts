/**
 * refresh.ts 单元测试
 *
 * 测试 token 刷新逻辑的核心行为（stub 实现）：
 * 1. 存根实现始终返回 false
 * 2. 并发请求时串行化刷新（共享相同的 promise）
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { refreshAccessTokenOnce, __resetRefreshPromise__ } from './refresh'

describe('refreshAccessTokenOnce', () => {
  beforeEach(() => {
    __resetRefreshPromise__()
  })

  it('given_stub_implementation_when_called_then_returns_false', async () => {
    // Act
    const result = await refreshAccessTokenOnce()

    // Assert
    expect(result).toBe(false)
  })

  it('given_concurrent_calls_when_refreshing_then_shares_same_promise', async () => {
    // Act - 同时发起多个刷新请求
    const promise1 = refreshAccessTokenOnce()
    const promise2 = refreshAccessTokenOnce()
    const promise3 = refreshAccessTokenOnce()

    // Assert - 所有 promise 应该是同一个对象（序列化）
    expect(promise1).toBe(promise2)
    expect(promise2).toBe(promise3)

    // 等待所有请求完成
    const [result1, result2, result3] = await Promise.all([
      promise1,
      promise2,
      promise3
    ])

    // Assert - 所有请求都返回 false
    expect(result1).toBe(false)
    expect(result2).toBe(false)
    expect(result3).toBe(false)
  })
})
