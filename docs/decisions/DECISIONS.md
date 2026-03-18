# 架构决策记录（ADR）

> 本文档记录 aieducenter-platform 项目的重要架构决策。
> cartisan-boot 框架的决策见：[cartisan-boot/docs/decisions/DECISIONS.md](../../cartisan-boot/docs/decisions/DECISIONS.md)

---

## ADR-001：采用虚拟币计费体系

- **日期**：2026-03-15
- **决策**：用户充值人民币 → 获得虚拟币（"智慧币"）→ 使用 AI 能力消耗虚拟币
- **理由**：
  - 统一计费：不同模型的 Token 计费方式差异大，虚拟币作为中间层简化前端展示
  - 汇率灵活：平台可根据上游成本调整各模型汇率，保持虚拟币购买力稳定
  - 企业控制：企业可为成员设置配额，无需实际扣费
- **计费公式**：`消耗虚拟币 = (input_tokens × 输入汇率 + output_tokens × 输出汇率)`
- **替代方案**：直接人民币计费（汇率波动影响用户体验）

---

## ADR-002：租户是计费主体

- **日期**：2026-03-15
- **决策**：虚拟币余额挂在租户下，而非用户个人
- **理由**：
  - 企业场景：企业统一充值，成员按配额使用
  - 个人用户：自动创建单人租户，语义一致
  - 财务清晰：一个发票主体 = 一个租户
- **替代方案**：用户个人余额（企业场景需复杂的报销流程）

---

## ADR-003：计费采用预扣+实扣模式

- **日期**：2026-03-15
- **决策**：
  1. 请求前：预估最大消耗，Redis 原子操作预扣
  2. 请求中：流式输出，累计实际 token
  3. 请求后：Redis 原子操作多退少补
- **理由**：
  - 预扣防超支：避免用户透支
  - 实扣保准确：流式输出结束前无法精确计算 token
  - Redis 原子操作：保证并发安全
- **替代方案**：事后扣费（可能透支）或固定扣费（不准确）

---

## ADR-004：前端采用 Next.js App Router

- **日期**：2026-03-15
- **决策**：前端使用 Next.js 15 App Router + shadcn/ui + Tailwind CSS
- **理由**：
  - SSR 支持落地推广页面（SEO 优化）
  - Vercel AI SDK 原生支持流式对话（`useChat` hook）
  - shadcn/ui 源码可控，AI 生成代码质量高
  - 后台和前台统一一套组件
- **替代方案**：React SPA（SEO 不利）或 Nuxt（团队不熟悉）

---

## ADR-005：前后端类型对齐用 OpenAPI

- **日期**：2026-03-15
- **决策**：后端 SpringDoc 输出 OpenAPI 3.1，前端自动生成 TypeScript 客户端
- **理由**：
  - 前后端类型安全对齐
  - 后端改接口前端编译期报错
  - AI 生成代码有完整类型提示
- **替代方案**：手写类型（易不一致）或 tRPC（后端需改动大）

---

## ADR-006：AI 对话上下文存储在服务端

- **日期**：2026-03-15
- **决策**：会话消息存储在后端，前端只做展示
- **理由**：
  - 多端同步：用户换设备，对话历史保留
  - 计费审计：消耗流水与消息记录对应
  - 合规要求：内容审计、数据留存
- **替代方案**：纯本地存储（多端不同步）

---

## ADR-007：智能体采用配置化而非硬编码

- **日期**：2026-03-15
- **决策**：智能体的 System Prompt、工具、参数配置存储在数据库
- **理由**：
  - 平台运营可调整智能体行为，无需发版
  - 支持用户自定义智能体
  - A/B 测试不同 Prompt 效果
- **替代方案**：硬编码（灵活度差）

---

## ADR-008：消息队列初期用 Redis Streams

- **日期**：2026-03-15
- **决策**：计费事件、异步任务初期用 Redis Streams，规模增长后迁移到 Kafka
- **理由**：
  - Redis Streams 无额外组件，开发期简化环境
  - 迁移成本低：封装消息发布接口
  - Kafka 成本高，小规模用不上
- **迁移触发条件**：消息积压 > 10万条 或 消费延迟 > 5秒

---

## ADR-009：web 和 admin 应用分离

- **日期**：2026-03-15
- **决策**：用户端（web）和平台运营后台（admin）分离为两个 Next.js 应用
- **理由**：
  - 权限隔离：运营后台功能不应暴露给普通用户
  - 独立部署：运营后台流量小，可部署在内部网络
  - UI 差异：后台重数据展示，前台重交互体验
- **共享**：通过 packages/ui 和 packages/api-client 共享代码

---

## ADR-010：会话标题自动生成

- **日期**：2026-03-15
- **决策**：会话创建时暂无标题，首条消息完成后调用 LLM 生成标题
- **理由**：
  - 用户不需要手动输入标题
  - 标题反映对话内容，方便历史查找
  - 异步生成不阻塞对话流程
- **Prompt 模板**："用不超过 10 个字概括以下对话的主题：{第一条用户消息}"

---

## ADR-011：cartisan-boot 通过 Composite Build 引用

- **日期**：2026-03-15
- **决策**：开发期通过 Gradle Composite Build 引用本地 cartisan-boot，而非 Maven Local
- **理由**：
  - 修改 cartisan-boot 后无需重新发布，aieducenter-platform 自动获取最新代码
  - IntelliJ IDEA 可同时导航到两个项目，调试方便
  - 两个项目保持独立的版本控制
- **配置**：`server/settings.gradle.kts` 中 `includeBuild("../../cartisan-boot")`
- **替代方案**：Maven Local（每次修改框架后需手动 publish，容易遗忘）

---

## ADR-012：Spring Boot 依赖直接声明而非 Platform BOM

- **日期**：2026-03-15
- **决策**：`build.gradle.kts` 中直接添加 `spring-boot-starter-web`，而非通过 `implementation(platform("com.cartisan:cartisan-dependencies"))`
- **理由**：
  - cartisan-dependencies 是 `java-platform` 类型，与 Spring Boot 插件的 runtime classpath 解析有冲突
  - 直接声明依赖更明确，避免 Composite Build 中的 variant 匹配问题
- **影响**：F01-01 实现方式与设计文档 02_interface.md 有差异，但功能等效
- **风险**：如需统一管理依赖版本，需后续调整

---

## ADR-013：前端 Monorepo 采用 pnpm workspace

- **日期**：2026-03-16
- **决策**：前端使用 pnpm workspace 管理 Monorepo，包含 web、admin 应用和 ui/api-client/shared 共享包
- **理由**：
  - pnpm workspace 是轻量级 Monorepo 方案，配置简单
  - workspace:* 协议自动链接本地包，开发期无需手动更新
  - 发布时自动替换为实际版本号
  - 与 TypeScript paths 配合实现类型安全的包引用
- **配置**：
  - `pnpm-workspace.yaml` 定义 workspace 成员
  - `tsconfig.base.json` 统一管理 TypeScript 配置和 paths 别名
  - `transpilePackages` 确保 Next.js 转译 workspace 包源码
- **替代方案**：npm workspaces（慢）、yarn workspaces（不使用 hoisting）
- **相关 Feature**：F01-04

---

## ADR-014：共享包空壳占位，延迟引入依赖

- **日期**：2026-03-16
- **决策**：F01-04 阶段 packages/ui/api-client/shared 仅创建包结构，不引入 shadcn/ui 等依赖
- **理由**：
  - F01-04 验收标准为 "Monorepo 可运行"
  - shadcn/ui 集成属于 F01-06 职责
  - 保持 Feature 边界清晰，符合 SOP 原子化原则
- **影响**：packages/ui 的 React peerDependencies 留到 F01-06 添加
- **相关 Feature**：F01-04、F01-06

---

## ADR-015：共享 UI 组件库采用 shadcn/ui New York 风格

- **日期**：2026-03-16
- **决策**：packages/ui 基于 shadcn/ui 构建，采用 New York 风格、CSS 变量主题系统
- **理由**：
  - shadcn/ui 使用"复制到项目"模式，组件源码可控，AI 理解度高
  - New York 风格现代简洁，偏方角设计，适合 AI 对话类产品
  - CSS 变量主题系统便于亮/暗模式切换
  - 扁平导出方式，使用简洁：`import { Button } from '@aieducenter/ui'`
- **实现细节**：
  - cn 工具函数放在 packages/shared，全项目复用
  - ThemeProvider 基于 next-themes，使用 class 切换模式
  - exports 字段同时导出 TypeScript 和 CSS
- **相关 Feature**：F01-06
- **修复记录**：
  - tsconfig.json 需添加 `jsx: "react-jsx"` 配置
  - shadcn add 命令可能将组件安装到字面路径 `@/components/`，需移动到正确位置
  - 组件内 `@/lib` 导入需改为相对路径 `../lib/utils`
  - **F01-06 实现时发现**：应用层需有独立的 Tailwind 配置
    - `web/src/app/globals.css`：包含 `@tailwind` 指令和 CSS 变量
    - `web/tailwind.config.ts`：content 需包含 `./src` 和 `../../packages/ui/src`
    - `web/postcss.config.mjs`：必须是 `.mjs` 格式（Next.js 15 要求）
    - `darkMode` 配置用字符串 `'class'` 而非数组 `['class']`

---

## ADR-016：API 客户端 401 响应不自动重试

- **日期**：2026-03-16
- **决策**：401 响应时触发 token 刷新，但不自动重试原请求，由调用方处理
- **理由**：
  - 简化实现：自动重试需要处理幂等性、请求去重等复杂问题
  - 调用方控制：调用方可根据请求类型决定是否重试
  - 避免无限循环：防止 401 → 刷新 → 401 的循环
- **相关 Feature**：F01-07
- **替代方案**：自动重试（复杂度高，暂不实现）

---

## ADR-017：AuthStore 分离 setAccessToken 和 clearAccessToken

- **日期**：2026-03-16
- **决策**：AuthStore 提供两个方法：`setAccessToken(token: string)` 和 `clearAccessToken()`
- **理由**：
  - 意图明确：设置 token 和清除 token 是两个不同的语义
  - 类型安全：setAccessToken 只接受非空字符串，避免 null 误用
  - 便于未来扩展：clearAccessToken 可扩展清除更多状态（如用户信息）
- **相关 Feature**：F01-07

---

## ADR-018：前端使用 vitest + happy-dom 测试

---

## ADR-019：前端使用 Next.js rewrites 代理 API，避免 CORS

- **日期**：2026-03-16
- **决策**：前端通过 Next.js rewrites 代理 `/api/*` 请求到后端，而非直接跨域访问
- **理由**：
  - 开发期避免 CORS 问题：前端和后端同源，无需配置 CORS
  - 生产环境灵活：可通过环境变量 `BACKEND_URL` 配置后端地址
  - 安全性提升：后端不需要暴露给所有源，只接受来自同域的请求
- **配置**：
  ```ts
  // next.config.ts
  export async function rewrites() {
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080'
    return [
      {
        source: '/api/:path*',
        destination: `${backendUrl}/api/:path*`,
      },
    ]
  }
  ```
- **前端调用**：使用相对路径 `/api/health` 而非 `http://localhost:8080/api/health`
- **替代方案**：后端配置 CORS（需要配置允许的源，生产环境复杂）
- **相关 Feature**：F01-08

- **日期**：2026-03-16
- **决策**：前端单元测试使用 vitest 框架 + happy-dom 环境
- **理由**：
  - vitest 与 Vite 生态无缝集成，配置简单
  - happy-dom 模拟浏览器环境（window、document），比 JSDOM 更快
  - ESM 原生支持，无需额外配置
- **相关 Feature**：F01-07
- **配置**：vitest.config.ts 中设置 `environment: 'happy-dom'`

---

## ADR-020：User 聚合根使用 BCrypt 加密密码

- **日期**：2026-03-18
- **Epic**：Epic 002 - 用户与登录
- **Feature**：F02-01
- **决策**：
  - 密码使用 BCrypt 算法加密存储，strength=10
  - 提供 `matchesPassword(String plainPassword)` 方法验证密码
  - 密码字段不在数据库设 UNIQUE 约束（允许不同用户使用相同密码）
- **理由**：
  - BCrypt 是行业标准的密码哈希算法，自动加盐，抗彩虹表攻击
  - strength=10 平衡安全性和性能（约 100ms 验证时间）
  - 不存储明文密码，避免数据泄露时密码暴露
- **替代方案**：
  - PBKDF2（Java 内置，但需要额外配置迭代次数）
  - Argon2（更安全，但 Spring Security 默认不支持）
  - SHA-256（不安全，缺乏盐值管理）
- **相关文件**：`User.java:47`, `User.java:180-182`

---

## ADR-021：User 聚合根直接使用 JPA 注解，无需适配器

- **日期**：2026-03-18
- **Epic**：Epic 002 - 用户与登录
- **Feature**：F02-01
- **决策**：User 聚合根直接添加 `@Entity`、`@Table`、`@Column` 等 JPA 注解，不创建独立的 JpaUser 适配器类
- **理由**：
  - 简化架构：减少一层转换，代码更直观
  - User 聚合根字段简单，无复杂嵌套，污染风险低
  - cartisan-boot 的 SoftDeletable 已处理审计字段（createdAt/updatedAt）
  - Spring Data JPA 可直接返回实体，无需手动转换
- **替代方案**：JpaUser 适配器模式（解耦更彻底，但增加复杂度）
- **风险**：如 User 聚合根变得复杂，未来可重构为适配器模式
- **相关文件**：`User.java:43-68`

---

## ADR-022：三种登录凭证（用户名必填，邮箱/手机号可选）

- **日期**：2026-03-18
- **Epic**：Epic 002 - 用户与登录
- **Feature**：F02-01
- **决策**：
  - `username`：必填，主登录凭证
  - `email`：可选，辅助登录凭证
  - `phoneNumber`：可选，辅助登录凭证
  - 三者在数据库中 UNIQUE 约束，软删除后可复用
- **理由**：
  - 用户名必填：确保系统有唯一标识，便于用户间交互（如 @username）
  - 邮箱/手机号可选：降低注册门槛，支持匿名/测试用户
  - 支持多种登录方式：满足不同用户偏好
- **替代方案**：
  - 单一登录凭证（灵活性差）
  - 邮箱必填（提高注册门槛，部分用户无邮箱）
- **唯一性校验分层**：
  - 格式校验：值对象构造函数（Domain 层）
  - 唯一性校验：Repository.exists 方法（Infrastructure 层），由应用层调用
- **相关文件**：`User.java:52-59`, `SpringDataJpaUserRepository.java:22-38`

---

## ADR-023：值对象使用 Record 实现自封装校验

- **日期**：2026-03-18
- **Epic**：Epic 002 - 用户与登录
- **Feature**：F02-01
- **决策**：Username、Email、PhoneNumber 使用 Java Record 实现值对象，在 compact constructor 中进行格式校验
- **理由**：
  - Record 天然不可变，符合值对象语义
  - compact constructor 语法简洁，校验逻辑集中
  - 实现 `ValueObject<T>` 接口，提供 `sameValueAs()` 方法
  - 校验失败抛出 `DomainException`（通过 `Assertions.require`），领域语义明确
- **替代方案**：
  - 传统 class（代码冗长）
  - Lombok @Value（需要额外依赖）
- **相关文件**：`Username.java`, `Email.java`, `PhoneNumber.java`

---

## ADR-024：nickname 空值时默认使用 username

- **日期**：2026-03-18
- **Epic**：Epic 002 - 用户与登录
- **Feature**：F02-01
- **决策**：创建用户时，若 nickname 为 null/空字符串，默认设置为 username
- **理由**：
  - UI 显示需要有友好的名称，不能总是显示 "user_123"
  - 减少用户输入步骤，提升注册体验
  - 用户可后续修改为真实昵称
- **影响**：
  - 无法区分"用户显式设置 nickname=username"和"使用默认值"
  - 如未来需要"清空昵称"功能，需修改此逻辑
- **相关文件**：`User.java:82-87`
