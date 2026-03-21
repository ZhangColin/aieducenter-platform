'use client'

export function DashboardHeader() {
  return (
    <header className="h-16 flex items-center justify-between px-8 bg-white/50 dark:bg-slate-900/50 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 z-10">
      {/* Breadcrumb */}
      <div className="flex items-center gap-4">
        <nav className="flex items-center text-sm text-slate-500 dark:text-slate-400">
          <span>首页</span>
          <span className="mx-2 text-slate-300">/</span>
          <span className="text-slate-900 dark:text-white font-medium">控制台</span>
        </nav>
      </div>

      {/* Right Side Actions */}
      <div className="flex items-center gap-6">
        {/* Search */}
        <div className="relative hidden lg:block">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">
            search
          </span>
          <input
            className="pl-10 pr-4 py-1.5 w-64 bg-slate-100/50 dark:bg-slate-800/50 border-none rounded-lg text-sm focus:ring-2 focus:ring-primary/30 transition-all"
            placeholder="搜索资源、租户或文档..."
            type="text"
          />
        </div>

        {/* Actions & Profile */}
        <div className="flex items-center gap-4">
          {/* Notifications */}
          <button className="p-2 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg relative transition-colors">
            <span className="material-symbols-outlined">notifications</span>
            <span className="absolute top-2.5 right-2.5 size-1.5 bg-red-500 rounded-full border border-white dark:border-slate-900" />
          </button>

          <div className="h-8 w-px bg-slate-200 dark:bg-slate-700" />

          {/* User Profile */}
          <div className="flex items-center gap-3 cursor-pointer group">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-bold text-slate-900 dark:text-white leading-none">超级管理员</p>
              <p className="text-[10px] text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-tight font-medium">
                Platform Admin
              </p>
            </div>
            <img
              className="size-10 rounded-full border-2 border-slate-100 dark:border-slate-800 group-hover:border-primary transition-colors object-cover"
              alt="User profile avatar of admin"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuAUnZraYRc-bfrcT_LJFqw_hzV-6AKmcbAQKrmgoeiF12mn9ecR6djRQRzMO7GP5cuaJkxA-k5HTQ6Q3dZUhgJDQqIpebqVlL-xbtFfXr6B37QZeQ5Ks8fEXrb4i65N1yxYIHsjo7hWT6VDX8rt99cIdlavi_phfSjHE4FfGHYrk6jxXpVGjT2did60myFWpNSgopfwXHorFg4SKlOSWYCydNItSG407gIFcMarsyZbhsRAX8ZhlOTl_roBf48MT_gNrC473z4rurA"
            />
          </div>
        </div>
      </div>
    </header>
  )
}
