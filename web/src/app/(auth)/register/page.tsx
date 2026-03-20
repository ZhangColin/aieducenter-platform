'use client'

import { useState } from 'react'

export default function RegisterPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [countdown, setCountdown] = useState(0)

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: 实现注册逻辑
  }

  const handleSendCode = () => {
    if (countdown > 0) return
    // TODO: 实现发送验证码逻辑
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
  }

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

              <form onSubmit={handleRegister} className="space-y-5">
                {/* Mobile/Email */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    手机号/邮箱
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      person
                    </span>
                    <input
                      type="text"
                      placeholder="请输入手机号或邮箱"
                      className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                    />
                  </div>
                </div>

                {/* SMS Code */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    验证码
                  </label>
                  <div className="flex gap-3">
                    <div className="relative flex-1">
                      <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                        verified_user
                      </span>
                      <input
                        type="text"
                        placeholder="请输入验证码"
                        className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                      />
                    </div>
                    <button
                      type="button"
                      onClick={handleSendCode}
                      disabled={countdown > 0}
                      className="px-4 py-3 rounded-lg border border-primary text-primary font-medium hover:bg-primary/5 transition-colors whitespace-nowrap min-w-[100px] disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {countdown > 0 ? `${countdown}s` : '获取验证码'}
                    </button>
                  </div>
                </div>

                {/* Password */}
                <div className="space-y-1.5">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    设置密码
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      lock
                    </span>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      placeholder="8-20位字符，包含字母及数字"
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
                    确认密码
                  </label>
                  <div className="relative">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                      lock_reset
                    </span>
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      placeholder="请再次输入密码"
                      className="w-full pl-10 pr-12 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
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
                    className="w-full bg-primary text-white py-3.5 rounded-lg font-bold text-lg hover:bg-primary/90 shadow-lg shadow-primary/20 transition-all transform active:scale-[0.98]"
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
    </div>
  )
}
