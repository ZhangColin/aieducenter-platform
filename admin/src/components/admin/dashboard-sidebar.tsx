'use client'

import { useState } from 'react'

interface DashboardSidebarProps {
  collapsed: boolean
  onToggle: () => void
}

export function DashboardSidebar({ collapsed, onToggle }: DashboardSidebarProps) {
  const [activeTier, setActiveTier] = useState('dashboard')

  const tierButtons = [
    { id: 'dashboard', icon: 'dashboard', title: '控制台' },
    { id: 'tenants', icon: 'corporate_fare', title: '租户管理' },
    { id: 'users', icon: 'group', title: '用户管理' },
    { id: 'models', icon: 'memory', title: '模型能力' },
    { id: 'finance', icon: 'account_balance_wallet', title: '财务管理' },
  ]

  return (
    <div className="flex flex-shrink-0">
      {/* First Tier: Icon Bar (Narrow) */}
      <aside className="w-20 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 flex flex-col items-center py-6 gap-6 z-30">
        {/* Logo */}
        <div className="size-10 flex items-center justify-center mb-2">
          <img
            alt="Logo"
            className="size-full object-contain"
            src="/logo-light-200.png"
          />
        </div>

        {/* Navigation Icons */}
        <nav className="flex flex-col gap-4">
          {tierButtons.map((btn) => (
            <button
              key={btn.id}
              className={`size-12 flex items-center justify-center rounded-xl text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all ${
                activeTier === btn.id
                  ? 'first-tier-active shadow-md'
                  : ''
              }`}
              title={btn.title}
              onClick={() => setActiveTier(btn.id)}
            >
              <span className="material-symbols-outlined">{btn.icon}</span>
            </button>
          ))}

          <div className="h-px w-8 bg-slate-100 dark:bg-slate-800 my-2" />

          <button
            className="size-12 flex items-center justify-center rounded-xl text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all"
            title="系统设置"
          >
            <span className="material-symbols-outlined">settings</span>
          </button>
        </nav>

        {/* Bottom Help Button */}
        <div className="mt-auto flex flex-col gap-4">
          <button
            className="size-12 flex items-center justify-center rounded-xl text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all"
            title="帮助"
          >
            <span className="material-symbols-outlined">help</span>
          </button>
        </div>
      </aside>

      {/* Second Tier: Detailed Pane (Collapsible) */}
      <aside
        className={`secondary-sidebar w-64 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border-r border-slate-200 dark:border-slate-800 flex flex-col z-20 overflow-hidden ${
          collapsed ? 'collapsed' : ''
        }`}
      >
        {/* Platform Logo & Name Section */}
        <div className="h-16 flex items-center px-4 border-b border-slate-100 dark:border-slate-800 justify-between">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="size-8 bg-primary/10 rounded flex-shrink-0 flex items-center justify-center text-primary">
              <span className="material-symbols-outlined text-xl">cloud_done</span>
            </div>
            <h1 className="text-sm font-bold text-slate-900 dark:text-white truncate">海创元智研云平台</h1>
          </div>
          <button
            className="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-md text-slate-400"
            onClick={onToggle}
          >
            <span className="material-symbols-outlined">menu_open</span>
          </button>
        </div>

        {/* Menu Groups */}
        <div className="flex-1 overflow-y-auto py-6 px-4 space-y-6">
          {/* Dashboard Sub-menu (Always Visible) */}
          <div className="menu-group">
            <div className="px-3 py-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
              <span>概览与分析</span>
            </div>
            <div className="menu-content mt-1 space-y-1">
              <a
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg active-nav-bg font-medium text-sm"
                href="#"
              >
                <span className="size-1.5 rounded-full bg-primary" />
                <span>实时看板</span>
              </a>
              <a
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm"
                href="#"
              >
                <span className="size-1.5 rounded-full bg-transparent border border-slate-300" />
                <span>核心指标明细</span>
              </a>
              <a
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm"
                href="#"
              >
                <span className="size-1.5 rounded-full bg-transparent border border-slate-300" />
                <span>资源消耗预测</span>
              </a>
            </div>
          </div>

          {/* Shortcut Functions (Always Visible) */}
          <div className="menu-group">
            <div className="px-3 py-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
              <span>快捷功能</span>
            </div>
            <div className="menu-content mt-1 space-y-1">
              <a
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm"
                href="#"
              >
                <span className="material-symbols-outlined text-lg">bolt</span>
                <span>快速审批</span>
              </a>
              <a
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm"
                href="#"
              >
                <span className="material-symbols-outlined text-lg">analytics</span>
                <span>导出报表</span>
              </a>
            </div>
          </div>

          {/* System Status (Always Visible) */}
          <div className="menu-group">
            <div className="px-3 py-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
              <span>服务治理</span>
            </div>
            <div className="menu-content mt-1 space-y-1">
              <a
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm"
                href="#"
              >
                <span className="material-symbols-outlined text-lg">dns</span>
                <span>节点管理</span>
              </a>
              <a
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-sm"
                href="#"
              >
                <span className="material-symbols-outlined text-lg">shield</span>
                <span>安全审计</span>
              </a>
            </div>
          </div>
        </div>

        {/* System Status Footer */}
        <div className="p-4 border-t border-slate-100 dark:border-slate-800">
          <div className="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="size-8 rounded-full bg-emerald-500/10 flex items-center justify-center">
                <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
              </div>
              <div>
                <p className="text-[10px] text-slate-400 leading-none">系统状态</p>
                <p className="text-xs font-bold text-slate-700 dark:text-slate-300 mt-1">运行正常</p>
              </div>
            </div>
            <span className="material-symbols-outlined text-slate-300 text-sm">info</span>
          </div>
        </div>
      </aside>
    </div>
  )
}
