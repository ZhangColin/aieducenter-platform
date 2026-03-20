export default function HomePage() {
  return (
    <div className="relative flex min-h-screen w-full flex-col overflow-x-hidden bg-background-light dark:bg-background-dark text-slate-900 dark:text-slate-100 font-display">
      {/* Navigation */}
      <header className="sticky top-0 z-50 w-full border-b border-slate-200 bg-white/80 backdrop-blur-md px-6 md:px-20 lg:px-40 py-4">
        <div className="mx-auto flex max-w-[1280px] items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-transparent">
              <img alt="Logo" className="h-full w-full object-contain" src="/logo-light-200.png" />
            </div>
            <h2 className="text-xl font-bold tracking-tight text-slate-900">海创元智研云平台</h2>
          </div>
          <nav className="hidden lg:flex items-center gap-10">
            <a className="text-sm font-semibold text-slate-600 hover:text-primary transition-colors" href="#">解决方案</a>
            <a className="text-sm font-semibold text-slate-600 hover:text-primary transition-colors" href="#">核心功能</a>
            <a className="text-sm font-semibold text-slate-600 hover:text-primary transition-colors" href="#">价格方案</a>
            <a className="text-sm font-semibold text-slate-600 hover:text-primary transition-colors" href="#">关于我们</a>
          </nav>
          <div className="flex items-center gap-3">
            <button
              aria-label="Toggle Theme"
              className="flex h-10 w-10 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 hover:text-primary transition-all"
            >
              <span className="material-symbols-outlined">light_mode</span>
            </button>
            <a href="/login" className="hidden sm:flex h-10 items-center justify-center rounded-lg px-5 text-sm font-bold text-slate-700 hover:bg-slate-100 transition-colors">
              登录
            </a>
            <a href="/login" className="flex h-10 items-center justify-center rounded-lg bg-primary px-6 text-sm font-bold text-white shadow-lg shadow-primary/20 hover:bg-primary/90 transition-all">
              立即开始
            </a>
          </div>
        </div>
      </header>

      <main className="flex-1">
        {/* Hero Section */}
        <section className="px-6 md:px-20 lg:px-40 py-16 md:py-24">
          <div className="mx-auto max-w-[1280px]">
            <div className="flex flex-col items-center gap-12 lg:flex-row lg:items-center">
              <div className="flex flex-1 flex-col gap-8 text-center lg:text-left">
                <div className="inline-flex w-fit self-center lg:self-start rounded-full bg-primary/10 px-4 py-1.5 text-sm font-semibold text-primary">
                  下一代 AI 集成平台
                </div>
                <h1 className="text-4xl font-black leading-[1.2] tracking-tight text-slate-900 md:text-5xl lg:text-6xl">
                  面向教育与企业的<br /><span className="text-primary">统一 AI 智研入口</span>
                </h1>
                <p className="max-w-2xl text-lg leading-relaxed text-slate-600 lg:text-xl">
                  集成文本、图像、音视频及 AI 编程的多模态能力，为科研与商业创新提供一站式智研解决方案。
                </p>
                <div className="flex flex-wrap justify-center gap-4 lg:justify-start">
                  <button className="h-14 min-w-[180px] rounded-xl bg-primary px-8 text-base font-bold text-white shadow-xl shadow-primary/25 hover:translate-y-[-2px] transition-all">
                    免费开始使用
                  </button>
                  <button className="h-14 min-w-[180px] rounded-xl border border-slate-200 bg-white px-8 text-base font-bold text-slate-700 hover:bg-slate-50 transition-all">
                    预约演示
                  </button>
                </div>
              </div>
              <div className="flex flex-1 justify-center lg:justify-end">
                <div className="relative h-[400px] w-full max-w-[600px] overflow-hidden rounded-2xl bg-slate-200 shadow-2xl lg:h-[500px]">
                  <div className="absolute inset-0 bg-gradient-to-tr from-primary/20 to-transparent"></div>
                  <img className="h-full w-full object-cover" alt="Modern AI interface dashboard with data visualizations" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAOGycSFIE_f4yCzs9TZmBZNwTzvZGET5MHs5xcpDLrLq-uRJaJPxhRt1p5naLv8Mk6hWJxQZe__KLUFkisg7jfBcBE7lBvKr0Uzi1bso8-wpJ0DjmDymwe_Jibg-AnzPxfa61AgCEpx2W7UEy1Pjau_WXmgXYtRZrotyB8-diubYNK8Nulq1cGk2eiFjClHy9YVgxg-GqaiqN_x1Fu16kIkRtYyXooIncWtG2ERsGYtSgGGrssNXD3_ljqO5rzlnyUKA_X1WLV4W4"/>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Core Value Prop */}
        <section className="bg-white px-6 md:px-20 lg:px-40 py-20 border-y border-slate-100">
          <div className="mx-auto max-w-[1280px]">
            <div className="mb-16 text-center">
              <h2 className="text-3xl font-bold tracking-tight text-slate-900 md:text-4xl">核心 AI 能力</h2>
              <p className="mt-4 text-slate-600">强大的工具集成，打造无缝的工作流程</p>
            </div>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {/* Card 1 */}
              <div className="group flex flex-col gap-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-8 transition-all hover:border-primary/30 hover:bg-white hover:shadow-xl hover:shadow-primary/5">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-primary shadow-sm group-hover:bg-primary group-hover:text-white transition-all">
                  <span className="material-symbols-outlined">forum</span>
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-900">文字对话</h3>
                  <p className="mt-2 text-sm leading-relaxed text-slate-600">先进的大语言模型对话，辅助研究、写作与支持。</p>
                </div>
              </div>

              {/* Card 2 */}
              <div className="group flex flex-col gap-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-8 transition-all hover:border-primary/30 hover:bg-white hover:shadow-xl hover:shadow-primary/5">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-primary shadow-sm group-hover:bg-primary group-hover:text-white transition-all">
                  <span className="material-symbols-outlined">palette</span>
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-900">生图/生音视频</h3>
                  <p className="mt-2 text-sm leading-relaxed text-slate-600">创意视觉内容生成，助力营销与教育内容创作。</p>
                </div>
              </div>

              {/* Card 3 */}
              <div className="group flex flex-col gap-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-8 transition-all hover:border-primary/30 hover:bg-white hover:shadow-xl hover:shadow-primary/5">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-primary shadow-sm group-hover:bg-primary group-hover:text-white transition-all">
                  <span className="material-symbols-outlined">terminal</span>
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-900">AI 编程</h3>
                  <p className="mt-2 text-sm leading-relaxed text-slate-600">智能代码辅助与调试，专为开发者与学生设计。</p>
                </div>
              </div>

              {/* Card 4 */}
              <div className="group flex flex-col gap-4 rounded-2xl border border-slate-100 bg-slate-50/50 p-8 transition-all hover:border-primary/30 hover:bg-white hover:shadow-xl hover:shadow-primary/5">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-primary shadow-sm group-hover:bg-primary group-hover:text-white transition-all">
                  <span className="material-symbols-outlined">smart_toy</span>
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-900">智能体</h3>
                  <p className="mt-2 text-sm leading-relaxed text-slate-600">自主任务工作流，轻松处理复杂的业务逻辑。</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Target Users Section */}
        <section className="px-6 md:px-20 lg:px-40 py-24">
          <div className="mx-auto max-w-[1280px]">
            <div className="mb-16 max-w-2xl">
              <h2 className="text-3xl font-bold tracking-tight text-slate-900 md:text-4xl">赋能多样化应用场景</h2>
              <p className="mt-6 text-lg text-slate-600">无论您是学术机构还是成长型企业，我们的平台都能根据您的特定组织需求进行扩展。</p>
            </div>
            <div className="grid grid-cols-1 gap-12 lg:grid-cols-2">
              {/* Education */}
              <div className="flex flex-col gap-6">
                <div className="aspect-video w-full overflow-hidden rounded-2xl bg-slate-100">
                  <img className="h-full w-full object-cover" alt="University students collaborating in a modern high-tech classroom" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDmweaO-Ftlx2ftAJ9y6Q26H1HhYJ2nAVECL_KGvGl-0xP7dd-e7x-Cmv5WNaz_xK-PU1PcqpOhIP55kNJP8fkaTfQZz_ZC6bqEKVGDw8Aty06y8myv_TMGjdCDFwC92kW39MxX5iifE3h_QuKORpLPdE1PMQFIo7f1hNf9jG83Fk38WAsfEOkjSILjD0ONAAPn62YyrppC4avlomRh4oxvi7-c-df-TAolo6W1tcNuEf0RiQ2l3ZA7XOAgXkpni9MWvQar9wWUV-c"/>
                </div>
                <div className="space-y-3">
                  <h3 className="text-2xl font-bold text-slate-900">教育科研机构</h3>
                  <p className="text-slate-600 leading-relaxed">为学校和研究实验室提供中心化的 AI 资源、协作工具以及面向师生的伦理化 AI 沙箱。</p>
                  <ul className="mt-4 space-y-2">
                    <li className="flex items-center gap-2 text-sm font-medium text-slate-700">
                      <span className="material-symbols-outlined text-primary text-xl">check_circle</span>
                      学术研究额度支持
                    </li>
                    <li className="flex items-center gap-2 text-sm font-medium text-slate-700">
                      <span className="material-symbols-outlined text-primary text-xl">check_circle</span>
                      学生工作区管理系统
                    </li>
                  </ul>
                </div>
              </div>

              {/* Enterprise */}
              <div className="flex flex-col gap-6">
                <div className="aspect-video w-full overflow-hidden rounded-2xl bg-slate-100">
                  <img className="h-full w-full object-cover" alt="Clean minimal corporate office interior with glass walls" src="https://lh3.googleusercontent.com/aida-public/AB6AXuC4wOdS7e8U2TEM2prolXdUQP5Vfo7uV_LJ7jh8sc3xQ46oQTuPqJ77zgy2ak1W7ymyug0LlJzKVLu3-Bvlp_izag6uEN-mPVWfbIZ-VaEwBSmbwu49wFH9LEn3OBAsVJAWKrM0bjxdtfbZaDfFU2YufvKpx3O98UCSHxzsv5_YrC_njcwNRmAzaE_gKXqq5RFFoi6iuxEElyyvspYqrycoBaUWlWxznbqyTCdr9mpCDDpFXJaFlpM_Ck_yO27VxW_QSpfpqYk8FNc"/>
                </div>
                <div className="space-y-3">
                  <h3 className="text-2xl font-bold text-slate-900">企业解决方案</h3>
                  <p className="text-slate-600 leading-relaxed">通过安全、可扩展的 AI 部署优化业务单元。定制化工作流设计，旨在提升生产力与创新产出。</p>
                  <ul className="mt-4 space-y-2">
                    <li className="flex items-center gap-2 text-sm font-medium text-slate-700">
                      <span className="material-symbols-outlined text-primary text-xl">check_circle</span>
                      企业级安全防护体系
                    </li>
                    <li className="flex items-center gap-2 text-sm font-medium text-slate-700">
                      <span className="material-symbols-outlined text-primary text-xl">check_circle</span>
                      专属 API 技术支持
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Final CTA */}
        <section className="px-6 md:px-20 lg:px-40 py-20">
          <div className="mx-auto max-w-[1280px] rounded-[2rem] bg-primary p-12 text-center text-white md:p-20 shadow-2xl shadow-primary/20">
            <h2 className="text-3xl font-black md:text-5xl">准备好开启您的智能研发之旅了吗？</h2>
            <p className="mx-auto mt-6 max-w-xl text-lg text-white/80">加入数百家已在海创元智研云平台构建未来的机构与企业。</p>
            <div className="mt-10 flex flex-wrap justify-center gap-4">
              <button className="h-14 min-w-[200px] rounded-xl bg-white px-8 text-base font-bold text-primary hover:bg-slate-50 transition-all">
                免费开始体验
              </button>
              <button className="h-14 min-w-[200px] rounded-xl border border-white/30 bg-white/10 px-8 text-base font-bold text-white hover:bg-white/20 transition-all">
                联系销售
              </button>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-slate-900 px-6 md:px-20 lg:px-40 py-16 text-slate-400">
        <div className="mx-auto max-w-[1280px]">
          <div className="grid grid-cols-1 gap-12 lg:grid-cols-4">
            <div className="col-span-1 lg:col-span-1">
              <div className="flex items-center gap-3 text-white">
                <div className="flex h-8 w-8 items-center justify-center rounded bg-transparent">
                  <span className="text-lg font-bold">AI</span>
                </div>
                <span className="text-lg font-bold">海创元智研</span>
              </div>
              <p className="mt-6 text-sm leading-relaxed">
                引领中国教育与企业领域的 AI 转型浪潮。
              </p>
            </div>

            <div>
              <h4 className="text-sm font-bold uppercase tracking-wider text-white">产品平台</h4>
              <ul className="mt-6 space-y-4 text-sm">
                <li><a className="hover:text-primary transition-colors" href="#">解决方案</a></li>
                <li><a className="hover:text-primary transition-colors" href="#">功能特性</a></li>
                <li><a className="hover:text-primary transition-colors" href="#">集成生态</a></li>
                <li><a className="hover:text-primary transition-colors" href="#">API 文档</a></li>
              </ul>
            </div>

            <div>
              <h4 className="text-sm font-bold uppercase tracking-wider text-white">关于公司</h4>
              <ul className="mt-6 space-y-4 text-sm">
                <li><a className="hover:text-primary transition-colors" href="#">关于我们</a></li>
                <li><a className="hover:text-primary transition-colors" href="#">加入我们</a></li>
                <li><a className="hover:text-primary transition-colors" href="#">隐私政策</a></li>
                <li><a className="hover:text-primary transition-colors" href="#">服务协议</a></li>
              </ul>
            </div>

            <div>
              <h4 className="text-sm font-bold uppercase tracking-wider text-white">联系我们</h4>
              <div className="mt-6 flex gap-4">
                <a className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-800 text-white hover:bg-primary transition-all" href="#">
                  <span className="material-symbols-outlined text-xl">share</span>
                </a>
                <a className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-800 text-white hover:bg-primary transition-all" href="#">
                  <span className="material-symbols-outlined text-xl">mail</span>
                </a>
                <a className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-800 text-white hover:bg-primary transition-all" href="#">
                  <span className="material-symbols-outlined text-xl">public</span>
                </a>
              </div>
            </div>
          </div>

          <div className="mt-16 border-t border-slate-800 pt-8 text-center text-xs">
            <p>© 2024 海创元智研云平台. 版权所有.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
