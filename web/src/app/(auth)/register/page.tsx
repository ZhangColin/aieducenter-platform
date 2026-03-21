'use client'

import { useState, useEffect } from 'react'

type UsernameStatus = 'checking' | 'available' | 'taken' | undefined
type PhoneStatus = 'checking' | 'available' | 'taken' | undefined

export default function RegisterPage() {
  // Password visibility states
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  // Form states
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [countdown, setCountdown] = useState(0)

  // Form data
  const [formData, setFormData] = useState({
    username: '',
    phone: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    verificationCode: '',
  })

  // Availability check states
  const [usernameStatus, setUsernameStatus] = useState<UsernameStatus>()
  const [phoneStatus, setPhoneStatus] = useState<PhoneStatus>()

  // Captcha modal states
  const [showCaptchaModal, setShowCaptchaModal] = useState(false)
  const [captchaUrl, setCaptchaUrl] = useState('')
  const [captchaId, setCaptchaId] = useState('')
  const [captchaCode, setCaptchaCode] = useState('')
  const [captchaError, setCaptchaError] = useState('')

  // Username validation and availability check
  useEffect(() => {
    const timeoutId = setTimeout(async () => {
      if (formData.username) {
        await checkUsername(formData.username)
      } else {
        setUsernameStatus(undefined)
      }
    }, 500)

    return () => clearTimeout(timeoutId)
  }, [formData.username])

  const checkUsername = async (username: string) => {
    // Basic validation: 3-20 chars, letter start
    const usernameRegex = /^[a-zA-Z][a-zA-Z0-9_]{2,19}$/
    if (!username || username.length < 3) {
      setUsernameStatus(undefined)
      return
    }
    if (!usernameRegex.test(username)) {
      setUsernameStatus('taken')
      return
    }

    setUsernameStatus('checking')
    try {
      const res = await fetch(
        `/api/account/check-username?username=${encodeURIComponent(username)}`
      )
      const data = await res.json()
      setUsernameStatus(data.data ? 'available' : 'taken')
    } catch {
      setUsernameStatus(undefined)
    }
  }

  // Phone validation and availability check
  useEffect(() => {
    const timeoutId = setTimeout(async () => {
      if (formData.phone) {
        await checkPhone(formData.phone)
      } else {
        setPhoneStatus(undefined)
      }
    }, 500)

    return () => clearTimeout(timeoutId)
  }, [formData.phone])

  const checkPhone = async (phone: string) => {
    // Basic validation: 11 digits, starts with 1
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phone || phone.length < 11) {
      setPhoneStatus(undefined)
      return
    }
    if (!phoneRegex.test(phone)) {
      setPhoneStatus('taken')
      return
    }

    setPhoneStatus('checking')
    try {
      const res = await fetch(`/api/account/check-phone?phone=${phone}`)
      const data = await res.json()
      setPhoneStatus(data.data ? 'available' : 'taken')
    } catch {
      setPhoneStatus(undefined)
    }
  }

  // Get captcha image
  const getCaptcha = async () => {
    setCaptchaError('')
    try {
      const res = await fetch('/api/captcha')
      const data = await res.json()
      setCaptchaUrl(data.data.image)
      setCaptchaId(data.data.captchaId)
    } catch {
      setCaptchaError('获取验证码失败，请重试')
    }
  }

  // Show captcha modal before sending SMS
  const sendSmsCode = () => {
    if (countdown > 0) return
    if (!formData.phone || phoneStatus !== 'available') {
      return
    }
    setShowCaptchaModal(true)
    getCaptcha()
  }

  // Confirm and send SMS code after captcha verification
  const confirmSendSms = async () => {
    if (!captchaCode.trim()) {
      setCaptchaError('请输入验证码')
      return
    }

    try {
      const res = await fetch('/api/account/verification-code/sms', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          phone: formData.phone,
          purpose: 'REGISTER',
          captchaId,
          captchaCode,
        }),
      })

      if (res.ok) {
        setShowCaptchaModal(false)
        setCaptchaCode('')
        setCaptchaUrl('')
        // Start countdown
        setCountdown(60)
        const timer = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) {
              clearInterval(timer)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      } else {
        const data = await res.json()
        setCaptchaError(data.message || '验证码错误，请重试')
        getCaptcha() // Refresh captcha on error
      }
    } catch {
      setCaptchaError('发送失败，请重试')
      getCaptcha()
    }
  }

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: 实现注册逻辑
    console.log('Register with:', formData)
  }

  const handleInputChange = (field: keyof typeof formData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  // Status icon component
  const StatusIcon = ({ status }: { status: UsernameStatus | PhoneStatus }) => {
    if (status === 'checking') {
      return (
        <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl animate-spin">
          refresh
        </span>
      )
    }
    if (status === 'available') {
      return (
        <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-green-500 text-xl">
          check_circle
        </span>
      )
    }
    if (status === 'taken') {
      return (
        <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-red-500 text-xl">
          cancel
        </span>
      )
    }
    return null
  }

  // Check if form is valid for submission
  const isFormValid =
    formData.username &&
    usernameStatus === 'available' &&
    formData.phone &&
    phoneStatus === 'available' &&
    formData.password &&
    formData.confirmPassword &&
    formData.password === formData.confirmPassword &&
    formData.verificationCode &&
    agreeTerms

  return (
    <div className="min-h-screen flex flex-col bg-background-light dark:bg-background-dark font-display">
      <div className="flex-grow flex items-center justify-center p-4 sm:p-8">
        <div className="max-w-[1000px] w-full grid lg:grid-cols-2 bg-white dark:bg-slate-900 rounded-xl shadow-xl overflow-hidden min-h-[600px]">
          {/* Left Side: Branding & Info */}
          <div className="hidden lg:flex flex-col justify-between p-10 bg-primary/5 border-r border-slate-100 dark:border-slate-800">
            <div>
              <div className="flex items-center gap-3 mb-12">
                <img src="/logo-light-200.png" alt="Logo" className="h-10 w-auto" />
                <h2 className="text-slate-900 dark:text-slate-100 text-2xl font-bold tracking-tight">
                  海创元智研云平台
                </h2>
              </div>
              <div className="space-y-6">
                <h1 className="text-slate-900 dark:text-slate-100 text-4xl font-bold leading-tight">
                  连接智慧
                  <br />
                  驱动科研创新
                </h1>
                <p className="text-slate-600 dark:text-slate-400 text-lg">
                  构建一站式数智化科研协作平台，助力科研效率提升。通过先进的AI算法与云计算技术，连接全球科研资源。
                </p>
              </div>
            </div>
            <div className="mt-8">
              <div
                className="aspect-video rounded-lg bg-cover bg-center shadow-md"
                style={{
                  backgroundImage:
                    "url('https://lh3.googleusercontent.com/aida-public/AB6AXuCSaQIHIuEIs5sBP80XfbMuYkyZM9M3LxEKIrrEKup1-0gIRef6X3id_9evE0-hdd0gFJak3zxXVPjzXxcZcpPqxu1h-I15fOrZDgY3Msuaw1UIjtufCd25Xvl4UA8rlC6G09w7ZLwSuteRHb7O-eJx3AVTnYrRAAuNZ5cNA9qjcp4T1Cy2E_GjtJUS03YlyMaS3K4HuLMt5CJST4FOwPy7zPL1mzyMe-Y5Hl3sOyVzPbMJWn-A-8qsWTBBMnc5rOX-7bq7HOZhQ1s')",
                }}
              />
            </div>
          </div>

          {/* Right Side: Registration Form */}
          <div className="flex flex-col justify-center p-10">
            <div className="w-full max-w-md mx-auto">
              {/* Mobile Logo */}
              <div className="lg:hidden flex items-center gap-2 mb-8">
                <img src="/logo-light-200.png" alt="Logo" className="h-8 w-auto" />
                <h2 className="text-xl font-bold text-slate-900 dark:text-white">海创元智研云</h2>
              </div>

              <div className="mb-8">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mb-2">
                  创建您的账号
                </h3>
                <p className="text-slate-500 dark:text-slate-400">请填写以下信息完成注册</p>
              </div>

              <form onSubmit={handleRegister} className="space-y-4">
                {/* Username */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    用户名 <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      badge
                    </span>
                    <input
                      type="text"
                      placeholder="3-20位字符，字母开头"
                      value={formData.username}
                      onChange={(e) => handleInputChange('username', e.target.value)}
                      className={`w-full pl-10 pr-10 py-3 rounded-lg border bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${
                        usernameStatus === 'taken'
                          ? 'border-red-300 dark:border-red-700'
                          : usernameStatus === 'available'
                            ? 'border-green-300 dark:border-green-700'
                            : 'border-slate-200 dark:border-slate-700'
                      }`}
                    />
                    <StatusIcon status={usernameStatus} />
                  </div>
                  {usernameStatus === 'taken' && (
                    <p className="text-xs text-red-500">用户名已被占用或格式不正确</p>
                  )}
                </div>

                {/* Phone */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    手机号 <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      phone
                    </span>
                    <input
                      type="tel"
                      placeholder="请输入11位手机号"
                      value={formData.phone}
                      onChange={(e) => handleInputChange('phone', e.target.value)}
                      maxLength={11}
                      className={`w-full pl-10 pr-10 py-3 rounded-lg border bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${
                        phoneStatus === 'taken'
                          ? 'border-red-300 dark:border-red-700'
                          : phoneStatus === 'available'
                            ? 'border-green-300 dark:border-green-700'
                            : 'border-slate-200 dark:border-slate-700'
                      }`}
                    />
                    <StatusIcon status={phoneStatus} />
                  </div>
                  {phoneStatus === 'taken' && (
                    <p className="text-xs text-red-500">手机号已被注册或格式不正确</p>
                  )}
                </div>

                {/* Password */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    设置密码 <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      lock
                    </span>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      placeholder="8-20位字符，包含字母及数字"
                      value={formData.password}
                      onChange={(e) => handleInputChange('password', e.target.value)}
                      className="w-full pl-10 pr-12 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      <span className="material-symbols-outlined text-xl">
                        {showPassword ? 'visibility' : 'visibility_off'}
                      </span>
                    </button>
                  </div>
                </div>

                {/* Confirm Password */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    确认密码 <span className="text-red-500">*</span>
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      lock_reset
                    </span>
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      placeholder="请再次输入密码"
                      value={formData.confirmPassword}
                      onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
                      className={`w-full pl-10 pr-12 py-3 rounded-lg border bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${
                        formData.confirmPassword && formData.password !== formData.confirmPassword
                          ? 'border-red-300 dark:border-red-700'
                          : 'border-slate-200 dark:border-slate-700'
                      }`}
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      <span className="material-symbols-outlined text-xl">
                        {showConfirmPassword ? 'visibility' : 'visibility_off'}
                      </span>
                    </button>
                  </div>
                  {formData.confirmPassword && formData.password !== formData.confirmPassword && (
                    <p className="text-xs text-red-500">两次输入的密码不一致</p>
                  )}
                </div>

                {/* Nickname (Optional) */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    昵称 <span className="text-slate-400 text-xs">(选填，默认使用用户名)</span>
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      face
                    </span>
                    <input
                      type="text"
                      placeholder="请输入昵称"
                      value={formData.nickname}
                      onChange={(e) => handleInputChange('nickname', e.target.value)}
                      className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                    />
                  </div>
                </div>

                {/* SMS Code */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    验证码 <span className="text-red-500">*</span>
                  </label>
                  <div className="flex gap-3">
                    <div className="relative flex-1">
                      <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                        verified_user
                      </span>
                      <input
                        type="text"
                        placeholder="请输入验证码"
                        value={formData.verificationCode}
                        onChange={(e) => handleInputChange('verificationCode', e.target.value)}
                        className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                      />
                    </div>
                    <button
                      type="button"
                      onClick={sendSmsCode}
                      disabled={countdown > 0 || phoneStatus !== 'available'}
                      className="px-4 py-3 rounded-lg border border-primary text-primary font-medium hover:bg-primary/5 transition-colors whitespace-nowrap min-w-[100px] disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {countdown > 0 ? `${countdown}s` : '获取验证码'}
                    </button>
                  </div>
                </div>

                {/* Compliance */}
                <div className="flex items-start gap-2 pt-2">
                  <input
                    id="terms"
                    type="checkbox"
                    checked={agreeTerms}
                    onChange={(e) => setAgreeTerms(e.target.checked)}
                    className="mt-1 rounded border-slate-300 text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                  />
                  <label
                    htmlFor="terms"
                    className="text-sm text-slate-500 dark:text-slate-400 leading-tight cursor-pointer"
                  >
                    我已阅读并同意{' '}
                    <a className="text-primary hover:underline" href="#">
                      《用户服务协议》
                    </a>{' '}
                    和{' '}
                    <a className="text-primary hover:underline" href="#">
                      《隐私政策》
                    </a>
                  </label>
                </div>

                {/* Actions */}
                <div className="pt-4">
                  <button
                    type="submit"
                    disabled={!isFormValid}
                    className="w-full bg-primary text-white py-3.5 rounded-lg font-bold text-lg hover:bg-primary/90 shadow-lg shadow-primary/20 transition-all transform active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none"
                  >
                    立即注册
                  </button>
                </div>

                <div className="text-center pt-4">
                  <p className="text-slate-500 dark:text-slate-400 text-sm">
                    已有账号？{' '}
                    <a className="text-primary font-semibold hover:underline" href="/login">
                      立即登录
                    </a>
                  </p>
                </div>
              </form>
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

      {/* Captcha Modal */}
      {showCaptchaModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-slate-900 rounded-lg p-6 max-w-sm w-full mx-4 shadow-2xl">
            <h3 className="text-lg font-bold mb-4 text-slate-900 dark:text-slate-100">请输入图形验证码</h3>
            {captchaUrl ? (
              <img
                src={captchaUrl}
                alt="验证码"
                className="mb-4 cursor-pointer rounded-lg border border-slate-200 dark:border-slate-700 hover:opacity-80 transition-opacity"
                onClick={getCaptcha}
              />
            ) : (
              <div className="mb-4 h-12 bg-slate-100 dark:bg-slate-800 rounded-lg animate-pulse" />
            )}
            <input
              type="text"
              placeholder="请输入验证码"
              className="w-full px-4 py-2 border rounded-lg mb-4 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none"
              value={captchaCode}
              onChange={(e) => setCaptchaCode(e.target.value)}
              autoFocus
            />
            {captchaError && <p className="text-red-500 text-sm mb-4">{captchaError}</p>}
            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => {
                  setShowCaptchaModal(false)
                  setCaptchaCode('')
                  setCaptchaError('')
                }}
                className="flex-1 py-2 border rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
              >
                取消
              </button>
              <button
                type="button"
                onClick={confirmSendSms}
                className="flex-1 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors"
              >
                确认
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
