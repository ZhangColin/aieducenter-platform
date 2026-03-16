# 海创元智研云平台 — 开发指南

> 版本：v1.0 | 日期：2026-03-16

## 目录

- [环境准备](#环境准备)
- [项目结构](#项目结构)
- [开发工作流](#开发工作流)
- [前端开发](#前端开发)
- [后端开发](#后端开发)
- [测试规范](#测试规范)
- [常见问题](#常见问题)

---

## 环境准备

### 必需软件

| 软件 | 版本 | 用途 |
|------|------|------|
| Java | 21+ | 后端开发 |
| Node.js | 20+ | 前端开发 |
| pnpm | 9+ | 前端包管理 |
| Docker | 最新版 | 集成测试 |
| IntelliJ IDEA | 最新版 | 后端 IDE |
| VS Code / Cursor | 最新版 | 前端 IDE |

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd aieducenter-platform
   ```

2. **安装前端依赖**
   ```bash
   pnpm install
   ```

3. **配置后端**
   - 确保 cartisan-boot 项目与本项目在同一父目录
   - `server/settings.gradle.kts` 已配置 `includeBuild("../../cartisan-boot")`

4. **启动开发服务器**
   ```bash
   # 前端（用户端）
   pnpm dev:web

   # 前端（管理后台）
   pnpm dev:admin

   # 后端
   cd server && ./gradlew bootRun
   ```

---

## 项目结构

```
aieducenter-platform/
├── server/                    # Java 后端
│   ├── src/main/java/com/aieducenter/
│   │   ├── account/           # 账户上下文
│   │   ├── tenant/            # 租户上下文
│   │   ├── gateway/           # AI 网关上下文
│   │   ├── conversation/      # 对话上下文
│   │   ├── billing/           # 计费上下文
│   │   ├── agent/             # 智能体上下文
│   │   ├── creative/          # 创作上下文
│   │   └── admin/             # 平台管理上下文
│   └── build.gradle.kts
│
├── web/                       # 用户端 Next.js
├── admin/                     # 管理后台 Next.js
│
├── packages/                  # 前端共享包
│   ├── ui/                    # 共享 UI 组件
│   ├── api-client/            # OpenAPI 客户端
│   └── shared/                # 共享工具函数
│
├── docs/                      # 项目文档
│   ├── sop/                   # AI 协作开发 SOP
│   ├── specs/                 # Epic/Feature 规格
│   ├── decisions/             # 架构决策记录
│   └── skills/                # 团队规则库
│
├── guides/                    # 本开发指南
├── CLAUDE.md                  # AI 协作上下文
└── README.md                  # 项目概览
```

---

## 开发工作流

### SOP Phase 流程

本项目遵循 **AI 协作开发 SOP**，所有 Feature 按以下流程开发：

```
Phase 0: Epic 拆解
    ↓
Phase 1: Research → 01_requirement.md
    ↓
Phase 2: Design → 02_interface.md
    ↓
Phase 3: Plan → 03_implementation.md
    ↓
Phase 4: Execute → 代码 + 测试（TDD 红绿循环）
    ↓
Phase 5: Review → 04_test_spec.md + 归档
```

**核心原则**：
- Phase 1-3 只产出文档，不写代码
- Phase 4 先写测试（红灯），再写实现（绿灯）
- 任务粒度：单次 50-150 行代码

### 前端开发流程

1. **启动开发服务器**
   ```bash
   pnpm dev:web    # 或 pnpm dev:admin
   ```

2. **创建新功能**
   - 遵循 TDD：先写测试，再写实现
   - 使用 `@aieducenter/ui` 组件
   - 通过 `@aieducenter/api-client` 调用后端

3. **类型检查与测试**
   ```bash
   cd packages/<package-name>
   pnpm typecheck
   pnpm test
   ```

### 后端开发流程

1. **启动后端**
   ```bash
   cd server
   ./gradlew bootRun
   ```

2. **创建新功能**
   - 遵循 DDD 分层架构
   - 先写测试（红灯），再写实现（绿灯）
   - 运行 ArchUnit 验证架构规则

3. **验证**
   ```bash
   ./gradlew test        # 单元测试
   ./gradlew check       # 测试 + ArchUnit
   ./gradlew pitest      # 变异测试（Phase 5）
   ```

---

## 前端开发

### Monorepo 使用

```bash
# 添加依赖到 web
pnpm --filter web add <package>

# 添加依赖到共享包
pnpm --filter @aieducenter/ui add <package>

# 运行特定包的命令
pnpm --filter @aieducenter/api-client test
```

### 共享 UI 组件

```tsx
import { Button } from '@aieducenter/ui'

export function MyComponent() {
  return <Button>点击我</Button>
}
```

### API 客户端

```typescript
import { api } from '@aieducenter/api-client'

// 自动类型推断，后端改接口前端编译期报错
const response = await api.GET('/api/v1/conversations')
```

### 状态管理

```typescript
import { useAuthStore } from '@aieducenter/shared/auth-store'

export function MyComponent() {
  const accessToken = useAuthStore(state => state.accessToken)
  // ...
}
```

### 样式规范

- 使用 Tailwind CSS 工具类
- 遵循 shadcn/ui 设计规范
- 亮/暗模式通过 `next-themes` 切换

---

## 后端开发

### DDD 分层架构

```
├── domain/         # 领域层（核心业务逻辑）
│   ├── model/      # 聚合根、实体、值对象
│   ├── service/    # 领域服务
│   └── repository/# 仓储接口
├── application/    # 应用层（用例编排）
│   ├── command/    # 命令
│   ├── query/      # 查询
│   └── handler/    # 命令/查询处理器
├── infrastructure/ # 基础设施层（技术实现）
│   ├── repository/ # 仓储实现
│   ├── client/     # 外部 API 客户端
│   └── config/     # 配置
└── interfaces/     # 接口层（Web API）
    └── rest/       # REST Controller
```

### 架构规则

- 领域层不依赖基础设施层
- 只有聚合根可以拥有 Repository
- 限界上下文之间通过领域事件通信

### 测试规范

```java
@Test
void givenValidRequest_whenCreateConversation_thenReturnsId() {
    // Given
    // ...

    // When
    // ...

    // Then
    assertThat(actual).isNotNull();
}
```

---

## 测试规范

### 前端测试

```bash
# 运行所有测试
pnpm --filter @aieducenter/api-client test

# 监听模式
pnpm --filter @aieducenter/ui test --watch

# 覆盖率报告
pnpm --filter @aieducenter/api-client test --coverage
```

### 后端测试

```bash
# 单元测试
./gradlew test

# 集成测试（需要 Docker）
./gradlew integrationTest

# 全量检查
./gradlew check

# 变异测试（Phase 5 必跑）
./gradlew pitest
```

### 质量门禁

- **前端**：所有测试通过，类型检查无错误
- **后端**：所有测试通过，ArchUnit 通过，PIT 杀死率 ≥ 70%

---

## 常见问题

### Q1：cartisan-boot 依赖找不到

**原因**：cartisan-boot 未在同一父目录，或 Composite Build 未配置。

**解决**：
1. 确保 cartisan-boot 与 aieducenter-platform 在同一父目录
2. 检查 `server/settings.gradle.kts` 中的 `includeBuild` 路径

### Q2：前端 workspace 包导入报错

**原因**：Next.js 未配置 `transpilePackages`。

**解决**：检查 `next.config.ts` 中是否包含：
```ts
transpilePackages: ['@aieducenter/ui', '@aieducenter/api-client', '@aieducenter/shared']
```

### Q3：Tailwind 样式不生效

**原因**：应用层 Tailwind 配置缺少 `content` 配置。

**解决**：在应用层 `tailwind.config.ts` 中添加：
```ts
content: [
  './src/**/*.{ts,tsx}',
  '../../packages/ui/src/**/*.{ts,tsx}',
],
```

### Q4：后端测试报 "数据库连接失败"

**原因**：集成测试需要 Docker 启动 Testcontainers。

**解决**：
1. 确保 Docker 正在运行
2. 或只运行单元测试：`./gradlew test --exclude '*IntegrationTest'`

### Q5：vitest 报错 "Cannot find module"

**原因**：vitest.config.ts 未配置正确。

**解决**：确保配置了 `test.match` 和 `environment`：
```ts
export default defineConfig({
  test: {
    environment: 'happy-dom',
    include: ['src/**/*.test.{ts,tsx}'],
  },
})
```

---

## 参考文档

- [AI 协作开发 SOP](../docs/sop/AI协作开发SOP.md) — 完整开发流程规范
- [团队规则库](../docs/skills/SKILL.md) — 踩坑经验、铁律
- [架构决策记录](../docs/decisions/DECISIONS.md) — 技术决策
- [项目设计](../docs/海创元智研云平台设计.md) — 技术架构、业务模型
