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
  const [isRegistering, setIsRegistering] = useState(false)
  const [registerError, setRegisterError] = useState('')
  const [toast, setToast] = useState('')

  // Form data
  const [formData, setFormData] = useState({
    username: '',
    phone: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    verificationCode: '',
  })

  // Validation error states
  const [usernameError, setUsernameError] = useState('')
  const [phoneError, setPhoneError] = useState('')
  const [passwordError, setPasswordError] = useState('')
  const [confirmPasswordError, setConfirmPasswordError] = useState('')

  // Availability check states
  const [usernameStatus, setUsernameStatus] = useState<UsernameStatus>()
  const [phoneStatus, setPhoneStatus] = useState<PhoneStatus>()

  // Captcha states (inline, not modal)
  const [showCaptcha, setShowCaptcha] = useState(false)
  const [captchaUrl, setCaptchaUrl] = useState('')
  const [captchaId, setCaptchaId] = useState('')
  const [captchaCode, setCaptchaCode] = useState('')
  const [captchaError, setCaptchaError] = useState('')
  const [smsSent, setSmsSent] = useState(false)

  // Show toast message
  const showToast = (msg: string) => {
    setToast(msg)
    setTimeout(() => setToast(''), 3000)
  }

  // Username validation and availability check
  useEffect(() => {
    const timeoutId = setTimeout(async () => {
      if (formData.username) {
        await checkUsername(formData.username)
      } else {
        setUsernameStatus(undefined)
        setUsernameError('')
      }
    }, 500)

    return () => clearTimeout(timeoutId)
  }, [formData.username])

  const checkUsername = async (username: string) => {
    // Basic validation: 3-20 chars, letter start
    const usernameRegex = /^[a-zA-Z][a-zA-Z0-9_]{2,19}$/

    if (!username) {
      setUsernameError('')
      setUsernameStatus(undefined)
      return
    }

    if (username.length < 3) {
      setUsernameError('用户名至少需要3个字符')
      setUsernameStatus(undefined)
      return
    }

    if (username.length > 20) {
      setUsernameError('用户名最多20个字符')
      setUsernameStatus(undefined)
      return
    }

    if (!usernameRegex.test(username)) {
      setUsernameError('用户名必须以字母开头，只能包含字母、数字和下划线')
      setUsernameStatus(undefined)
      return
    }

    setUsernameError('')
    setUsernameStatus('checking')
    try {
      const res = await fetch(
        `/api/account/check-username?username=${encodeURIComponent(username)}`
      )
      const data = await res.json()
      setUsernameStatus(data.data ? 'available' : 'taken')
      if (!data.data) {
        setUsernameError('该用户名已被占用')
      }
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
        setPhoneError('')
      }
    }, 500)

    return () => clearTimeout(timeoutId)
  }, [formData.phone])

  const checkPhone = async (phone: string) => {
    // Basic validation: 11 digits, starts with 1
    const phoneRegex = /^1[3-9]\d{9}$/

    if (!phone) {
      setPhoneError('')
      setPhoneStatus(undefined)
      return
    }

    if (phone.length < 11) {
      setPhoneError('请输入11位手机号')
      setPhoneStatus(undefined)
      return
    }

    if (!phoneRegex.test(phone)) {
      setPhoneError('手机号格式不正确（1开头，11位数字）')
      setPhoneStatus(undefined)
      return
    }

    setPhoneError('')
    setPhoneStatus('checking')
    try {
      const res = await fetch(`/api/account/check-phone?phone=${phone}`)
      const data = await res.json()
      setPhoneStatus(data.data ? 'available' : 'taken')
      if (!data.data) {
        setPhoneError('该手机号已被注册')
      }
    } catch {
      setPhoneStatus(undefined)
    }
  }

  // Password validation
  useEffect(() => {
    if (!formData.password) {
      setPasswordError('')
      return
    }

    if (formData.password.length < 8) {
      setPasswordError('密码至少需要8个字符')
      return
    }

    if (formData.password.length > 20) {
      setPasswordError('密码最多20个字符')
      return
    }

    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d).+$/
    if (!passwordRegex.test(formData.password)) {
      setPasswordError('密码必须同时包含字母和数字')
      return
    }

    setPasswordError('')

    // Check confirm password match
    if (formData.confirmPassword && formData.password !== formData.confirmPassword) {
      setConfirmPasswordError('两次输入的密码不一致')
    } else {
      setConfirmPasswordError('')
    }
  }, [formData.password])

  // Confirm password validation
  useEffect(() => {
    if (!formData.confirmPassword) {
      setConfirmPasswordError('')
      return
    }

    if (formData.password !== formData.confirmPassword) {
      setConfirmPasswordError('两次输入的密码不一致')
    } else {
      setConfirmPasswordError('')
    }
  }, [formData.confirmPassword, formData.password])

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

  // Show captcha inline when clicking "Get SMS Code"
  const sendSmsCode = () => {
    if (countdown > 0) return
    if (!formData.phone || phoneStatus !== 'available') {
      return
    }
    if (!showCaptcha) {
      setShowCaptcha(true)
      getCaptcha()
    }
  }

  // Send SMS code after captcha verification
  const confirmSendSms = async () => {
    if (!captchaCode.trim()) {
      setCaptchaError('请输入图形验证码')
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
        setCaptchaCode('')
        setCaptchaUrl('')
        setShowCaptcha(false)
        setSmsSent(true)
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
        showToast('验证码已发送')
      } else {
        const data = await res.json()
        setCaptchaError(data.message || '验证码错误')
        getCaptcha()
      }
    } catch {
      setCaptchaError('发送失败，请重试')
      getCaptcha()
    }
  }

  // Register API call
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setRegisterError('')
    setIsRegistering(true)

    try {
      const res = await fetch('/api/account/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: formData.username,
          password: formData.password,
          nickname: formData.nickname || undefined,
          phone: formData.phone,
          verificationCode: formData.verificationCode,
        }),
      })

      const data = await res.json()

      if (res.ok) {
        showToast('注册成功！')
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
      } else {
        setRegisterError(data.message || '注册失败，请重试')
      }
    } catch {
      setRegisterError('网络错误，请检查连接后重试')
    } finally {
      setIsRegistering(false)
    }
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
    !usernameError &&
    usernameStatus === 'available' &&
    formData.phone &&
    !phoneError &&
    phoneStatus === 'available' &&
    formData.password &&
    !passwordError &&
    formData.confirmPassword &&
    !confirmPasswordError &&
    formData.verificationCode &&
    agreeTerms

  return (
    <div className="min-h-screen flex flex-col bg-background-light dark:bg-background-dark font-display">
      {/* Toast Notification - Light Theme */}
      {toast && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 px-6 py-3 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-lg shadow-lg flex items-center gap-2 animate-fade-in">
          <span className="material-symbols-outlined text-emerald-600">check_circle</span>
          <span className="text-sm font-medium">{toast}</span>
        </div>
      )}

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
                        usernameError ? 'border-red-300 dark:border-red-700' :
                        usernameStatus === 'available' ? 'border-green-300 dark:border-green-700' :
                        'border-slate-200 dark:border-slate-700'
                      }`}
                    />
                    <StatusIcon status={usernameStatus} />
                  </div>
                  {usernameError && <p className="text-xs text-red-500">{usernameError}</p>}
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
                        phoneError ? 'border-red-300 dark:border-red-700' :
                        phoneStatus === 'available' ? 'border-green-300 dark:border-green-700' :
                        'border-slate-200 dark:border-slate-700'
                      }`}
                    />
                    <StatusIcon status={phoneStatus} />
                  </div>
                  {phoneError && <p className="text-xs text-red-500">{phoneError}</p>}
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
                      className={`w-full pl-10 pr-12 py-3 rounded-lg border bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all ${
                        passwordError ? 'border-red-300 dark:border-red-700' : 'border-slate-200 dark:border-slate-700'
                      }`}
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
                  {passwordError && <p className="text-xs text-red-500">{passwordError}</p>}
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
                        confirmPasswordError ? 'border-red-300 dark:border-red-700' : 'border-slate-200 dark:border-slate-700'
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
                  {confirmPasswordError && <p className="text-xs text-red-500">{confirmPasswordError}</p>}
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

                {/* SMS Code with Inline Captcha */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    验证码 <span className="text-red-500">*</span>
                  </label>

                  {/* SMS Code Input */}
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
                      className="px-4 py-3 rounded-lg border border-primary text-primary font-medium hover:bg-primary/5 transition-colors whitespace-nowrap min-w-[100px] disabled:opacity-50 disabled:cursor-not-allowed relative"
                    >
                      {countdown > 0 ? `${countdown}s` : smsSent ? '重新获取' : '获取验证码'}
                    </button>
                  </div>

                  {/* Inline Captcha Section */}
                  {showCaptcha && (
                    <div className="mt-3 p-4 bg-slate-50 dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 animate-fade-in">
                      <p className="text-xs text-slate-500 mb-2">请输入图形验证码后发送短信验证码</p>
                      <div className="flex gap-3 items-start">
                        {captchaUrl ? (
                          <img
                            src={captchaUrl}
                            alt="验证码"
                            className="cursor-pointer rounded border border-slate-200 dark:border-slate-700 hover:opacity-80 transition-opacity"
                            onClick={getCaptcha}
                          />
                        ) : (
                          <div className="w-32 h-12 bg-slate-200 dark:bg-slate-700 rounded animate-pulse" />
                        )}
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
                            onClick={confirmSendSms}
                            className="w-full py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-primary/90 transition-colors"
                          >
                            发送验证码
                          </button>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Success message */}
                  {smsSent && !showCaptcha && countdown === 0 && (
                    <p className="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">check_circle</span>
                      验证码已发送，固定为 123456
                    </p>
                  )}
                </div>

                {/* Register Error */}
                {registerError && (
                  <div className="p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                    <p className="text-sm text-red-600 dark:text-red-400 flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">error</span>
                      {registerError}
                    </p>
                  </div>
                )}

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
                    disabled={!isFormValid || isRegistering}
                    className="w-full bg-primary text-white py-3.5 rounded-lg font-bold text-lg hover:bg-primary/90 shadow-lg shadow-primary/20 transition-all transform active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none flex items-center justify-center gap-2"
                  >
                    {isRegistering ? (
                      <>
                        <span className="material-symbols-outlined animate-spin text-lg">refresh</span>
                        注册中...
                      </>
                    ) : (
                      <>
                        <span>立即注册</span>
                        <span className="material-symbols-outlined text-lg">arrow_forward</span>
                      </>
                    )}
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
    </div>
  )
}
