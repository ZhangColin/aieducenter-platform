'use client'

import { useState } from 'react'

export default function AdminLoginPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: 实现后台登录逻辑
  }

  return (
    <div className="min-h-screen bg-[#F8FAFC] dark:bg-background-dark flex items-center justify-center p-4 font-display">
      {/* Theme Toggle Button */}
      <div className="fixed top-6 right-6 z-50">
        <button className="p-2.5 rounded-full bg-white dark:bg-slate-800 shadow-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-all active:scale-95 flex items-center justify-center">
          <span className="material-symbols-outlined text-[20px]">dark_mode</span>
        </button>
      </div>

      {/* Login Card */}
      <div className="w-full max-w-[440px] bg-white dark:bg-slate-900 rounded-xl shadow-xl shadow-blue-500/5 overflow-hidden border border-slate-100 dark:border-slate-800">
        {/* Login Header/Logo Area */}
        <div className="pt-10 pb-6 px-8 flex flex-col items-center">
          <div className="mb-6">
            <img alt="Company Logo" className="h-20 w-auto object-contain" src="/logo-light-200.png" />
          </div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100 tracking-tight">平台总管理后台</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-2 text-sm">Admin Management System</p>
        </div>

        {/* Login Form Area */}
        <div className="px-8 pb-10">
          <form onSubmit={handleLogin} className="space-y-5">
            {/* Username Input */}
            <div className="flex flex-col gap-2">
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-300">用户名 / 邮箱</label>
              <div className="relative">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[20px]">person</span>
                <input
                  className="w-full pl-10 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                  placeholder="请输入您的账号"
                  required
                  type="text"
                />
              </div>
            </div>

            {/* Password Input */}
            <div className="flex flex-col gap-2">
              <div className="flex justify-between items-center">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-300">密码</label>
                <a className="text-xs text-primary hover:underline" href="#">忘记密码？</a>
              </div>
              <div className="relative">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[20px]">lock</span>
                <input
                  className="w-full pl-10 pr-12 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                  placeholder="请输入您的密码"
                  required
                  type={showPassword ? 'text' : 'password'}
                />
                <button
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  <span className="material-symbols-outlined text-[20px]">
                    {showPassword ? 'visibility' : 'visibility_off'}
                  </span>
                </button>
              </div>
            </div>

            {/* Remember Me */}
            <div className="flex items-center justify-between py-1">
              <label className="flex items-center gap-2 cursor-pointer group">
                <input
                  className="size-4 rounded border-slate-300 dark:border-slate-600 text-primary focus:ring-primary focus:ring-offset-0 transition-colors"
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                />
                <span className="text-sm text-slate-600 dark:text-slate-400 group-hover:text-slate-800 dark:group-hover:text-slate-200">记住我</span>
              </label>
            </div>

            {/* Login Button */}
            <button
              className="w-full bg-primary hover:bg-primary/90 text-white font-semibold py-3.5 rounded-lg shadow-md shadow-primary/20 transition-all active:scale-[0.98] flex items-center justify-center gap-2"
              type="submit"
            >
              <span>立即登录</span>
              <span className="material-symbols-outlined text-[18px]">login</span>
            </button>
          </form>

          {/* SSO/Corporate Identity */}
          <div className="mt-8">
            <div className="relative flex py-3 items-center">
              <div className="flex-grow border-t border-slate-200 dark:border-slate-700"></div>
              <span className="flex-shrink mx-4 text-xs text-slate-400 uppercase tracking-widest">其他登录方式</span>
              <div className="flex-grow border-t border-slate-200 dark:border-slate-700"></div>
            </div>
            <div className="mt-4 flex flex-col gap-3">
              <a
                className="flex items-center justify-center gap-2 w-full py-2.5 px-4 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm"
                href="#"
              >
                <span className="material-symbols-outlined text-[20px] text-primary">hub</span>
                使用企业 SSO 登录 (Corporate Identity)
              </a>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="bg-slate-50 dark:bg-slate-800/50 py-4 px-8 border-t border-slate-100 dark:border-slate-800">
          <p className="text-center text-[11px] text-slate-400 dark:text-slate-500 leading-relaxed uppercase tracking-tighter">
            © 2024 Platform Internal System. All rights reserved.
            <br />Security Level: Restricted Access
          </p>
        </div>
      </div>
    </div>
  )
}
