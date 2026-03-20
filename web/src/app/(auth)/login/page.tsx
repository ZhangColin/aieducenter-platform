'use client'

import { useState } from 'react'

export default function LoginPage() {
  const [loginType, setLoginType] = useState<'account' | 'sms'>('account')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: 实现登录逻辑
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
            {/* Abstract background decoration */}
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
                  onClick={() => setLoginType('account')}
                  className={`px-6 py-3 text-sm font-bold border-b-2 transition-colors ${
                    loginType === 'account'
                      ? 'text-primary border-primary'
                      : 'text-slate-500 border-transparent hover:text-slate-700 dark:hover:text-slate-300'
                  }`}
                >
                  账号登录
                </button>
                <button
                  onClick={() => setLoginType('sms')}
                  className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
                    loginType === 'sms'
                      ? 'text-primary border-primary'
                      : 'text-slate-500 border-transparent hover:text-slate-700 dark:hover:text-slate-300'
                  }`}
                >
                  手机验证码
                </button>
              </div>

              <form onSubmit={handleLogin} className="space-y-5">
                {loginType === 'account' ? (
                  <>
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        邮箱/手机号
                      </label>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          person
                        </span>
                        <input
                          type="text"
                          placeholder="请输入账号"
                          className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <div className="flex justify-between">
                        <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                          登录密码
                        </label>
                        <a className="text-xs text-primary hover:underline" href="#">
                          忘记密码？
                        </a>
                      </div>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          lock
                        </span>
                        <input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="请输入密码"
                          className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
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
                    </div>

                    <div className="flex items-center">
                      <input
                        id="remember"
                        type="checkbox"
                        checked={rememberMe}
                        onChange={(e) => setRememberMe(e.target.checked)}
                        className="size-4 rounded border-slate-300 text-primary focus:ring-primary cursor-pointer"
                      />
                      <label
                        htmlFor="remember"
                        className="ml-2 text-sm text-slate-600 dark:text-slate-400 cursor-pointer"
                      >
                        30天内免登录
                      </label>
                    </div>
                  </>
                ) : (
                  <>
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
                          className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        验证码
                      </label>
                      <div className="flex gap-3">
                        <div className="relative flex-1">
                          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                            verified
                          </span>
                          <input
                            type="text"
                            placeholder="请输入验证码"
                            className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                          />
                        </div>
                        <button
                          type="button"
                          className="px-4 py-3 whitespace-nowrap rounded-lg border border-primary text-primary font-medium hover:bg-primary/5 transition-colors"
                        >
                          获取验证码
                        </button>
                      </div>
                    </div>
                  </>
                )}

                <button
                  type="submit"
                  className="w-full py-3 bg-primary hover:bg-primary/90 text-white font-bold rounded-lg transition-colors shadow-lg shadow-primary/20"
                >
                  立即登录
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
                  <button className="flex items-center justify-center gap-2 py-2 px-4 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                    <div className="size-5 rounded-full bg-[#07C160] flex items-center justify-center text-white">
                      <span className="material-symbols-outlined text-[14px]">chat</span>
                    </div>
                    <span className="text-sm text-slate-600 dark:text-slate-300">微信登录</span>
                  </button>
                  <button className="flex items-center justify-center gap-2 py-2 px-4 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                    <div className="size-5 rounded-full bg-[#0089FF] flex items-center justify-center text-white">
                      <span className="material-symbols-outlined text-[14px]">business</span>
                    </div>
                    <span className="text-sm text-slate-600 dark:text-slate-300">钉钉登录</span>
                  </button>
                </div>
              </div>

              <p className="mt-8 text-center text-sm text-slate-500">
                还没有账号？{' '}
                <a className="text-primary font-semibold hover:underline" href="/register">
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
