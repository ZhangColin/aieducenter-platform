import Link from 'next/link'

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col bg-background-light dark:bg-background-dark font-display">
      {/* Top Navigation Bar */}
      <header className="w-full bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-3">
              <img
                alt="海创元智研云平台 Logo"
                className="h-8 w-auto"
                src="/logo-light-200.png"
              />
              <span className="text-[#1E293B] dark:text-slate-100 text-xl font-semibold whitespace-nowrap">
                海创元智研云平台
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-grow flex items-center justify-center px-4 py-12">
        <div className="max-w-2xl w-full text-center">
          {/* 404 Visual */}
          <div className="relative mb-8">
            <h1 className="text-[120px] sm:text-[180px] font-black leading-none text-primary/10 select-none">
              404
            </h1>
            <div className="absolute inset-0 flex items-center justify-center">
              <span className="material-symbols-outlined text-primary opacity-80" style={{ fontSize: '5rem', lineHeight: 1 }}>
                sentiment_dissatisfied
              </span>
            </div>
          </div>

          {/* Error Message */}
          <h2 className="text-2xl sm:text-3xl font-bold text-slate-900 dark:text-slate-100 mb-4">
            抱歉，您访问的页面不存在
          </h2>
          <p className="text-slate-500 dark:text-slate-400 text-lg mb-10 max-w-md mx-auto">
            该页面可能已被删除、重命名或暂时不可用。
          </p>

          {/* Primary Action */}
          <div className="flex justify-center mb-12">
            <Link
              className="inline-flex items-center justify-center px-8 py-3 bg-primary hover:bg-primary/90 text-white font-bold rounded-lg transition-colors shadow-lg shadow-primary/20"
              href="/"
            >
              <span className="material-symbols-outlined mr-2 text-xl">home</span>
              返回首页
            </Link>
          </div>

          {/* Secondary Quick Links */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 border-t border-slate-200 dark:border-slate-800 pt-10">
            <a
              className="flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:border-primary hover:text-primary transition-all group"
              href="#"
            >
              <span className="material-symbols-outlined text-slate-400 group-hover:text-primary">
                help
              </span>
              <span className="font-medium">帮助中心</span>
            </a>
            <a
              className="flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:border-primary hover:text-primary transition-all group"
              href="#"
            >
              <span className="material-symbols-outlined text-slate-400 group-hover:text-primary">
                support_agent
              </span>
              <span className="font-medium">联系客服</span>
            </a>
            <a
              className="flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:border-primary hover:text-primary transition-all group"
              href="#"
            >
              <span className="material-symbols-outlined text-slate-400 group-hover:text-primary">
                dashboard
              </span>
              <span className="font-medium">进入工作台</span>
            </a>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full py-8 border-t border-slate-200 dark:border-slate-800 text-center">
        <p className="text-slate-400 dark:text-slate-500 text-sm">
          © 2024 海创元智研云平台 版权所有
        </p>
      </footer>
    </div>
  )
}
