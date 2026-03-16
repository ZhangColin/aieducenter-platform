/**
 * API 客户端统一导出入口
 */

export { api } from './api/client'
export type { paths, components, operations } from './api/schema'

// 便捷类型导出
export type ApiError = {
  error?: {
    status: number
    message: string
  }
}

// 重新导出手动类型
export type { ApiResponse, FieldError, PageResponse } from './api/types'
