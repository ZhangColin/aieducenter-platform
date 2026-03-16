# 海创元智研云平台 — 开发指南

> 面向开发者：如何在 aieducenter-platform 平台上开发功能

## 目录

- [开发环境准备](#开发环境准备)
- [项目结构说明](#项目结构说明)
- [功能开发流程](#功能开发流程)
- [API 客户端生成](#api-客户端生成)
- [前端页面开发](#前端页面开发)
- [后端接口开发](#后端接口开发)
- [测试与质量要求](#测试与质量要求)
- [常见问题](#常见问题)

---

## 开发环境准备

### 必需软件

| 软件 | 版本 | 用途 |
|------|------|------|
| Java | 21+ | 后端开发 |
| Node.js | 20+ | 前端开发 |
| pnpm | 9+ | 前端包管理 |
| Docker | 最新版 | 集成测试（Testcontainers） |
| IntelliJ IDEA | 最新版 | 后端 IDE（推荐） |
| VS Code / Cursor | 最新版 | 前端 IDE |

### 初始化步骤

```bash
# 1. 克隆项目（确保 cartisan-boot 在同一父目录）
git clone <repository-url>
cd aieducenter-platform

# 2. 安装前端依赖
pnpm install

# 3. 验证环境
pnpm typecheck        # TypeScript 类型检查
cd server && ./gradlew check  # 后端编译+测试
```

---

## 项目结构说明

```
aieducenter-platform/
├── server/                    # Java 后端
│   ├── src/main/java/com/aieducenter/
│   │   ├── account/           # 账户上下文（注册、登录、个人信息）
│   │   ├── tenant/            # 租户上下文（企业管理、成员、权限）
│   │   ├── gateway/           # AI 网关上下文（模型路由、调用）
│   │   ├── conversation/      # 对话上下文（会话、消息）
│   │   ├── billing/           # 计费上下文（虚拟币、充值、消耗）
│   │   ├── agent/             # 智能体上下文（智能体配置、执行）
│   │   ├── creative/          # 创作上下文（生图、生音视频）
│   │   └── admin/             # 平台管理上下文（模型管理、运营数据）
│   └── build.gradle.kts
│
├── web/                       # 用户端应用（Next.js）
├── admin/                     # 运营后台应用（Next.js）
│
├── packages/                  # 前端共享包
│   ├── ui/                    # shadcn/ui 组件库
│   ├── api-client/            # OpenAPI 生成的 API 客户端
│   └── shared/                # 共享工具（cn 函数、类型等）
│
└── docs/                      # 项目文档
    ├── guides/                # 本指南
    ├── sop/                   # AI 协作开发 SOP
    ├── specs/                 # Epic/Feature 规格
    ├── decisions/             # 架构决策
    └── skills/                # 踩坑经验
```

---

## 功能开发流程

### SOP Phase 概览

所有新功能遵循 **AI 协作开发 SOP**：

```
Phase 0: Epic 拆解（大需求拆小）
    ↓
Phase 1: Research → 需求澄清
    ↓
Phase 2: Design → 接口设计
    ↓
Phase 3: Plan → 实施计划
    ↓
Phase 4: Execute → TDD 开发
    ↓
Phase 5: Review → 审查归档
```

**核心原则**：
- Phase 1-3 只产出文档，不写代码
- Phase 4 先写测试（红灯），再写实现（绿灯）
- 单次任务粒度：50-150 行代码

详见：[AI 协作开发 SOP](../sop/AI协作开发SOP.md)

---

## API 客户端生成

当后端添加或修改 API 后，需要同步更新前端 TypeScript 类型定义。

### 前置条件

后端服务正在运行：
```bash
cd server && ./gradlew bootRun
# 确保后端启动在 http://localhost:8080
```

### 生成步骤

```bash
cd packages/api-client

# 1. 同步 OpenAPI schema（推荐）
pnpm sync:openapi
# 从 http://localhost:8080/v3/api-docs 获取最新 schema

# 2. 生成 TypeScript 类型
pnpm gen:api
# 根据 openapi.json 生成 src/api/schema.ts

# 3. 验证类型检查
pnpm typecheck
```

### 生成的文件

| 文件 | 说明 |
|------|------|
| `openapi.json` | 后端 OpenAPI 规范（同步获取） |
| `src/api/schema.ts` | 自动生成的 TypeScript 类型定义 |
| `src/api/client.ts` | API 客户端（带 token 注入和自动刷新） |
| `src/api/types.ts` | 手动补充的类型（ApiResponse、PageResponse 等） |

### 使用方式

```typescript
import { api } from '@aieducenter/api-client'
import type { paths } from '@aieducenter/api-client/schema'

// 类型安全的 API 调用
const response = await api.GET('/api/v1/conversations', {
  params: { query: { page: 1, size: 20 } }
})

// 后端改接口？前端编译期报错，及时发现问题！
```

---

## 前端页面开发

### 添加新页面（用户端）

1. **创建页面文件**
   ```bash
   # web/src/app/new-feature/
   mkdir -p web/src/app/new-feature
   touch web/src/app/new-feature/page.tsx
   ```

2. **使用共享组件**
   ```tsx
   // web/src/app/new-feature/page.tsx
   import { Button } from '@aieducenter/ui'
   import { api } from '@aieducenter/api-client'
   import { useAuthStore } from '@aieducenter/shared/auth-store'

   export default function NewFeaturePage() {
     const accessToken = useAuthStore(state => state.accessToken)

     const handleClick = async () => {
       const response = await api.POST('/api/v1/endpoint')
       // ...
     }

     return <Button onClick={handleClick}>点击我</Button>
   }
   ```

3. **添加导航（如需要）**
   - 修改 `web/src/app/layout.tsx` 中的导航菜单

### 添加共享 UI 组件

如果新组件需要在多个应用中复用：

```bash
cd packages/ui
npx shadcn-ui@latest add button  # 示例：添加按钮组件
```

---

## 后端接口开发

### 添加新的 REST 端点

#### 1. 定义接口契约

在 `server/src/main/java/com/aieducenter/{context}/interfaces/rest/` 中创建 Controller：

```java
@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Conversation", description = "对话管理")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    @Operation(summary = "查询对话列表")
    public ApiResponse<PageResponse<ConversationDto>> listConversations(
        @RequestParam(defaultValue = "1") @Min(1) Integer page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        // 实现...
    }
}
```

#### 2. 实现领域逻辑

遵循 DDD 分层架构：

```
interfaces/rest/          # Controller 层
    ↓
application/            # Application 层（用例）
    ↓
domain/                 # Domain 层（核心逻辑）
    ↓
infrastructure/         # Infrastructure 层（持久化）
```

#### 3. 编写测试（TDD）

```java
class ConversationServiceTest {
    @Test
    void givenValidRequest_whenCreateConversation_thenReturnsId() {
        // Given
        // ...

        // When
        // ...

        // Then
        assertThat(actual).isNotNull();
    }
}
```

#### 4. 验证架构规则

```bash
cd server
./gradlew check    # 包含 ArchUnit 架构验证
```

### DDD 分层规范

| 层级 | 职责 | 约束 |
|------|------|------|
| **domain** | 核心业务逻辑 | 不依赖任何外部框架 |
| **application** | 用例编排 | 可依赖 domain |
| **infrastructure** | 技术实现 | 可依赖 domain |
| **interfaces** | 对外接口 | 可依赖 application |

---

## 测试与质量要求

### 前端测试

```bash
# API 客户端测试
cd packages/api-client
pnpm test           # 运行测试
pnpm typecheck      # 类型检查
```

### 后端测试

```bash
cd server
./gradlew test           # 单元测试
./gradlew check          # 测试 + ArchUnit
./gradlew pitest         # 变异测试（Phase 5 必跑，杀死率 ≥ 70%）
```

### 质量门禁

- **前端**：所有测试通过，类型检查无错误
- **后端**：所有测试通过，ArchUnit 通过，PIT 杀死率 ≥ 70%
- **命名规范**：`given_{条件}_when_{操作}_then_{预期结果}`

---

## 常见问题

### Q1：cartisan-boot 依赖找不到

**现象**：`Cannot access cartisan-xxx`

**解决**：
1. 确保 cartisan-boot 在 `../cartisan-boot/` 目录
2. 检查 `server/settings.gradle.kts` 中的 `includeBuild` 路径

### Q2：前端 workspace 包导入报错

**现象**：`Cannot find module @aieducenter/ui`

**解决**：
1. 确保 `pnpm install` 已执行
2. 检查 `next.config.ts` 中的 `transpilePackages` 配置
3. 检查 `tsconfig.json` 中的 `paths` 配置

### Q3：API 客户端生成失败

**现象**：`pnpm gen:api` 报错

**解决**：
1. 确保 `openapi.json` 文件存在（运行 `pnpm sync:openapi`）
2. 确保后端服务正在运行

### Q4：Tailwind 样式不生效

**解决**：检查应用层 `tailwind.config.ts` 中的 `content` 配置是否包含 packages 路径

### Q5：后端测试报数据库连接失败

**解决**：确保 Docker 正在运行，Testcontainers 需要 Docker 启动容器

---

## 参考文档

| 文档 | 说明 |
|------|------|
| [cartisan-boot 使用手册](cartisan-boot-使用手册.md) | 后端框架详细文档 |
| [AI 协作开发 SOP](../sop/AI协作开发SOP.md) | 完整开发流程规范 |
| [团队规则库](../skills/SKILL.md) | 踩坑经验、编码规范 |
| [架构决策记录](../decisions/DECISIONS.md) | 技术决策原因 |
