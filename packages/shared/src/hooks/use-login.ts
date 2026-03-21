/**
 * 登录 Hook
 *
 * 封装登录逻辑，提供密码登录和短信验证码登录功能
 */

import { useState, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore, type AuthUser } from '@aieducenter/shared/auth-store'
import {
  loginByPassword,
  loginBySms,
  getCaptcha,
  sendSmsCode,
  type LoginByPasswordParams,
  type LoginBySmsParams,
  type CaptchaResponse,
  type SendSmsCodeResponse
} from '@aieducenter/api-client'

export interface LoginResult {
  success: boolean
  error?: string
}

// ========== Hook ==========

export function useLogin() {
  const router = useRouter()
  const { login: authLogin } = useAuthStore()

  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  /**
   * 密码登录
   */
  const loginByPasswordHandler = useCallback(async (params: LoginByPasswordParams): Promise<LoginResult> => {
    setIsLoading(true)
    setError(null)

    try {
      const response = await loginByPassword(params)

      // 登录成功，存储 token 和用户信息
      // 注意：这里需要获取用户信息，暂时使用占位符
      const user: AuthUser = {
        userId: response.token, // 暂时使用 token 作为 userId，后续需要调用 profile 接口
        nickname: '',
        avatar: null
      }

      authLogin(response.token, user)

      // 跳转到首页
      router.push('/')

      return { success: true }
    } catch (err: any) {
      const message = err.message || '登录失败，请重试'
      setError(message)
      return { success: false, error: message }
    } finally {
      setIsLoading(false)
    }
  }, [authLogin, router])

  /**
   * 短信验证码登录
   */
  const loginBySmsHandler = useCallback(async (params: LoginBySmsParams): Promise<LoginResult> => {
    setIsLoading(true)
    setError(null)

    try {
      const response = await loginBySms(params)

      // 登录成功，存储 token 和用户信息
      const user: AuthUser = {
        userId: response.token,
        nickname: '',
        avatar: null
      }

      authLogin(response.token, user)

      // 跳转到首页
      router.push('/')

      return { success: true }
    } catch (err: any) {
      const message = err.message || '登录失败，请重试'
      setError(message)
      return { success: false, error: message }
    } finally {
      setIsLoading(false)
    }
  }, [authLogin, router])

  /**
   * 获取图形验证码
   */
  const getCaptchaHandler = useCallback(async (): Promise<CaptchaResponse | null> => {
    try {
      return await getCaptcha()
    } catch (err: any) {
      setError('获取验证码失败')
      return null
    }
  }, [])

  /**
   * 发送短信验证码
   */
  const sendSmsCodeHandler = useCallback(async (
    phone: string,
    captchaId: string,
    captchaCode: string
  ): Promise<{ success: boolean; data?: SendSmsCodeResponse; error?: string }> => {
    try {
      const data = await sendSmsCode({
        phone,
        purpose: 'LOGIN',
        captchaId,
        captchaCode
      })
      return { success: true, data }
    } catch (err: any) {
      const message = err.message || '发送验证码失败'
      setError(message)
      return { success: false, error: message }
    }
  }, [])

  return {
    // 状态
    isLoading,
    error,

    // 方法
    loginByPassword: loginByPasswordHandler,
    loginBySms: loginBySmsHandler,
    getCaptcha: getCaptchaHandler,
    sendSmsCode: sendSmsCodeHandler,

    // 工具方法
    clearError: () => setError(null)
  }
}
