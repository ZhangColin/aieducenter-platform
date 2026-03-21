'use client'

import { useState } from 'react'
import { DashboardSidebar } from '@/components/admin/dashboard-sidebar'
import { DashboardHeader } from '@/components/admin/dashboard-header'

export default function DashboardPage() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)

  return (
    <div className={`flex h-screen overflow-hidden ${sidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
      {/* Two-Tier Navigation System */}
      <DashboardSidebar
        collapsed={sidebarCollapsed}
        onToggle={() => setSidebarCollapsed(!sidebarCollapsed)}
      />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col overflow-hidden">
        <DashboardHeader />

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto p-8 space-y-6">
          {/* Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Total Tenants */}
            <div className="bg-white dark:bg-slate-900 p-6 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start">
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">总租户数</p>
                <span className="p-2 bg-blue-50 dark:bg-blue-900/20 text-blue-600 rounded-lg material-symbols-outlined">apartment</span>
              </div>
              <div className="mt-4 flex items-baseline gap-2">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-white">1,280</h3>
                <span className="text-xs font-medium text-emerald-500 flex items-center">
                  <span className="material-symbols-outlined text-xs">trending_up</span> 12%
                </span>
              </div>
              <p className="text-[10px] text-slate-400 mt-2 font-medium">较上月新增 142 家</p>
            </div>

            {/* Total Users */}
            <div className="bg-white dark:bg-slate-900 p-6 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start">
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">全平台累计用户</p>
                <span className="p-2 bg-purple-50 dark:bg-purple-900/20 text-purple-600 rounded-lg material-symbols-outlined">groups</span>
              </div>
              <div className="mt-4 flex items-baseline gap-2">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-white">856,432</h3>
                <span className="text-xs font-medium text-emerald-500 flex items-center">
                  <span className="material-symbols-outlined text-xs">trending_up</span> 5.4%
                </span>
              </div>
              <p className="text-[10px] text-slate-400 mt-2 font-medium">平均日活跃 42k+</p>
            </div>

            {/* Today AI Calls */}
            <div className="bg-white dark:bg-slate-900 p-6 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start">
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">今日 AI 调用量</p>
                <span className="p-2 bg-amber-50 dark:bg-amber-900/20 text-amber-600 rounded-lg material-symbols-outlined">bolt</span>
              </div>
              <div className="mt-4 flex items-baseline gap-2">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-white">2.4M</h3>
                <span className="text-xs font-medium text-emerald-500 flex items-center">
                  <span className="material-symbols-outlined text-xs">trending_up</span> 18.2%
                </span>
              </div>
              <p className="text-[10px] text-slate-400 mt-2 font-medium">峰值并发 1,200 QPS</p>
            </div>

            {/* Monthly Revenue */}
            <div className="bg-white dark:bg-slate-900 p-6 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start">
                <p className="text-sm font-medium text-slate-500 dark:text-slate-400">本月总营收 (折合)</p>
                <span className="p-2 bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 rounded-lg material-symbols-outlined">payments</span>
              </div>
              <div className="mt-4 flex items-baseline gap-2">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-white">¥1,240,500</h3>
                <span className="text-xs font-medium text-rose-500 flex items-center">
                  <span className="material-symbols-outlined text-xs">trending_down</span> 2.1%
                </span>
              </div>
              <p className="text-[10px] text-slate-400 mt-2 font-medium">目标达成率 92.4%</p>
            </div>
          </div>

          {/* Middle Charts */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Area Chart */}
            <div className="lg:col-span-2 bg-white dark:bg-slate-900 p-6 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h4 className="text-base font-bold text-slate-900 dark:text-white">全平台 AI 调用趋势</h4>
                  <p className="text-[10px] font-medium text-slate-400 uppercase tracking-tight">过去 30 天调用量统计</p>
                </div>
                <select className="bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-[10px] font-bold rounded-lg px-3 py-1.5 focus:ring-primary">
                  <option>最近 30 天</option>
                  <option>最近 7 天</option>
                </select>
              </div>
              <div className="flex-1 min-h-[300px] flex flex-col justify-end">
                <div className="relative w-full h-full pt-4">
                  <svg className="w-full h-full" preserveAspectRatio="none" viewBox="0 0 800 240">
                    <defs>
                      <linearGradient id="chartGradient" x1="0" x2="0" y1="0" y2="1">
                        <stop offset="0%" stopColor="#308ce8" stopOpacity="0.2" />
                        <stop offset="100%" stopColor="#308ce8" stopOpacity="0" />
                      </linearGradient>
                    </defs>
                    <path d="M0,180 Q100,160 200,100 T400,120 T600,60 T800,40 L800,240 L0,240 Z" fill="url(#chartGradient)" />
                    <path d="M0,180 Q100,160 200,100 T400,120 T600,60 T800,40" fill="none" stroke="#308ce8" strokeWidth="3" />
                    <line className="dark:stroke-slate-800" stroke="#f1f5f9" strokeDasharray="4" x1="0" x2="800" y1="60" y2="60" />
                    <line className="dark:stroke-slate-800" stroke="#f1f5f9" strokeDasharray="4" x1="0" x2="800" y1="120" y2="120" />
                    <line className="dark:stroke-slate-800" stroke="#f1f5f9" strokeDasharray="4" x1="0" x2="800" y1="180" y2="180" />
                  </svg>
                </div>
                <div className="flex justify-between mt-4 px-2 text-[10px] font-bold text-slate-400">
                  <span>01 May</span>
                  <span>07 May</span>
                  <span>14 May</span>
                  <span>21 May</span>
                  <span>28 May</span>
                  <span>31 May</span>
                </div>
              </div>
            </div>

            {/* Pie Chart / Distribution */}
            <div className="bg-white dark:bg-slate-900 p-6 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm">
              <h4 className="text-base font-bold text-slate-900 dark:text-white mb-6">各模型消耗占比</h4>
              <div className="relative size-48 mx-auto flex items-center justify-center">
                <svg className="size-full rotate-[-90deg]" viewBox="0 0 100 100">
                  <circle className="dark:stroke-slate-800" cx="50" cy="50" fill="none" r="40" stroke="#f1f5f9" strokeWidth="12" />
                  <circle cx="50" cy="50" fill="none" r="40" stroke="#308ce8" strokeDasharray="125 251.2" strokeWidth="12" />
                  <circle cx="50" cy="50" fill="none" r="40" stroke="#a855f7" strokeDasharray="60 251.2" strokeDashoffset="-125" strokeWidth="12" />
                  <circle cx="50" cy="50" fill="none" r="40" stroke="#f59e0b" strokeDasharray="40 251.2" strokeDashoffset="-185" strokeWidth="12" />
                </svg>
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <span className="text-2xl font-bold text-slate-900 dark:text-white">100%</span>
                  <span className="text-[10px] text-slate-400 uppercase font-bold tracking-tighter">资源分布</span>
                </div>
              </div>
              <div className="mt-8 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="size-2 rounded-full bg-primary" />
                    <span className="text-xs text-slate-600 dark:text-slate-400 font-medium">GPT-4o / Turbo</span>
                  </div>
                  <span className="text-xs font-bold text-slate-900 dark:text-white">45%</span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="size-2 rounded-full bg-purple-500" />
                    <span className="text-xs text-slate-600 dark:text-slate-400 font-medium">Claude 3.5 Sonnet</span>
                  </div>
                  <span className="text-xs font-bold text-slate-900 dark:text-white">28%</span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="size-2 rounded-full bg-amber-500" />
                    <span className="text-xs text-slate-600 dark:text-slate-400 font-medium">Gemini 1.5 Pro</span>
                  </div>
                  <span className="text-xs font-bold text-slate-900 dark:text-white">15%</span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="size-2 rounded-full bg-slate-300" />
                    <span className="text-xs text-slate-600 dark:text-slate-400 font-medium">其他自研模型</span>
                  </div>
                  <span className="text-xs font-bold text-slate-900 dark:text-white">12%</span>
                </div>
              </div>
            </div>
          </div>

          {/* Bottom Lists */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 pb-4">
            {/* New Tenants */}
            <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden flex flex-col">
              <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                <h4 className="text-base font-bold text-slate-900 dark:text-white">最新入驻租户</h4>
                <a className="text-primary text-[10px] font-bold hover:underline uppercase tracking-tight" href="#">查看全部</a>
              </div>
              <div className="divide-y divide-slate-100 dark:divide-slate-800 flex-1">
                <div className="px-6 py-4 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="size-10 rounded-lg bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center text-primary font-bold text-xs">TH</div>
                    <div>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">天行科技 (Tianhang Tech)</p>
                      <p className="text-[10px] text-slate-400 font-medium mt-0.5">入驻时间: 2024-05-31 14:20</p>
                    </div>
                  </div>
                  <span className="px-2 py-0.5 bg-emerald-50 dark:bg-emerald-900/30 text-emerald-600 text-[10px] font-bold rounded">正式版</span>
                </div>
                <div className="px-6 py-4 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="size-10 rounded-lg bg-purple-50 dark:bg-purple-900/20 flex items-center justify-center text-purple-600 font-bold text-xs">LM</div>
                    <div>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">黎明创意工作室</p>
                      <p className="text-[10px] text-slate-400 font-medium mt-0.5">入驻时间: 2024-05-31 11:05</p>
                    </div>
                  </div>
                  <span className="px-2 py-0.5 bg-slate-100 dark:bg-slate-800 text-slate-500 text-[10px] font-bold rounded">试用中</span>
                </div>
                <div className="px-6 py-4 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="size-10 rounded-lg bg-amber-50 dark:bg-amber-900/20 flex items-center justify-center text-amber-600 font-bold text-xs">YX</div>
                    <div>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">云享云端有限公司</p>
                      <p className="text-[10px] text-slate-400 font-medium mt-0.5">入驻时间: 2024-05-30 18:45</p>
                    </div>
                  </div>
                  <span className="px-2 py-0.5 bg-emerald-50 dark:bg-emerald-900/30 text-emerald-600 text-[10px] font-bold rounded">正式版</span>
                </div>
              </div>
            </div>

            {/* Work Orders / Alerts */}
            <div className="bg-white dark:bg-slate-900 rounded-lg border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden flex flex-col">
              <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
                <h4 className="text-base font-bold text-slate-900 dark:text-white">待处理工单 / 系统预警</h4>
                <div className="flex gap-2">
                  <span className="bg-red-50 dark:bg-red-900/30 text-red-600 px-2 py-0.5 rounded-full text-[10px] font-bold">3 紧急</span>
                  <span className="bg-amber-50 dark:bg-amber-900/30 text-amber-600 px-2 py-0.5 rounded-full text-[10px] font-bold">5 待办</span>
                </div>
              </div>
              <div className="divide-y divide-slate-100 dark:divide-slate-800 flex-1">
                <div className="px-6 py-4 flex items-start gap-4 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <div className="p-2 bg-rose-50 dark:bg-rose-900/20 text-rose-600 rounded-lg">
                    <span className="material-symbols-outlined text-lg">error</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-start">
                      <p className="text-sm font-bold text-slate-900 dark:text-white truncate">节点 CPU 负载过高 (HK-Zone-3)</p>
                      <span className="text-[10px] text-slate-400 font-medium flex-shrink-0">10分钟前</span>
                    </div>
                    <p className="text-[11px] text-slate-500 mt-1 line-clamp-1">节点当前 CPU 占用率已持续 5 分钟超过 95%，请及时扩容。</p>
                    <button className="mt-2 text-primary text-[11px] font-bold hover:underline">立即排查</button>
                  </div>
                </div>
                <div className="px-6 py-4 flex items-start gap-4 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <div className="p-2 bg-amber-50 dark:bg-amber-900/20 text-amber-600 rounded-lg">
                    <span className="material-symbols-outlined text-lg">confirmation_number</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-start">
                      <p className="text-sm font-bold text-slate-900 dark:text-white truncate">租户 [天行科技] 提交额度申请</p>
                      <span className="text-[10px] text-slate-400 font-medium flex-shrink-0">1小时前</span>
                    </div>
                    <p className="text-[11px] text-slate-500 mt-1 line-clamp-1">申请上调 GPT-4o 调用额度至 50,000次/日，需审批。</p>
                    <div className="mt-2 flex gap-4">
                      <button className="text-primary text-[11px] font-bold hover:underline">去审批</button>
                      <button className="text-slate-400 text-[11px] font-bold hover:underline">忽略</button>
                    </div>
                  </div>
                </div>
                <div className="px-6 py-4 flex items-start gap-4 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                  <div className="p-2 bg-blue-50 dark:bg-blue-900/20 text-blue-600 rounded-lg">
                    <span className="material-symbols-outlined text-lg">account_balance</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-start">
                      <p className="text-sm font-bold text-slate-900 dark:text-white truncate">本月结算汇率待更新</p>
                      <span className="text-[10px] text-slate-400 font-medium flex-shrink-0">3小时前</span>
                    </div>
                    <p className="text-[11px] text-slate-500 mt-1 line-clamp-1">系统未检测到 6 月份自动汇率同步，请手动核对。</p>
                    <button className="mt-2 text-primary text-[11px] font-bold hover:underline">前往设置</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
