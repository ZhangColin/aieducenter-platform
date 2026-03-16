# 海创元智研云平台 - 开发手册

> 面向开发者：如何在 aieducenter-platform 平台上开发功能
>
> **版本**：v1.0 | **日期**：2026-03-16 | **Epic 状态**：Epic 1（项目骨架）已完成

---

## 目录

- [开发环境准备](#一开发环境准备)
- [项目结构说明](#二项目结构说明)
- [开发流程与规范](#三开发流程与规范)
- [后端开发指南](#四后端开发指南)
- [前端开发指南](#五前端开发指南)
- [API 客户端生成](#六api-客户端生成)
- [测试与质量要求](#七测试与质量要求)
- [常见问题与排错](#八常见问题与排错)
- [开发注意事项](#九开发注意事项)
- [快速参考](#十快速参考)

---

## 一、开发环境准备

### 1.1 必需软件

| 软件 | 版本 | 用途 | 下载地址 |
|------|------|------|----------|
| Java | 21+ | 后端开发 | https://adoptium.net/ |
| Node.js | 20+ | 前端开发 | https://nodejs.org/ |
| pnpm | 9+ | 前端包管理 | `npm install -g pnpm` |
| Docker | 最新版 | 集成测试（Testcontainers） | https://www.docker.com/ |
| IntelliJ IDEA | 最新版 | 后端 IDE（推荐） | https://www.jetbrains.com/idea/ |
| VS Code / Cursor | 最新版 | 前端 IDE | https://code.visualstudio.com/ |

### 1.2 初始化步骤

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

### 1.3 一键启动

```bash
# 同时启动前后端（推荐）
./scripts/dev.sh

# 或分别启动
cd server && ./gradlew bootRun  # 后端（8080）
pnpm dev:web                     # Web 前端（3000）
pnpm dev:admin                   # Admin 前端（3001）
```

---

## 二、项目结构说明

### 2.1 整体结构

```
aieducenter-platform/
├── server/                    # Java 后端
│   ├── src/main/java/com/aieducenter/
│   │   ├── AieduCenterApplication       # 主启动类
│   │   ├── controller/                  # 应用级接口
│   │   ├── config/                      # 配置类
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
├── web/                       # 用户端应用（Next.js）
├── admin/                     # 运营后台应用（Next.js）
│
├── packages/                  # 前端共享包
│   ├── ui/                    # shadcn/ui 组件库
│   ├── api-client/            # OpenAPI 生成的 API 客户端
│   └── shared/                # 共享工具（cn 函数、类型等）
│
├── scripts/                   # 脚本
│   ├── dev.sh                 # 一键启动
│   ├── validate.sh            # 代码验证
│   └── deploy-*.sh            # 部署脚本
│
└── docs/                      # 项目文档
    ├── guides/                # 本指南
    ├── sop/                   # AI 协作开发 SOP
    ├── specs/                 # Epic/Feature 规格
    ├── decisions/             # 架构决策
    └── skills/                # 踩坑经验
```

### 2.2 后端目录结构（单个 Context）

每个限界上下文遵循 DDD 四层架构：

```
{context}/
├── domain/                    # 领域层（核心业务逻辑）
│   ├── model/                 # 实体、值对象
│   ├── service/               # 领域服务
│   └── repository/            # 仓储接口
├── application/               # 应用层（用例编排）
│   ├── command/               # 命令
│   ├── query/                 # 查询
│   └── service/               # 应用服务
├── infrastructure/            # 基础设施层（技术实现）
│   ├── persistence/           # 持久化实现
│   ├── repository/            # 仓储实现
│   └── external/              # 外部服务调用
└── interfaces/                # 接口层（对外接口）
    ├── rest/                  # REST Controller
    └── dto/                   # 数据传输对象
```

### 2.3 前端目录结构

```
web/或admin/
├── src/
│   ├── app/                   # Next.js App Router 页面
│   │   ├── (public)/          # 路由组：公开页面
│   │   ├── (auth)/            # 路由组：需认证页面
│   │   ├── layout.tsx         # 根布局
│   │   └── page.tsx           # 首页
│   ├── components/            # 页面组件
│   └── lib/                   # 工具函数
├── public/                    # 静态资源
├── package.json
├── next.config.ts             # Next.js 配置
├── tailwind.config.ts         # Tailwind 配置
└── tsconfig.json              # TypeScript 配置
```

---

## 三、开发流程与规范

### 3.1 AI 协作开发 SOP

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

### 3.2 代码验证

```bash
# 全量验证
./scripts/validate.sh

# 仅后端
./scripts/validate-backend.sh

# 仅前端
./scripts/validate-frontend.sh
```

### 3.3 Git Hooks

```bash
# 安装 pre-push hook
./scripts/install-git-hooks.sh

# 之后每次 git push 前自动执行验证
```

---

## 四、后端开发指南

### 4.1 创建新的 REST 端点

#### Step 1: 定义接口契约

在 `server/src/main/java/com/aieducenter/{context}/interfaces/rest/` 中创建 Controller：

```java
package com.aieducenter.conversation.interfaces.rest;

import com.cartisan.web.response.ApiResponse;
import com.aieducenter.conversation.interfaces.rest.dto.ConversationDto;
import com.aieducenter.conversation.application.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversations")
@Validated
@Tag(name = "Conversation", description = "对话管理")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    @Operation(summary = "查询对话列表")
    public ApiResponse<PageResponse<ConversationDto>> listConversations(
        @RequestParam(defaultValue = "1") @Min(1) Integer page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        // 实现...
        return ApiResponse.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建对话")
    public ApiResponse<ConversationDto> createConversation(
        @Valid @RequestBody CreateConversationRequest request
    ) {
        // 实现...
        return ApiResponse.ok(response);
    }
}
```

#### Step 2: 定义 DTO

```java
package com.aieducenter.conversation.interfaces.rest.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CreateConversationRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;

    @Size(max = 500, message = "描述长度不能超过500字符")
    private String description;
}
```

#### Step 3: 实现领域逻辑

遵循 DDD 分层架构：

```java
// Domain 层 - 实体
package com.aieducenter.conversation.domain.model;

import com.cartisan.domain.support.Entity;
import com.cartisan.domain.support.Identity;
import lombok.Getter;

@Getter
public class Conversation extends Entity<Conversation, ConversationId> {
    private final ConversationId id;
    private String title;
    private String description;
    private Long tenantId;
    private Long userId;

    public Conversation(ConversationId id, String title, String description, Long tenantId, Long userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tenantId = tenantId;
        this.userId = userId;
    }

    public void updateTitle(String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        this.title = newTitle;
    }
}
```

```java
// Application 层 - 用例
package com.aieducenter.conversation.application;

import com.aieducenter.conversation.domain.model.Conversation;
import com.aieducenter.conversation.domain.repository.ConversationRepository;
import com.aieducenter.conversation.interfaces.rest.dto.CreateConversationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationApplicationService {

    private final ConversationRepository conversationRepository;

    @Transactional
    public ConversationId create(CreateConversationRequest request, Long tenantId, Long userId) {
        Conversation conversation = new Conversation(
            conversationRepository.nextId(),
            request.getTitle(),
            request.getDescription(),
            tenantId,
            userId
        );
        conversationRepository.save(conversation);
        return conversation.getId();
    }
}
```

#### Step 4: 编写测试（TDD）

```java
package com.aieducenter.conversation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationApplicationServiceTest {

    @Test
    void givenValidRequest_whenCreateConversation_thenReturnsId() {
        // Given
        CreateConversationRequest request = new CreateConversationRequest("测试对话", "");

        // When
        ConversationId id = service.create(request, 1L, 1L);

        // Then
        assertThat(id).isNotNull();
    }

    @Test
    void givenBlankTitle_whenCreateConversation_thenThrowsException() {
        // Given
        CreateConversationRequest request = new CreateConversationRequest("", "");

        // When & Then
        assertThatThrownBy(() -> service.create(request, 1L, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("标题不能为空");
    }
}
```

### 4.2 使用 ApiResponse

```java
// 成功响应
return ApiResponse.ok(data);

// 错误响应
return ApiResponse.error(404, "Resource not found");

// 分页响应
return ApiResponse.ok(new PageResponse<>(items, total, page, size));
```

**注意**：`ApiResponse.ok()` 返回的 message 是 "Success"（首字母大写）

### 4.3 参数校验

```java
@RestController
@Validated  // 必须放在类上，@RequestParam 校验才生效
public class MyController {

    @GetMapping("/search")
    public void search(
        @RequestParam @Min(1) Integer page,
        @RequestParam @Max(100) Integer size
    ) {
        // Spring 会先校验，校验失败抛出 ConstraintViolationException
    }
}
```

### 4.4 依赖注入

```java
// 构造器注入（推荐）
@RequiredArgsConstructor
public class MyService {
    private final MyRepository repository;
}

// 或使用 Lombok
@Service
public class MyService {
    private final MyRepository repository;

    public MyService(MyRepository repository) {
        this.repository = repository;
    }
}
```

### 4.5 常用注解

| 注解 | 用途 | 位置 |
|------|------|------|
| `@RestController` | 标识 REST Controller | 类 |
| `@RequestMapping` | 定义基础路径 | 类/方法 |
| `@GetMapping` 等 | HTTP 方法映射 | 方法 |
| `@RequestParam` | 查询参数 | 参数 |
| `@PathVariable` | 路径变量 | 参数 |
| `@RequestBody` | 请求体 | 参数 |
| `@Valid` | 触发校验 | 参数 |
| `@Validated` | 类级别校验 | 类 |
| `@Operation` | Swagger 文档 | 方法 |
| `@Tag` | Swagger 分组 | 类 |

---

## 五、前端开发指南

### 5.1 添加新页面

#### 创建页面文件

```bash
# web/src/app/new-feature/
mkdir -p web/src/app/new-feature
touch web/src/app/new-feature/page.tsx
```

#### 页面代码示例

```tsx
// web/src/app/new-feature/page.tsx
'use client'

import { useState, useEffect } from 'react'
import { api } from '@aieducenter/api-client'
import { Button, Card, CardHeader, CardTitle, CardContent } from '@aieducenter/ui'

export default function NewFeaturePage() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function fetchData() {
      try {
        const { data, error } = await api.GET('/api/v1/conversations', {
          params: { query: { page: 1, size: 20 } }
        })

        if (error) {
          console.error('Failed to fetch:', error)
          return
        }

        setData(data)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  if (loading) return <div>加载中...</div>

  return (
    <div className="container mx-auto py-8">
      <Card>
        <CardHeader>
          <CardTitle>新功能</CardTitle>
        </CardHeader>
        <CardContent>
          {/* 内容 */}
        </CardContent>
      </Card>
    </div>
  )
}
```

### 5.2 使用共享组件

```tsx
import {
  Button,
  Input,
  Label,
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableCell,
  TableBody
} from '@aieducenter/ui'

export function MyComponent() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>标题</CardTitle>
        <CardDescription>描述</CardDescription>
      </CardHeader>
      <CardContent>
        <div>
          <Label>输入框</Label>
          <Input placeholder="请输入..." />
        </div>
      </CardContent>
      <CardFooter>
        <Button>提交</Button>
      </CardFooter>
    </Card>
  )
}
```

### 5.3 使用主题切换

```tsx
'use client'

import { useTheme } from '@aieducenter/ui'
import { Button } from '@aieducenter/ui'

export function ThemeToggle() {
  const { theme, setTheme } = useTheme()

  return (
    <Button
      variant="outline"
      size="icon"
      onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
    >
      {theme === 'dark' ? '🌙' : '☀️'}
    </Button>
  )
}
```

### 5.4 状态管理（Zustand）

```tsx
// 在 shared 包中定义 store
// packages/shared/src/auth-store.ts
import { create } from 'zustand'

interface AuthState {
  accessToken: string | null
  setAccessToken: (token: string) => void
  clearAccessToken: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  setAccessToken: (token) => set({ accessToken: token }),
  clearAccessToken: () => set({ accessToken: null })
}))

// 在组件中使用
import { useAuthStore } from '@aieducenter/shared/auth-store'

export function MyComponent() {
  const accessToken = useAuthStore((state) => state.accessToken)
  const setAccessToken = useAuthStore((state) => state.setAccessToken)

  // ...
}
```

### 5.5 前端路由组

```
web/src/app/
├── (public)/              # 公开页面，无需登录
│   ├── layout.tsx         # 布局（顶部导航）
│   └── page.tsx           # 首页
├── (auth)/                # 需要认证的页面
│   ├── layout.tsx         # 布局
│   └── dashboard/
│       └── page.tsx       # 仪表板
├── login/
│   └── page.tsx           # 登录页
├── layout.tsx             # 根布局
└── not-found.tsx          # 404 页面
```

### 5.6 样式开发

```tsx
// Tailwind CSS 类名
<div className="flex items-center justify-between p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
  <h1 className="text-2xl font-bold text-gray-900 dark:text-white">标题</h1>
</div>

// 使用 cn 函数合并类名
import { cn } from '@aieducenter/shared'

<div className={cn(
  "base-class",
  isActive && "active-class",
  "additional-class"
)} />
```

---

## 六、API 客户端生成

### 6.1 生成步骤

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

### 6.2 使用方式

```typescript
import { api } from '@aieducenter/api-client'
import type { paths } from '@aieducenter/api-client/schema'

// GET 请求
const { data, error } = await api.GET('/api/v1/conversations', {
  params: { query: { page: 1, size: 20 } }
})

// POST 请求
const { data, error } = await api.POST('/api/v1/conversations', {
  body: {
    title: '新对话',
    description: '这是一个测试对话'
  }
})

// 带路径参数
const { data, error } = await api.GET('/api/v1/conversations/{id}', {
  params: { path: { id: '123' } }
})

// 错误处理
if (error) {
  console.error('API Error:', error)
  // 处理错误
  return
}

// 使用 data
console.log(data)
```

### 6.3 生成的文件

| 文件 | 说明 |
|------|------|
| `openapi.json` | 后端 OpenAPI 规范（同步获取） |
| `src/api/schema.ts` | 自动生成的 TypeScript 类型定义 |
| `src/api/client.ts` | API 客户端（带 token 注入和自动刷新） |
| `src/api/types.ts` | 手动补充的类型（ApiResponse、PageResponse 等） |

---

## 七、测试与质量要求

### 7.1 后端测试

```bash
cd server

# 单元测试
./gradlew test

# 集成测试
./gradlew integrationTest

# 测试 + ArchUnit 架构验证
./gradlew check

# 变异测试（Phase 5 必跑，杀死率 ≥ 70%）
./gradlew pitest
```

#### 测试命名规范

```
given_{条件}_when_{操作}_then_{预期结果}
```

示例：
```java
@Test
void givenInsufficientCoins_whenChat_thenThrowsInsufficientBalanceException() {
    // Given
    Tenant tenant = createTenantWithCoins(10);

    // When
    Throwable thrown = catchException(() -> service.chat(tenant, request));

    // Then
    assertThat(thrown)
        .isInstanceOf(InsufficientBalanceException.class)
        .hasMessageContaining("余额不足");
}
```

#### 使用 AssertJ

```java
import static org.assertj.core.api.Assertions.assertThat;

// 链式调用，语义清晰
.assertThat(actual)
    .isNotNull()
    .hasFieldOrPropertyWithValue("id", expectedId);

// 集合断言
.assertThat(list)
    .hasSize(3)
    .containsExactly("a", "b", "c")
    .doesNotContain("d");
```

### 7.2 前端测试

```bash
# API 客户端测试
cd packages/api-client
pnpm test           # 运行测试
pnpm typecheck      # 类型检查

# 全量类型检查
pnpm -r typecheck
```

### 7.3 质量门禁

- **前端**：所有测试通过，类型检查无错误
- **后端**：所有测试通过，ArchUnit 通过，PIT 杀死率 ≥ 70%

---

## 八、常见问题与排错

### 8.1 cartisan-boot 依赖找不到

**现象**：`Cannot access cartisan-xxx`

**解决**：
1. 确保 cartisan-boot 在 `../cartisan-boot/` 目录
2. 检查 `server/settings.gradle.kts` 中的 `includeBuild` 路径

```kotlin
// server/settings.gradle.kts
rootProject.name = "aieducenter-server"
includeBuild("../../cartisan-boot")  // 确保路径正确
```

### 8.2 前端 workspace 包导入报错

**现象**：`Cannot find module @aieducenter/ui`

**解决**：
1. 确保 `pnpm install` 已执行
2. 检查 `next.config.ts` 中的 `transpilePackages` 配置

```ts
// next.config.ts
const nextConfig: NextConfig = {
  transpilePackages: [
    '@aieducenter/ui',
    '@aieducenter/api-client',
    '@aieducenter/shared'
  ]
}
```

3. 检查 `tsconfig.json` 中的 `paths` 配置

### 8.3 API 客户端生成失败

**现象**：`pnpm gen:api` 报错

**解决**：
1. 确保 `openapi.json` 文件存在（运行 `pnpm sync:openapi`）
2. 确保后端服务正在运行

### 8.4 Tailwind 样式不生效

**解决**：检查应用层 `tailwind.config.ts` 中的 `content` 配置

```ts
// web/tailwind.config.ts 或 admin/tailwind.config.ts
content: [
  './src/**/*.{ts,tsx}',
  '../../packages/ui/src/**/*.{ts,tsx}',
],
```

同时确保有 `postcss.config.mjs`：

```js
// web/postcss.config.mjs 或 admin/postcss.config.mjs
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
};
```

### 8.5 后端测试报数据库连接失败

**解决**：确保 Docker 正在运行，Testcontainers 需要 Docker 启动容器

```bash
# 检查 Docker 状态
docker ps

# 如果 Docker 未运行，启动 Docker Desktop
```

### 8.6 端口被占用

```bash
# 查看端口占用
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# 杀死进程
kill -9 <PID>
```

---

## 九、开发注意事项

本节从 [SKILL.md](../skills/SKILL.md) 中提取关键规则。

### 9.1 代码风格

| 规则 | 说明 |
|------|------|
| STYLE-001 | 领域接口应包含完整 JavaDoc 和使用示例 |
| STYLE-002 | JavaDoc 中必须转义 HTML 特殊字符（`&lt;`、`&gt;`） |
| STYLE-003 | 使用 Record 实现 ValueObject 和 Identity |

### 9.2 前端开发

| 规则 | 说明 | 记忆口诀 |
|------|------|----------|
| FRONT-000 | 前后端联调用 Next.js rewrites 代理，避免 CORS | 前端调后端？rewrites 代理跑 |
| FRONT-001 | Next.js 配置 transpilePackages 转译 workspace 包 | workspace 包导入源码？transpilePackages 必配 |
| FRONT-007 | shadcn add 组件导入路径需手动修复 | shadcn 组件导入？执行后先修路径 |
| FRONT-008 | packages/ui 必须导出 CSS 文件 | 共享包导 CSS？exports 字段要配 |
| FRONT-011 | Next.js 应用需有独立的 Tailwind 配置 | Tailwind 不生效？查 content 配置和 postcss.config.mjs |
| FRONT-013 | Tailwind CSS 需固定版本 v3 | shadcn/ui 组件？Tailwind CSS 锁 v3 |
| FRONT-014 | Next.js 15 + ESLint 10 需配置 .eslintrc.json | Next.js 跑 lint？先装 ESLint 再配 eslintrc |

### 9.3 Spring Web

| 规则 | 说明 |
|------|------|
| WEB-000 | `ApiResponse.ok()` 返回的 message 是 "Success"（首字母大写） |
| WEB-002 | `@Validated` 必须放在类上触发 `@RequestParam` 校验 |
| WEB-003 | `@Component` 默认 bean 名称可能与自动配置冲突，需显式指定 |

---

## 十、快速参考

### 10.1 常用命令

| 操作 | 命令 |
|------|------|
| 启动前后端 | `./scripts/dev.sh` |
| 验证代码 | `./scripts/validate.sh` |
| 后端编译 | `cd server && ./gradlew build` |
| 后端启动 | `cd server && ./gradlew bootRun` |
| 后端测试 | `cd server && ./gradlew test` |
| Web 启动 | `pnpm dev:web` |
| Admin 启动 | `pnpm dev:admin` |
| 生成 API 客户端 | `cd packages/api-client && pnpm gen:api` |
| 同步 OpenAPI | `cd packages/api-client && pnpm sync:openapi` |
| 前端类型检查 | `pnpm -r typecheck` |

### 10.2 端口分配

| 服务 | 端口 |
|------|------|
| 后端 API | 8080 |
| Web 前端 | 3000 |
| Admin 前端 | 3001 |
| PostgreSQL | 5432 |
| Redis | 6379 |

### 10.3 相关文档

| 文档 | 说明 |
|------|------|
| [使用手册](aieducenter-platform-使用手册.md) | 面向用户/运维的功能说明 |
| [cartisan-boot 使用手册](cartisan-boot-使用手册.md) | 后端框架详细文档 |
| [AI 协作开发 SOP](../sop/AI协作开发SOP.md) | 完整开发流程规范 |
| [团队规则库](../skills/SKILL.md) | 踩坑经验、编码规范 |
| [架构决策记录](../decisions/DECISIONS.md) | 技术决策原因 |

### 10.4 Epic 完成状态

| Epic | 名称 | 状态 |
|------|------|------|
| Epic 1 | 项目骨架 | ✅ 已完成 |
| Epic 2 | 用户与登录 | 规划中 |
| Epic 3 | 企业管理 | 规划中 |
| Epic 4 | AI 对话 | 规划中 |
| Epic 5 | 智能体 | 规划中 |
| Epic 6 | 创作工具 | 规划中 |
| Epic 7 | 计费系统 | 规划中 |
| Epic 8 | 平台运营 | 规划中 |
