# aieducenter-platform — AI 协作上下文

## 项目是什么

海创元智研云平台是一个 **AI 聚合 SaaS 平台**，面向学校和企业，核心价值：

1. **统一入口**：接入各类大模型（文字对话、生图、生音视频、AI 编程），用户无需分别注册各厂商
2. **智能体**：提供预制和自定义智能体（PPT 生成、文档分析等），处理复杂 AI 任务
3. **费用管理**：虚拟币体系 + 各模型汇率，用户充值后按量使用，企业可控制成员配额
4. **多租户**：支持企业/机构入驻，成员管理，权限配置

## 与 cartisan-boot 的关系

本项目基于 [cartisan-boot](../cartisan-boot/) 框架开发：

- cartisan-boot 提供 DDD 基建、Web 规范、安全抽象、数据层封装、大模型调用封装等技术基础设施
- aieducenter-platform 在此之上实现所有业务逻辑
- 两个项目并行开发，开发期通过 Gradle Composite Build 联调

## 技术栈

### 后端

- **框架基座**：cartisan-boot（DDD、Web、Security、Data、AI、Event、Test）
- **消息队列**：Redis Streams（初期）→ Kafka（规模增长后）
- **定时任务**：Spring Scheduler（初期）→ XXL-Job（规模增长后）
- **对象存储**：cartisan-storage（阿里云 OSS / MinIO）
- **支付**：cartisan-payment（微信支付 / 支付宝）
- **缓存**：Redis（余额缓存、限流、会话上下文缓存）

### 前端

- **框架**：Next.js 15（App Router）
- **UI 组件**：shadcn/ui + Tailwind CSS
- **状态管理**：Zustand
- **API 通信**：OpenAPI 自动生成的 TypeScript 客户端
- **Monorepo**：pnpm workspace

## 关键文档

- `docs/海创元智研云平台设计.md` — 项目完整蓝图（技术架构、业务模型、Epic 拆分）
- `docs/decisions/DECISIONS.md` — 架构决策记录
- `docs/skills/SKILL.md` — 团队规则库（踩坑经验、铁律）
- `docs/specs/` — 各 Epic 的规格文档

## 项目结构

```
aieducenter-platform/
├── server/                              # Java 后端
│   └── src/main/java/com/aieducenter/
│       ├── account/                     # Account Context
│       ├── tenant/                      # Tenant Context
│       ├── gateway/                     # AI Gateway Context
│       ├── conversation/               # Conversation Context
│       ├── billing/                     # Billing Context
│       ├── agent/                       # Agent Context
│       ├── creative/                    # Creative Context
│       └── admin/                       # Platform Admin Context
│
├── web/                                 # 用户端 + 企业管理（Next.js）
├── admin/                               # 平台运营后台（Next.js）
├── packages/                            # 前端共享代码
│   ├── ui/                              # 共享 UI 组件
│   ├── api-client/                      # OpenAPI 生成的客户端
│   └── shared/                          # 共享工具函数、类型
└── docs/                                # 项目文档
```

## 限界上下文

| 上下文 | 核心职责 |
|--------|---------|
| **Account** | 用户注册、登录、个人信息、API Key 管理 |
| **Tenant** | 租户/企业管理、成员管理、角色权限、成员配额 |
| **AI Gateway** | 模型路由策略、请求调度、限流降级、调用日志 |
| **Billing** | 虚拟币余额、充值/退款、消耗计费、汇率管理 |
| **Conversation** | 会话列表、消息存储、上下文窗口管理 |
| **Agent** | 智能体定义与配置、工具注册、执行引擎、多步编排 |
| **Creative** | 生图、生音视频、AI 编程等非对话类 AI 能力 |
| **Platform Admin** | 模型/Provider 管理、定价汇率、运营数据、审计 |

## AI 协作规范

所有开发遵循 cartisan-boot 的 `docs/sop/AI协作开发SOP.md`：

- Phase 0：Epic 分解（只产出文档，不写代码）
- Phase 1-3：需求 → 接口设计 → 实现方案（只产出文档）
- Phase 4：先写测试（红）→ 再写实现（绿）
- Phase 5：Review + ArchUnit + PIT

**任务粒度：单次实现 50-150 行代码。超过则继续拆分。**

## 开发环境要求

### 后端

- Java 21
- Docker（集成测试需要，Testcontainers）

### 前端

- Node.js 20+
- pnpm

## 质量门禁

- 遵循 `docs/skills/SKILL.md` 中的所有规则
- 测试命名规范：`given_{条件}_when_{操作}_then_{预期结果}`
- 使用 AssertJ 而非 JUnit 断言
- Phase 5 审查时执行 PIT 变异测试（杀死率 ≥ 70%）
