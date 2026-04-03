# aieducenter-platform

海创元智研云平台，AI 聚合 SaaS 平台。统一入口接入各类大模型，提供智能体、虚拟币计费、多租户管理。基于 cartisan-boot 框架开发。

## 技术栈

### 后端
- Java 21 / Spring Boot 3.4.x / Maven
- 基座框架：cartisan-boot（DDD、Web、Security、Data、AI、Event、Test）→ [使用手册](docs/guide/cartisan-boot-使用手册.md)
- 缓存/消息：Redis（余额缓存、限流、会话上下文、Streams 消息队列）

### 前端
- Next.js 15（App Router）/ shadcn/ui + Tailwind CSS / Zustand
- Monorepo：pnpm workspace

## 架构约束

- DDD 六边形架构，领域层零外部依赖
- 限界上下文之间通过领域事件通信，禁止直接跨上下文调用
- 所有金额/虚拟币使用 BigDecimal / long，禁止浮点数
- 构造函数注入，禁止 @Autowired 字段注入

## 编码规范

- DTO 使用 Java Record，构造函数校验不变量
- 测试命名：given_{条件}_when_{操作}_then_{预期结果}
- 测试使用 AssertJ，禁止无意义断言
- 踩坑经验见 docs/PITFALLS.md，遇到相关问题时查阅

## 常用命令

> **注意：** 所有 Maven 命令需在 `server/` 子目录下执行。

- 编译：`cd server && mvn compile`
- 单元测试（无需 Docker）：`cd server && mvn test`
- 指定测试：`cd server && mvn test -Dtest=XxxTest`
- 全量检查（含 ArchUnit）：`cd server && mvn verify`
- 变异测试：`cd server && mvn org.pitest:pitest-maven:mutationCoverage`
- 前端开发：`pnpm dev`

## 开发流程

使用 Superpowers 技能驱动开发，按需求规模分层：

- **大需求**：先充分讨论，产出需求设计文档（含 Epic 拆解），再逐个 Epic 推进
- **Epic / 中需求**：讨论后产出 Backlog 文档（含 Feature 拆解），再逐个 Feature 推进
- **Feature / 小需求 / Bug**：直接用 Superpowers 技能（brainstorming -> writing-plans -> TDD -> verification）

阶段性完成后人工触发归档：提取有价值内容到 docs/guide/，然后清理过程文档。
