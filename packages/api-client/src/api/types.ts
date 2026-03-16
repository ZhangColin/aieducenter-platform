/**
 * 手动补充的 API 类型定义
 *
 * 这些类型用于配合 openapi-typescript 生成的类型使用
 */

/** 后端统一响应格式（cartisan-web ApiResponse<T>） */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId: string
  errors?: FieldError[]
}

/** 字段级错误 */
export interface FieldError {
  field: string
  message: string
  errorCode?: string
}

/** 分页响应 */
export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  size: number
}
