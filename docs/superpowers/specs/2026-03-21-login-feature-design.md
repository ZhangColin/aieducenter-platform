# 登录功能设计文档

> 日期：2026-03-21
> 状态：设计阶段
> 关联 Epic：Epic 2 - F02-05（后端）、F02-10（前端）

---

## 一、背景

当前系统后端已实现登录相关 API，前端注册页面已完成，但登录页面仅有 UI 缺少 API 集成。本设计旨在打通前后端，实现完整的登录功能。

**现状：**
- ✅ 后端密码登录 API 已实现
- ✅ 后端短信验证码登录 API 已实现
- ✅ 前端注册页面已完成（含 API 调用）
- ⚠️ 前端登录页面仅 UI，缺少 API 调用
- ⚠️ 登录接口缺少验证码保护

---

## 二、目标

1. **前端登录页面与后端 API 打通**
2. **添加登录验证码保护**（防止暴力破解）
3. **提供良好的用户体验**（加载状态、错误提示、登录成功跳转）

---

## 三、架构设计

### 3.1 前端分层架构

```
┌─────────────────────────────────────────────────────────┐
│  web/src/app/(auth)/login/page.tsx                       │
│  - UI 渲染                                               │
│  - 用户交互                                              │
│  - 表单验证                                              │
├─────────────────────────────────────────────────────────┤
│  packages/shared/src/hooks/use-login.ts                  │
│  - 登录状态管理                                          │
│  - API 调用协调                                          │
│  - 错误处理                                              │
├─────────────────────────────────────────────────────────┤
│  packages/api-client/src/auth/login.ts                  │
│  - 密码登录 API                                          │
│  - 短信验证码登录 API                                    │
│  - 获取验证码 API                                        │
├─────────────────────────────────────────────────────────┤
│  packages/shared/src/auth-store.ts                      │
│  - Token 存储                                            │
│  - 用户状态                                              │
│  - 登录/登出方法                                         │
└─────────────────────────────────────────────────────────┘
```

### 3.2 后端调整

**新增验证码要求：**

登录接口需要增加图形验证码校验，修改如下：

```java
// 密码登录
public record LoginByPasswordCommand(
    @NotBlank String account,
    @NotBlank String password,
    @NotBlank String captchaId,      // 新增
    @NotBlank String captchaCode     // 新增
) {}

// 短信验证码登录
public record LoginBySmsCommand(
    @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") String phone,
    @NotBlank @Size(min = 6, max = 6) String code,
    @NotBlank String captchaId,      // 新增
    @NotBlank String captchaCode     // 新增
) {}
```

---

## 四、接口设计

### 4.1 后端接口

| 接口 | 方法 | 请求体 | 响应 |
|------|------|--------|------|
| `/api/account/login` | POST | `{account, password, captchaId, captchaCode}` | `{data: {token}}` |
| `/api/account/login/sms` | POST | `{phone, code, captchaId, captchaCode}` | `{data: {token}}` |
| `/api/captcha` | GET | - | `{data: {captchaId, image}}` |
| `/api/account/verification-code/sms` | POST | `{phone, purpose, captchaId, captchaCode}` | `{data: {expireSeconds, cooldownSeconds}}` |

### 4.2 错误码

| 错误码 | HTTP状态 | 说明 |
|--------|----------|------|
| USER_011 | 401 | 账号不存在 |
| USER_012 | 401 | 账号或密码错误 |
| CAPTCHA_001 | 400 | 图形验证码错误或已过期 |

---

## 五、前端实现

### 5.1 登录页面状态

```typescript
interface LoginState {
  // UI 状态
  loginType: 'account' | 'sms'
  showPassword: boolean
  showCaptcha: boolean
  countdown: number

  // 表单数据
  account: string
  password: string
  phone: string
  smsCode: string
  captchaId: string
  captchaCode: string
  captchaUrl: string

  // 加载与错误
  isLoading: boolean
  error: string
}
```

### 5.2 登录流程

```
┌──────────────┐
│  用户打开页面  │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  获取图形验证码│
└──────┬───────┘
       │
       ▼
┌──────────────┐     ┌──────────────┐
│  密码登录     │     │  短信验证码登录│
│  - 输入账号   │     │  - 输入手机号 │
│  - 输入密码   │     │  - 获取验证码 │
│  - 输入验证码 │     │  - 输入验证码 │
└──────┬───────┘     └──────┬───────┘
       │                     │
       └──────────┬──────────┘
                  │
                  ▼
         ┌──────────────┐
         │  调用登录API  │
         └──────┬───────┘
                │
         ┌──────┴──────┐
         ▼             ▼
    ┌─────────┐   ┌─────────┐
    │  成功    │   │  失败    │
    │ 存储Token│   │ 显示错误 │
    │ 跳转首页  │   │ 刷新验证码│
    └─────────┘   └─────────┘
```

### 5.3 API 客户端 (packages/api-client/src/auth/login.ts)

```typescript
import { api } from '../api/client'

export interface LoginByPasswordParams {
  account: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface LoginBySmsParams {
  phone: string
  code: string
  captchaId: string
  captchaCode: string
}

export interface LoginResponse {
  token: string
}

export interface CaptchaResponse {
  captchaId: string
  image: string
}

export async function loginByPassword(params: LoginByPasswordParams): Promise<LoginResponse> {
  const { data, error } = await api.POST('/api/account/login', {
    body: params
  })
  if (error) throw error
  return data as LoginResponse
}

export async function loginBySms(params: LoginBySmsParams): Promise<LoginResponse> {
  const { data, error } = await api.POST('/api/account/login/sms', {
    body: params
  })
  if (error) throw error
  return data as LoginResponse
}

export async function getCaptcha(): Promise<CaptchaResponse> {
  const { data, error } = await api.GET('/api/captcha')
  if (error) throw error
  return data as CaptchaResponse
}
```

### 5.4 登录 Hook (packages/shared/src/hooks/use-login.ts)

```typescript
import { useState, useCallback } from 'react'
import { useAuthStore } from '../auth-store'
import { loginByPassword, loginBySms, getCaptcha } from '@aieducenter/api-client/auth/login'

export function useLogin() {
  const { login } = useAuthStore()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleLogin = useCallback(async (params: LoginParams) => {
    setIsLoading(true)
    setError(null)

    try {
      const response = params.type === 'password'
        ? await loginByPassword(params)
        : await loginBySms(params)

      login(response.token, { userId: '', nickname: '', avatar: null })
      return { success: true }
    } catch (err: any) {
      const message = err.error?.message || '登录失败，请重试'
      setError(message)
      return { success: false, error: message }
    } finally {
      setIsLoading(false)
    }
  }, [login])

  return { handleLogin, isLoading, error }
}
```

---

## 六、后端实现

### 6.1 修改 DTO

```java
// packages/server/src/main/java/com/aieducenter/account/application/dto/LoginByPasswordCommand.java
public record LoginByPasswordCommand(
    @NotBlank String account,
    @NotBlank String password,
    @NotBlank String captchaId,
    @NotBlank String captchaCode
) {}
```

### 6.2 修改 AppService

```java
// packages/server/src/main/java/com/aieducenter/account/application/AccountLoginAppService.java

public LoginResult loginByPassword(LoginByPasswordCommand command) {
    // 1. 验证图形验证码
    captchaAppService.verifyCaptcha(command.captchaId(), command.captchaCode());

    // 2. 查找用户
    User user = userRepository.findByUsername(command.account())
        .or(() -> userRepository.findByEmail(command.account()))
        .or(() -> userRepository.findByPhoneNumber(command.account()))
        .orElseThrow(() -> new DomainException(UserError.ACCOUNT_NOT_FOUND));

    // 3. 验证密码
    if (!user.matchesPassword(command.password())) {
        throw new DomainException(UserError.LOGIN_PASSWORD_INCORRECT);
    }

    // 4. 生成 Token
    var tokenInfo = authenticationService.login(user.getId());
    return new LoginResult(tokenInfo.token());
}
```

---

## 七、测试策略

### 7.1 后端测试

1. **单元测试**：`AccountLoginAppServiceTest`
   - 密码登录成功
   - 验证码错误
   - 账号不存在
   - 密码错误

2. **集成测试**：`AccountLoginIntegrationTest`
   - 完整登录流程
   - 验证码校验

### 7.2 前端测试

1. **Hook 测试**：`use-login.test.ts`
   - 登录成功状态更新
   - 错误处理
   - Loading 状态

2. **E2E 测试**：登录流程端到端测试

---

## 八、验收标准

### 后端

- [ ] 密码登录接口需要验证码
- [ ] 短信验证码登录接口需要验证码
- [ ] 单元测试通过
- [ ] 集成测试通过

### 前端

- [ ] 登录页面可正常获取和显示图形验证码
- [ ] 密码登录功能正常
- [ ] 短信验证码登录功能正常
- [ ] 登录成功后 Token 正确存储
- [ ] 登录成功后正确跳转
- [ ] 错误提示友好
- [ ] Loading 状态正常

---

## 九、文件清单

### 后端新增/修改

| 文件 | 操作 | 说明 |
|------|------|------|
| `LoginByPasswordCommand.java` | 修改 | 添加验证码字段 |
| `LoginBySmsCommand.java` | 修改 | 添加验证码字段 |
| `AccountLoginAppService.java` | 修改 | 添加验证码校验逻辑 |
| `AccountLoginAppServiceTest.java` | 修改 | 更新测试用例 |
| `AccountLoginIntegrationTest.java` | 修改 | 更新集成测试 |

### 前端新增/修改

| 文件 | 操作 | 说明 |
|------|------|------|
| `packages/api-client/src/auth/login.ts` | 新建 | 登录 API 客户端 |
| `packages/shared/src/hooks/use-login.ts` | 新建 | 登录 Hook |
| `web/src/app/(auth)/login/page.tsx` | 修改 | 集成登录逻辑 |
| `packages/api-client/src/auth/index.ts` | 修改 | 导出登录 API |

---

## 十、后续优化

1. **登录失败限流**：同一 IP/账号多次失败后增加限制
2. **登录日志**：记录登录行为用于安全审计
3. **记住登录**：延长 Token 有效期
4. **多种登录方式**：邮箱验证码登录、第三方登录
