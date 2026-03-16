# 海创元智研云平台 (aieducenter-platform)

> AI 聚合 SaaS 平台 — 接入各类大模型，提供智能体服务，做好费用管理

## 项目简介

海创元智研云平台是一个面向学校和企业的 AI 聚合 SaaS 平台，核心价值：

1. **统一入口**：接入各类大模型（文字对话、生图、生音视频、AI 编程），用户无需分别注册各厂商
2. **智能体**：提供预制和自定义智能体（PPT 生成、文档分析等），处理复杂 AI 任务
3. **费用管理**：虚拟币体系 + 各模型汇率，用户充值后按量使用，企业可控制成员配额
4. **多租户**：支持企业/机构入驻，成员管理，权限配置

## 技术栈

### 后端
- **框架基座**：cartisan-boot（DDD、Web、Security、Data、AI、Event、Test）
- **消息队列**：Redis Streams → Kafka
- **定时任务**：Spring Scheduler → XXL-Job
- **对象存储**：cartisan-storage（阿里云 OSS / MinIO）
- **支付**：cartisan-payment（微信支付 / 支付宝）
- **缓存**：Redis

### 前端
- **框架**：Next.js 15（App Router）
- **UI 组件**：shadcn/ui + Tailwind CSS
- **状态管理**：Zustand
- **API 通信**：OpenAPI 自动生成的 TypeScript 客户端
- **Monorepo**：pnpm workspace

## 快速开始

### 环境要求
- Java 21
- Node.js 20+
- pnpm
- Docker（集成测试）

### 安装依赖
```bash
# 安装前端依赖
pnpm install
```

### 运行项目
```bash
# 启动前端应用（用户端）
pnpm dev:web

# 启动前端应用（管理后台）
pnpm dev:admin

# 启动后端（需要先配置 cartisan-boot）
cd server && ./gradlew bootRun
```

## 项目结构

```
aieducenter-platform/
├── server/          # Java 后端
├── web/             # 用户端 + 企业管理（Next.js）
├── admin/           # 平台运营后台（Next.js）
├── packages/        # 前端共享代码
│   ├── ui/          # 共享 UI 组件
│   ├── api-client/  # OpenAPI 生成的客户端
│   └── shared/      # 共享工具函数、类型
└── docs/            # 项目文档
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

## 文档

- [开发指南](docs/guide/development-guide.md) — 开发环境配置、工作流程、编码规范
- [项目设计](docs/海创元智研云平台设计.md) — 技术架构、业务模型、Epic 拆分
- [架构决策](docs/decisions/DECISIONS.md) — 技术决策记录
- [团队规则](docs/skills/SKILL.md) — 踩坑经验、铁律

## 开发流程

本项目遵循 [AI 协作开发 SOP](docs/sop/AI协作开发SOP.md)：

- Phase 0：Epic 分解（只产出文档，不写代码）
- Phase 1-3：需求 → 接口设计 → 实现方案（只产出文档）
- Phase 4：先写测试（红）→ 再写实现（绿）
- Phase 5：Review + 验证 + 归档

## 质量门禁

- 遵循 `docs/skills/SKILL.md` 中的所有规则
- 测试命名规范：`given_{条件}_when_{操作}_then_{预期结果}`
- 使用 AssertJ 而非 JUnit 断言
- Phase 5 审查时执行 PIT 变异测试（杀死率 ≥ 70%）

## 许可证

Copyright © 2025 海创元
