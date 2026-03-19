/**
 * Token 刷新逻辑
 *
 * 提供串行化的 token 刷新功能，防止并发 401 时多次刷新
 */
// TODO: F02-14 实现时，用 authStore.login(newToken, currentUser) 替换此处逻辑

/** 刷新端点路径，导出供后续使用 */
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
export function refreshAccessTokenOnce(): Promise<boolean> {
  // 如果已有刷新进行中，返回相同的 promise
  if (refreshPromise) {
    return refreshPromise
  }

  refreshPromise = (async () => {
    // TODO: F02-14 实现时，用 authStore.login(newToken, currentUser) 替换此处逻辑
    return false
  })()

  return refreshPromise
}

/**
 * 重置刷新状态（仅用于测试）
 */
export function __resetRefreshPromise__(): void {
  refreshPromise = null
}
