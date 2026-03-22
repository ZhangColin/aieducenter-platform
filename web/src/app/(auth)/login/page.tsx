'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useLogin } from '@aieducenter/shared'

type LoginType = 'account' | 'sms'

export default function LoginPage() {
  const router = useRouter()
  const {
    loginByPassword,
    loginBySms,
    getCaptcha,
    sendSmsCode,
    isLoading,
    error,
    clearError
  } = useLogin()

  // UI 状态
  const [loginType, setLoginType] = useState<LoginType>('account')
  const [showPassword, setShowPassword] = useState(false)

  // 表单数据 - 密码登录
  const [account, setAccount] = useState('')
  const [password, setPassword] = useState('')

  // 表单数据 - 短信登录
  const [phone, setPhone] = useState('')
  const [smsCode, setSmsCode] = useState('')

  // 验证码相关（短信登录用）
  const [showCaptcha, setShowCaptcha] = useState(false)
  const [captchaId, setCaptchaId] = useState('')
  const [captchaUrl, setCaptchaUrl] = useState('')
  const [captchaCode, setCaptchaCode] = useState('')
  const [captchaError, setCaptchaError] = useState('')

  // 验证码相关（密码登录用）
  const [passwordCaptchaId, setPasswordCaptchaId] = useState('')
  const [passwordCaptchaUrl, setPasswordCaptchaUrl] = useState('')
  const [passwordCaptchaCode, setPasswordCaptchaCode] = useState('')

  // 短信倒计时
  const [countdown, setCountdown] = useState(0)
  const [smsSent, setSmsSent] = useState(false)

  // 表单验证错误
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  // 初始化：获取密码登录的验证码
  useEffect(() => {
    fetchPasswordCaptcha()
  }, [])

  // 倒计时逻辑
  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => {
      setCountdown(prev => prev - 1)
    }, 1000)
    return () => clearInterval(timer)
  }, [countdown])

  // 获取图形验证码（密码登录用）
  const fetchPasswordCaptcha = async () => {
    const result = await getCaptcha()
    if (result) {
      setPasswordCaptchaId(result.captchaId)
      setPasswordCaptchaUrl(result.image)
      setPasswordCaptchaCode('')
    }
  }

  // 获取图形验证码（短信登录用）
  const fetchSmsCaptcha = async () => {
    setCaptchaError('')
    const result = await getCaptcha()
    if (result) {
      setCaptchaId(result.captchaId)
      setCaptchaUrl(result.image)
      setCaptchaCode('')
    }
  }

  // 发送短信验证码
  const handleSendSms = async () => {
    if (countdown > 0) return
    if (!phone || phone.length !== 11) {
      return
    }
    if (!showCaptcha) {
      setShowCaptcha(true)
      fetchSmsCaptcha()
      return
    }

    if (!captchaCode.trim()) {
      setCaptchaError('请输入图形验证码')
      return
    }

    const result = await sendSmsCode(phone, captchaId, captchaCode)
    if (result.success) {
      setShowCaptcha(false)
      setSmsSent(true)
      setCountdown(60)
    } else {
      fetchSmsCaptcha()
    }
  }

  // 处理登录
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    clearError()
    setFieldErrors({})

    const errors: Record<string, string> = {}

    if (loginType === 'account') {
      // 密码登录验证
      if (!account.trim()) {
        errors.account = '请输入账号'
      }
      if (!password) {
        errors.password = '请输入密码'
      }
      if (!passwordCaptchaCode.trim()) {
        errors.passwordCaptchaCode = '请输入图形验证码'
      }

      if (Object.keys(errors).length > 0) {
        setFieldErrors(errors)
        return
      }

      const result = await loginByPassword({ account, password, captchaId: passwordCaptchaId, captchaCode: passwordCaptchaCode })
      if (result.success) {
        // 登录成功，已在 hook 中处理跳转
      } else {
        // 刷新验证码
        fetchPasswordCaptcha()
      }
    } else {
      // 短信验证码登录验证
      if (!phone.trim()) {
        errors.phone = '请输入手机号'
      } else if (!/^1[3-9]\d{9}$/.test(phone)) {
        errors.phone = '请输入正确的手机号'
      }
      if (!smsCode.trim()) {
        errors.smsCode = '请输入短信验证码'
      }

      if (Object.keys(errors).length > 0) {
        setFieldErrors(errors)
        return
      }

      const result = await loginBySms({ phone, code: smsCode })
      if (result.success) {
        // 登录成功，已在 hook 中处理跳转
      } else {
        // 刷新验证码
        fetchSmsCaptcha()
      }
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-background-light dark:bg-background-dark font-display">
      <div className="flex-grow flex items-center justify-center p-4 sm:p-8">
        <div className="max-w-[1200px] w-full grid lg:grid-cols-2 bg-white dark:bg-slate-900 rounded-xl shadow-xl overflow-hidden min-h-[700px]">
          {/* Left Side: Hero/Branding */}
          <div className="hidden lg:flex flex-col justify-between p-12 bg-primary/5 relative overflow-hidden">
            <div className="relative z-10">
              <div className="flex items-center gap-3 mb-12">
                <img src="/logo-light-200.png" alt="Logo" className="h-10 w-auto" />
                <h1 className="text-2xl font-bold text-slate-900 dark:text-white">海创元智研云平台</h1>
              </div>
              <div className="space-y-6">
                <h2 className="text-4xl font-extrabold text-slate-900 dark:text-white leading-tight">
                  连接智慧 <br />
                  <span className="text-primary">驱动科研创新</span>
                </h2>
                <p className="text-lg text-slate-600 dark:text-slate-400 max-w-md">
                  面向教育与企业的统一 AI 智研入口，集成先进算法与海量算力，助力您的研究更进一步。
                </p>
              </div>
            </div>
            <div className="relative z-10 mt-auto">
              <div className="flex items-center gap-4 p-4 bg-white/80 dark:bg-slate-800/80 backdrop-blur rounded-lg border border-primary/10">
                <div className="size-12 rounded-full bg-primary/20 flex items-center justify-center">
                  <span className="material-symbols-outlined text-primary">verified_user</span>
                </div>
                <div>
                  <p className="text-sm font-semibold text-slate-900 dark:text-white">企业级安全保障</p>
                  <p className="text-xs text-slate-500">端到端加密与数据隔离技术</p>
                </div>
              </div>
            </div>
            <div className="absolute -bottom-20 -left-20 size-80 bg-primary/10 rounded-full blur-3xl"></div>
            <div className="absolute -top-20 -right-20 size-96 bg-primary/5 rounded-full blur-3xl"></div>
          </div>

          {/* Right Side: Login Form */}
          <div className="flex flex-col justify-center p-8 sm:p-16">
            <div className="w-full max-w-md mx-auto">
              {/* Mobile Logo */}
              <div className="lg:hidden flex items-center gap-2 mb-8">
                <img src="/logo-light-200.png" alt="Logo" className="h-8 w-auto" />
                <h2 className="text-xl font-bold text-slate-900 dark:text-white">海创元智研</h2>
              </div>

              <div className="mb-8">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">欢迎回来</h3>
                <p className="text-slate-500 dark:text-slate-400">请选择登录方式进入您的智研空间</p>
              </div>

              {/* Tabs */}
              <div className="flex border-b border-slate-200 dark:border-slate-700 mb-8">
                <button
                  onClick={() => { setLoginType('account'); clearError(); setFieldErrors({}) }}
                  className={`px-6 py-3 text-sm font-bold border-b-2 transition-colors ${
                    loginType === 'account'
                      ? 'text-primary border-primary'
                      : 'text-slate-500 border-transparent hover:text-slate-700 dark:hover:text-slate-300'
                  }`}
                >
                  账号登录
                </button>
                <button
                  onClick={() => { setLoginType('sms'); clearError() }}
                  className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
                    loginType === 'sms'
                      ? 'text-primary border-primary'
                      : 'text-slate-500 border-transparent hover:text-slate-700 dark:hover:text-slate-300'
                  }`}
                >
                  手机验证码
                </button>
              </div>

              {/* Error Message */}
              {error && (
                <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                  <p className="text-sm text-red-600 dark:text-red-400 flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm">error</span>
                    {error}
                  </p>
                </div>
              )}

              <form onSubmit={handleLogin} className="space-y-5">
                {loginType === 'account' ? (
                  <>
                    {/* 账号输入 */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        账号
                      </label>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          person
                        </span>
                        <input
                          type="text"
                          placeholder="用户名/邮箱/手机号"
                          value={account}
                          onChange={(e) => { setAccount(e.target.value); setFieldErrors(prev => ({ ...prev, account: '' })) }}
                          className={`w-full pl-10 pr-4 py-3 rounded-lg border bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${fieldErrors.account ? 'border-red-500' : 'border-slate-200 dark:border-slate-700'}`}
                        />
                      </div>
                      {fieldErrors.account && <p className="text-xs text-red-500">{fieldErrors.account}</p>}
                    </div>

                    {/* 密码输入 */}
                    <div className="space-y-2">
                      <div className="flex justify-between">
                        <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                          登录密码
                        </label>
                      </div>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          lock
                        </span>
                        <input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="请输入密码"
                          value={password}
                          onChange={(e) => { setPassword(e.target.value); setFieldErrors(prev => ({ ...prev, password: '' })) }}
                          className={`w-full pl-10 pr-12 py-3 rounded-lg border bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${fieldErrors.password ? 'border-red-500' : 'border-slate-200 dark:border-slate-700'}`}
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400"
                        >
                          <span className="material-symbols-outlined text-xl">
                            {showPassword ? 'visibility' : 'visibility_off'}
                          </span>
                        </button>
                      </div>
                      {fieldErrors.password && <p className="text-xs text-red-500">{fieldErrors.password}</p>}
                    </div>

                    {/* 图形验证码 - 密码登录 */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        图形验证码
                      </label>
                      <div className="flex gap-3">
                        <input
                          type="text"
                          placeholder="请输入验证码"
                          value={passwordCaptchaCode}
                          onChange={(e) => { setPasswordCaptchaCode(e.target.value); setFieldErrors(prev => ({ ...prev, passwordCaptchaCode: '' })) }}
                          className={`flex-1 px-4 py-3 rounded-lg border bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${fieldErrors.passwordCaptchaCode ? 'border-red-500' : 'border-slate-200 dark:border-slate-700'}`}
                        />
                        <div
                          className="w-40 h-12 rounded-lg border border-slate-200 dark:border-slate-700 overflow-hidden cursor-pointer hover:opacity-80 transition-opacity flex items-center justify-center bg-white dark:bg-slate-800 flex-shrink-0"
                          onClick={fetchPasswordCaptcha}
                        >
                          {passwordCaptchaUrl ? (
                            <img
                              src={passwordCaptchaUrl}
                              alt="验证码"
                              className="max-w-full max-h-full object-contain"
                            />
                          ) : (
                            <div className="w-full h-full bg-slate-200 dark:bg-slate-700 animate-pulse" />
                          )}
                        </div>
                      </div>
                      {fieldErrors.passwordCaptchaCode && <p className="text-xs text-red-500">{fieldErrors.passwordCaptchaCode}</p>}
                      <p className="text-xs text-slate-500">点击图片刷新验证码</p>
                    </div>
                  </>
                ) : (
                  <>
                    {/* 手机号输入 */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        手机号
                      </label>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          smartphone
                        </span>
                        <input
                          type="tel"
                          placeholder="请输入手机号"
                          value={phone}
                          onChange={(e) => { setPhone(e.target.value); setFieldErrors(prev => ({ ...prev, phone: '' })) }}
                          maxLength={11}
                          className={`w-full pl-10 pr-4 py-3 rounded-lg border bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${fieldErrors.phone ? 'border-red-500' : 'border-slate-200 dark:border-slate-700'}`}
                        />
                      </div>
                      {fieldErrors.phone && <p className="text-xs text-red-500">{fieldErrors.phone}</p>}
                    </div>

                    {/* 短信验证码输入（带内联图形验证码） */}
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        短信验证码
                      </label>
                      <div className="flex gap-3">
                        <div className="relative flex-1">
                          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                            verified
                          </span>
                          <input
                            type="text"
                            placeholder="请输入验证码"
                            value={smsCode}
                            onChange={(e) => { setSmsCode(e.target.value); setFieldErrors(prev => ({ ...prev, smsCode: '' })) }}
                            maxLength={6}
                            className={`w-full pl-10 pr-4 py-3 rounded-lg border bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${fieldErrors.smsCode ? 'border-red-500' : 'border-slate-200 dark:border-slate-700'}`}
                          />
                        </div>
                        <button
                          type="button"
                          onClick={handleSendSms}
                          disabled={countdown > 0 || !phone || phone.length !== 11}
                          className="px-4 py-3 whitespace-nowrap rounded-lg border border-primary text-primary font-medium hover:bg-primary/5 transition-colors disabled:opacity-50 disabled:cursor-not-allowed min-w-[100px]"
                        >
                          {countdown > 0 ? `${countdown}s` : smsSent ? '重新获取' : '获取验证码'}
                        </button>
                      </div>
                      {fieldErrors.smsCode && <p className="text-xs text-red-500">{fieldErrors.smsCode}</p>}

                      {/* 内联图形验证码区域 */}
                      {showCaptcha && (
                        <div className="mt-3 p-4 bg-slate-50 dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 animate-fade-in">
                          <p className="text-xs text-slate-500 mb-2">请输入图形验证码后发送短信验证码</p>
                          <div className="flex gap-3 items-start">
                            <div
                              className="w-40 h-12 rounded border border-slate-200 dark:border-slate-700 overflow-hidden cursor-pointer hover:opacity-80 transition-opacity flex items-center justify-center bg-white dark:bg-slate-800 flex-shrink-0"
                              onClick={fetchSmsCaptcha}
                            >
                              {captchaUrl ? (
                                <img
                                  src={captchaUrl}
                                  alt="验证码"
                                  className="max-w-full max-h-full object-contain"
                                />
                              ) : (
                                <div className="w-full h-full bg-slate-200 dark:bg-slate-700 animate-pulse" />
                              )}
                            </div>
                            <div className="flex-1 space-y-2">
                              <input
                                type="text"
                                placeholder="图形验证码"
                                className="w-full px-3 py-2 text-sm border rounded-lg dark:bg-slate-700 border-slate-200 dark:border-slate-700 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none"
                                value={captchaCode}
                                onChange={(e) => setCaptchaCode(e.target.value)}
                              />
                              {captchaError && <p className="text-xs text-red-500">{captchaError}</p>}
                              <button
                                type="button"
                                onClick={handleSendSms}
                                className="w-full py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary/90 transition-colors"
                              >
                                发送验证码
                              </button>
                            </div>
                          </div>
                        </div>
                      )}

                      {/* 成功提示 */}
                      {smsSent && countdown === 0 && (
                        <p className="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
                          <span className="material-symbols-outlined text-sm">check_circle</span>
                          验证码已发送，固定为 123456
                        </p>
                      )}
                    </div>
                  </>
                )}

                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-full py-3 bg-primary hover:bg-primary/90 text-white font-bold rounded-lg transition-colors shadow-lg shadow-primary/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  {isLoading ? (
                    <>
                      <span className="material-symbols-outlined animate-spin">refresh</span>
                      登录中...
                    </>
                  ) : (
                    '立即登录'
                  )}
                </button>
              </form>

              <div className="mt-8">
                <div className="relative">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-slate-200 dark:border-slate-700"></div>
                  </div>
                  <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-white dark:bg-slate-900 px-2 text-slate-500">其他方式登录</span>
                  </div>
                </div>

                <div className="mt-6 grid grid-cols-2 gap-4">
                  <button type="button" className="flex items-center justify-center gap-2 py-2 px-4 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                    <div className="size-5 rounded-full bg-[#07C160] flex items-center justify-center text-white">
                      <span className="material-symbols-outlined text-[14px]">chat</span>
                    </div>
                    <span className="text-sm text-slate-600 dark:text-slate-300">微信登录</span>
                  </button>
                  <button type="button" className="flex items-center justify-center gap-2 py-2 px-4 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                    <div className="size-5 rounded-full bg-[#0089FF] flex items-center justify-center text-white">
                      <span className="material-symbols-outlined text-[14px]">business</span>
                    </div>
                    <span className="text-sm text-slate-600 dark:text-slate-300">钉钉登录</span>
                  </button>
                </div>
              </div>

              <p className="mt-8 text-center text-sm text-slate-500">
                还没有账号？{' '}
                <a className="text-primary font-semibold hover:underline cursor-pointer" onClick={() => router.push('/register')}>
                  立即注册
                </a>
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="p-6 text-center">
        <div className="flex flex-wrap justify-center gap-6 text-sm text-slate-500 mb-4">
          <a className="hover:text-primary transition-colors" href="#">
            关于我们
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            服务协议
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            隐私政策
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            联系支持
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            官方博客
          </a>
        </div>
        <p className="text-xs text-slate-400">
          © 2024 海创元 (Hai Chuang Yuan). All rights reserved. 京ICP备XXXXXXXX号
        </p>
      </footer>
    </div>
  )
}
