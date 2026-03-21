/**
 * 登录 API 客户端
 *
 * 提供密码登录、短信验证码登录和图形验证码获取功能
 *
 * <p>使用相对路径，通过 Next.js rewrites 代理到后端。
 * 开发环境由 Next.js 代理，生产环境由 nginx 代理。</p>
 */

// ========== 类型定义 ==========

export interface LoginByPasswordParams {
  account: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface LoginBySmsParams {
  phone: string
  code: string
  captchaId: string
  captchaCode: string
}

export interface LoginResponse {
  token: string
}

export interface CaptchaResponse {
  captchaId: string
  image: string
}

export interface SendSmsCodeParams {
  phone: string
  purpose: 'LOGIN' | 'REGISTER' | 'RESET_PASSWORD'
  captchaId: string
  captchaCode: string
}

export interface SendSmsCodeResponse {
  expireSeconds: number
  cooldownSeconds: number
}

// 后端统一响应格式
interface ApiResponse<T> {
  code: number
  message: string
  data: T
  errors?: any
}

// ========== API 函数 ==========

/**
 * 密码登录
 */
export async function loginByPassword(params: LoginByPasswordParams): Promise<LoginResponse> {
  const response = await fetch(`/api/account/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params)
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '登录失败' }))
    throw error
  }

  const result: ApiResponse<LoginResponse> = await response.json()
  return result.data
}

/**
 * 短信验证码登录
 */
export async function loginBySms(params: LoginBySmsParams): Promise<LoginResponse> {
  const response = await fetch(`/api/account/login/sms`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params)
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '登录失败' }))
    throw error
  }

  const result: ApiResponse<LoginResponse> = await response.json()
  return result.data
}

/**
 * 获取图形验证码
 */
export async function getCaptcha(): Promise<CaptchaResponse> {
  const response = await fetch(`/api/captcha`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '获取验证码失败' }))
    throw error
  }

  const result: ApiResponse<CaptchaResponse> = await response.json()
  return result.data
}

/**
 * 发送短信验证码
 */
export async function sendSmsCode(params: SendSmsCodeParams): Promise<SendSmsCodeResponse> {
  const response = await fetch(`/api/account/verification-code/sms`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params)
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '发送验证码失败' }))
    throw error
  }

  const result: ApiResponse<SendSmsCodeResponse> = await response.json()
  return result.data
}

/**
 * 退出登录
 */
export async function logout(): Promise<void> {
  const response = await fetch(`/api/account/logout`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // Sa-Token 需要从 cookie 中获取 token，浏览器会自动发送
    }
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '退出登录失败' }))
    throw error
  }
}
