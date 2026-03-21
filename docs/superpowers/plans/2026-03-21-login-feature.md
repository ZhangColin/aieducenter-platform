# 登录功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-step. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现前端登录页面与后端 API 打通，并添加登录验证码保护

**Architecture:** 后端修改 DTO 和 AppService 添加验证码校验；前端创建分层架构（API Client → Hook → Page）

**Tech Stack:** Java 21/Spring Boot（后端）、Next.js 15/Zustand/openapi-fetch（前端）

---

## 文件结构

### 后端（server/）
```
src/main/java/com/aieducenter/account/
├── application/dto/
│   ├── LoginByPasswordCommand.java    (修改：添加验证码字段)
│   └── LoginBySmsCommand.java         (修改：添加验证码字段)
├── application/
│   └── AccountLoginAppService.java    (修改：添加验证码校验)
└── ...test/...
    ├── application/AccountLoginAppServiceTest.java           (修改)
    └── web/AccountLoginIntegrationTest.java                  (修改)
```

### 前端（packages/）
```
api-client/src/auth/
├── login.ts                            (新建：登录 API)
└── index.ts                            (修改：导出)

shared/src/
└── hooks/use-login.ts                  (新建：登录 Hook)

web/src/app/(auth)/login/
└── page.tsx                            (修改：集成登录逻辑)
```

---

## Part 1: 后端 - 添加验证码字段到 DTO

### Task 1: 修改 LoginByPasswordCommand 添加验证码字段

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/application/dto/LoginByPasswordCommand.java`

- [ ] **Step 1: 读取现有文件确认当前结构**

```bash
cat server/src/main/java/com/aieducenter/account/application/dto/LoginByPasswordCommand.java
```

- [ ] **Step 2: 修改文件添加验证码字段**

将文件内容替换为：

```java
package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 密码登录命令。
 *
 * @param account  用户名、邮箱或手机号
 * @param password 明文密码
 * @param captchaId 图形验证码ID
 * @param captchaCode 图形验证码
 */
public record LoginByPasswordCommand(
    @NotBlank String account,
    @NotBlank String password,
    @NotBlank String captchaId,
    @NotBlank String captchaCode
) {}
```

- [ ] **Step 3: 编译验证**

```bash
cd server && ./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/account/application/dto/LoginByPasswordCommand.java
git commit -m "feat(account): add captcha fields to LoginByPasswordCommand"
```

---

### Task 2: 修改 LoginBySmsCommand 添加验证码字段

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/application/dto/LoginBySmsCommand.java`

- [ ] **Step 1: 读取现有文件确认当前结构**

```bash
cat server/src/main/java/com/aieducenter/account/application/dto/LoginBySmsCommand.java
```

- [ ] **Step 2: 修改文件添加验证码字段**

将文件内容替换为：

```java
package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 短信验证码登录命令。
 *
 * @param phone 手机号
 * @param code  6 位短信验证码
 * @param captchaId 图形验证码ID
 * @param captchaCode 图形验证码
 */
public record LoginBySmsCommand(
    @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") String phone,
    @NotBlank @Size(min = 6, max = 6) String code,
    @NotBlank String captchaId,
    @NotBlank String captchaCode
) {}
```

- [ ] **Step 3: 编译验证**

```bash
cd server && ./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/account/application/dto/LoginBySmsCommand.java
git commit -m "feat(account): add captcha fields to LoginBySmsCommand"
```

---

## Part 2: 后端 - 修改 AppService 添加验证码校验

### Task 3: 修改 AccountLoginAppService 添加验证码校验

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/application/AccountLoginAppService.java`

- [ ] **Step 1: 读取现有文件确认结构**

```bash
cat server/src/main/java/com/aieducenter/account/application/AccountLoginAppService.java
```

- [ ] **Step 2: 添加 CaptchaAppService 依赖**

首先在类中添加 `captchaAppService` 字段。找到构造函数，添加参数：

```java
private final UserRepository userRepository;
private final VerificationCodeAppService verificationCodeAppService;
private final AuthenticationService authenticationService;
private final CaptchaAppService captchaAppService;  // 新增

public AccountLoginAppService(
        UserRepository userRepository,
        VerificationCodeAppService verificationCodeAppService,
        AuthenticationService authenticationService,
        CaptchaAppService captchaAppService) {  // 新增参数
    this.userRepository = userRepository;
    this.verificationCodeAppService = verificationCodeAppService;
    this.authenticationService = authenticationService;
    this.captchaAppService = captchaAppService;  // 新增赋值
}
```

同时需要添加导入：

```java
import com.aieducenter.verification.application.CaptchaAppService;
```

- [ ] **Step 3: 编译验证依赖添加**

```bash
cd server && ./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交依赖添加**

```bash
git add server/src/main/java/com/aieducenter/account/application/AccountLoginAppService.java
git commit -m "feat(account): add CaptchaAppService dependency to AccountLoginAppService"
```

- [ ] **Step 5: 修改 loginByPassword 方法添加验证码校验**

在 `loginByPassword` 方法开始处添加验证码校验，修改后的方法应该是：

```java
/**
 * 密码登录。
 *
 * <p>account 可以是用户名、邮箱或手机号，依次尝试查找；全部未命中则抛出 ACCOUNT_NOT_FOUND (401)。</p>
 *
 * @param command 登录命令
 * @return 登录结果（含 token）
 * @throws DomainException ACCOUNT_NOT_FOUND (401) / LOGIN_PASSWORD_INCORRECT (401) / CAPTCHA_INVALID (400)
 */
public LoginResult loginByPassword(LoginByPasswordCommand command) {
    // 1. 验证图形验证码
    captchaAppService.verifyCaptcha(command.captchaId(), command.captchaCode());

    // 2. 查找用户
    String account = command.account();
    User user = userRepository.findByUsername(account)
        .or(() -> userRepository.findByEmail(account))
        .or(() -> userRepository.findByPhoneNumber(account))
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

- [ ] **Step 6: 修改 loginBySms 方法添加验证码校验**

在 `loginBySms` 方法开始处添加验证码校验，修改后的方法应该是：

```java
/**
 * 短信验证码登录。
 *
 * <p>先校验图形验证码，再校验短信验证码（失败则直接向上抛出），最后查找账号。</p>
 *
 * @param command 登录命令
 * @return 登录结果（含 token）
 * @throws DomainException ACCOUNT_NOT_FOUND (401)，或 VerificationCodeError（验证码无效/过期/已用）
 */
public LoginResult loginBySms(LoginBySmsCommand command) {
    // 1. 验证图形验证码
    captchaAppService.verifyCaptcha(command.captchaId(), command.captchaCode());

    // 2. 校验短信验证码
    verificationCodeAppService.verifyPhoneCode(
        new VerifySmsCodeCommand(command.phone(), command.code(), "LOGIN"));

    // 3. 查找用户
    User user = userRepository.findByPhoneNumber(command.phone())
        .orElseThrow(() -> new DomainException(UserError.ACCOUNT_NOT_FOUND));

    // 4. 生成 Token
    var tokenInfo = authenticationService.login(user.getId());
    return new LoginResult(tokenInfo.token());
}
```

- [ ] **Step 7: 编译验证**

```bash
cd server && ./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 提交**

```bash
git add server/src/main/java/com/aieducenter/account/application/AccountLoginAppService.java
git commit -m "feat(account): add captcha verification to login methods"
```

---

## Part 3: 后端 - 更新测试

### Task 4: 更新 AccountLoginAppServiceTest

**Files:**
- Modify: `server/src/test/java/com/aieducenter/account/application/AccountLoginAppServiceTest.java`

- [ ] **Step 1: 读取现有测试文件**

```bash
cat server/src/test/java/com/aieducenter/account/application/AccountLoginAppServiceTest.java
```

- [ ] **Step 2: 更新测试用例**

需要为所有测试方法添加 captchaId 和 captchaCode 参数。以下是主要修改：

对于 `loginByPassword` 相关测试，修改为：

```java
// 示例：成功的密码登录测试
@Test
void given_valid_credentials_and_captcha_when_loginByPassword_then_returnToken() {
    // Given
    when(captchaAppService.verifyCaptcha("captchaId", "1234")).thenReturn(null); // 验证码通过
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(authenticationService.login(user.getId())).thenReturn(new TokenInfo("test-token", 3600));

    // When
    LoginByPasswordCommand command = new LoginByPasswordCommand(
        "testuser", "password123", "captchaId", "1234"
    );
    LoginResult result = service.loginByPassword(command);

    // Then
    assertThat(result.token()).isEqualTo("test-token");
    verify(captchaAppService).verifyCaptcha("captchaId", "1234");
}

// 示例：验证码错误的测试
@Test
void given_invalid_captcha_when_loginByPassword_then_throwException() {
    // Given
    when(captchaAppService.verifyCaptcha("invalidId", "wrong"))
        .thenThrow(new DomainException(CaptchaError.CAPTCHA_INVALID));

    // When/Then
    LoginByPasswordCommand command = new LoginByPasswordCommand(
        "testuser", "password123", "invalidId", "wrong"
    );

    assertThatThrownBy(() -> service.loginByPassword(command))
        .isInstanceOf(DomainException.class)
        .satisfies(e -> assertThat(((DomainException) e).getError()).isEqualTo(CaptchaError.CAPTCHA_INVALID));
}
```

- [ ] **Step 3: 运行测试**

```bash
cd server && ./gradlew test --tests "*AccountLoginAppServiceTest"
```

Expected: 所有测试通过

- [ ] **Step 4: 提交**

```bash
git add server/src/test/java/com/aieducenter/account/application/AccountLoginAppServiceTest.java
git commit -m "test(account): update tests for captcha verification"
```

---

### Task 5: 更新 AccountLoginIntegrationTest

**Files:**
- Modify: `server/src/test/java/com/aieducenter/account/web/AccountLoginIntegrationTest.java`

- [ ] **Step 1: 读取现有测试文件**

```bash
cat server/src/test/java/com/aieducenter/account/web/AccountLoginIntegrationTest.java
```

- [ ] **Step 2: 添加图形验证码 Mock 和获取**

首先，需要在测试类中添加 CaptchaAppService 的 Mock：

```java
@MockBean
private CaptchaAppService captchaAppService;

@BeforeEach
void setUpMocks() {
    // Mock verification code for registration calls
    when(verificationCodeAppService.verifyPhoneCode(any()))
        .thenReturn(new VerifyCodeResult(true, "OK"));

    // Mock captcha verification (always pass)
    when(captchaAppService.verifyCaptcha(anyString(), anyString())).thenReturn(null);
}
```

- [ ] **Step 3: 更新密码登录测试**

修改登录请求体，添加验证码字段：

```java
@Test
@Transactional
void given_username_and_password_when_login_by_password_then_return_token() throws Exception {
    registerUser("loginuser1", "Password1", null, null);

    String loginBody = """
        {
            "account": "loginuser1",
            "password": "Password1",
            "captchaId": "test-captcha-id",
            "captchaCode": "1234"
        }
        """;

    mvc.perform(post("/api/account/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.token").isNotEmpty());
}
```

类似地更新其他密码登录测试（`given_email_and_password`、`given_phone_and_password`、`given_unknown_account`）。

- [ ] **Step 4: 更新短信验证码登录测试**

```java
@Test
@Transactional
void given_valid_sms_code_when_login_by_sms_then_return_token() throws Exception {
    registerUser("smsuser1", "Password1", null, "13900139001");

    when(verificationCodeAppService.verifyPhoneCode(any()))
        .thenReturn(new VerifyCodeResult(true, "OK"));

    String smsBody = """
        {
            "phone": "13900139001",
            "code": "123456",
            "captchaId": "test-captcha-id",
            "captchaCode": "1234"
        }
        """;

    mvc.perform(post("/api/account/login/sms")
            .contentType(MediaType.APPLICATION_JSON)
            .content(smsBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.token").isNotEmpty());
}
```

类似地更新 `given_unregistered_phone_when_login_by_sms` 测试。

- [ ] **Step 5: 运行集成测试**

```bash
cd server && ./gradlew test --tests "*AccountLoginIntegrationTest"
```

Expected: 所有测试通过

- [ ] **Step 6: 提交**

```bash
git add server/src/test/java/com/aieducenter/account/web/AccountLoginIntegrationTest.java
git commit -m "test(account): update integration tests for captcha verification"
```

---

## Part 4: 前端 - 创建登录 API 客户端

### Task 6: 创建登录 API 客户端文件

**Files:**
- Create: `packages/api-client/src/auth/login.ts`
- Create: `packages/api-client/src/auth/index.ts`

- [ ] **Step 1: 创建 api-client/src/auth 目录（如果不存在）**

```bash
mkdir -p packages/api-client/src/auth
```

- [ ] **Step 2: 创建 login.ts 文件**

创建文件 `packages/api-client/src/auth/login.ts`：

```typescript
/**
 * 登录 API 客户端
 *
 * 提供密码登录、短信验证码登录和图形验证码获取功能
 */

import { api } from '../api/client'

// ========== 类型定义 ==========

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

export interface SendSmsCodeParams {
  phone: string
  purpose: 'LOGIN' | 'REGISTER' | 'RESET_PASSWORD'
  captchaId: string
  captchaCode: string
}

export interface SendSmsCodeResponse {
  expireSeconds: number
  cooldownSeconds: number
}

// ========== API 函数 ==========

/**
 * 密码登录
 */
export async function loginByPassword(params: LoginByPasswordParams): Promise<LoginResponse> {
  const { data, error } = await api.POST('/api/account/login', {
    body: params
  })

  if (error) {
    throw error
  }

  return data as LoginResponse
}

/**
 * 短信验证码登录
 */
export async function loginBySms(params: LoginBySmsParams): Promise<LoginResponse> {
  const { data, error } = await api.POST('/api/account/login/sms', {
    body: params
  })

  if (error) {
    throw error
  }

  return data as LoginResponse
}

/**
 * 获取图形验证码
 */
export async function getCaptcha(): Promise<CaptchaResponse> {
  const { data, error } = await api.GET('/api/captcha')

  if (error) {
    throw error
  }

  return data as CaptchaResponse
}

/**
 * 发送短信验证码
 */
export async function sendSmsCode(params: SendSmsCodeParams): Promise<SendSmsCodeResponse> {
  const { data, error } = await api.POST('/api/account/verification-code/sms', {
    body: params
  })

  if (error) {
    throw error
  }

  return data as SendSmsCodeResponse
}
```

- [ ] **Step 3: 创建 auth/index.ts 导出文件**

> **注意**：`api-client/src/auth/` 目录当前不存在 `index.ts`，这是新建文件操作。

创建文件 `packages/api-client/src/auth/index.ts`：

```typescript
export * from './refresh'
export * from './login'
```

- [ ] **Step 4: TypeScript 类型检查**

```bash
cd packages/api-client && pnpm typecheck
```

Expected: 无错误

- [ ] **Step 5: 提交**

```bash
git add packages/api-client/src/auth/
git commit -m "feat(api-client): add login API client"
```

---

## Part 5: 前端 - 创建登录 Hook

### Task 7: 创建 useLogin Hook

**Files:**
- Create: `packages/shared/src/hooks/use-login.ts`
- Modify: `packages/shared/src/index.ts`

- [ ] **Step 1: 创建 hooks 目录（如果不存在）**

```bash
mkdir -p packages/shared/src/hooks
```

- [ ] **Step 2: 创建 use-login.ts 文件**

创建文件 `packages/shared/src/hooks/use-login.ts`：

> **注意**：`@aieducenter/shared/auth-store` 导入路径是有效的，因为 `packages/shared/package.json` 中已配置 `"./auth-store": "./src/auth-store.ts"`。

```typescript
/**
 * 登录 Hook
 *
 * 封装登录逻辑，提供密码登录和短信验证码登录功能
 */

import { useState, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore, type AuthUser } from '@aieducenter/shared/auth-store'
import {
  loginByPassword,
  loginBySms,
  getCaptcha,
  sendSmsCode,
  type LoginByPasswordParams,
  type LoginBySmsParams,
  type CaptchaResponse,
  type SendSmsCodeResponse
} from '@aieducenter/api-client'

export interface LoginResult {
  success: boolean
  error?: string
}

// ========== Hook ==========

export function useLogin() {
  const router = useRouter()
  const { login: authLogin } = useAuthStore()

  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  /**
   * 密码登录
   */
  const loginByPasswordHandler = useCallback(async (params: LoginByPasswordParams): Promise<LoginResult> => {
    setIsLoading(true)
    setError(null)

    try {
      const response = await loginByPassword(params)

      // 登录成功，存储 token 和用户信息
      // 注意：这里需要获取用户信息，暂时使用占位符
      const user: AuthUser = {
        userId: response.token, // 暂时使用 token 作为 userId，后续需要调用 profile 接口
        nickname: '',
        avatar: null
      }

      authLogin(response.token, user)

      // 跳转到首页
      router.push('/')

      return { success: true }
    } catch (err: any) {
      const message = err.error?.message || '登录失败，请重试'
      setError(message)
      return { success: false, error: message }
    } finally {
      setIsLoading(false)
    }
  }, [authLogin, router])

  /**
   * 短信验证码登录
   */
  const loginBySmsHandler = useCallback(async (params: LoginBySmsParams): Promise<LoginResult> => {
    setIsLoading(true)
    setError(null)

    try {
      const response = await loginBySms(params)

      // 登录成功，存储 token 和用户信息
      const user: AuthUser = {
        userId: response.token,
        nickname: '',
        avatar: null
      }

      authLogin(response.token, user)

      // 跳转到首页
      router.push('/')

      return { success: true }
    } catch (err: any) {
      const message = err.error?.message || '登录失败，请重试'
      setError(message)
      return { success: false, error: message }
    } finally {
      setIsLoading(false)
    }
  }, [authLogin, router])

  /**
   * 获取图形验证码
   */
  const getCaptchaHandler = useCallback(async (): Promise<CaptchaResponse | null> => {
    try {
      return await getCaptcha()
    } catch (err: any) {
      setError('获取验证码失败')
      return null
    }
  }, [])

  /**
   * 发送短信验证码
   */
  const sendSmsCodeHandler = useCallback(async (
    phone: string,
    captchaId: string,
    captchaCode: string
  ): Promise<{ success: boolean; data?: SendSmsCodeResponse; error?: string }> => {
    try {
      const data = await sendSmsCode({
        phone,
        purpose: 'LOGIN',
        captchaId,
        captchaCode
      })
      return { success: true, data }
    } catch (err: any) {
      const message = err.error?.message || '发送验证码失败'
      setError(message)
      return { success: false, error: message }
    }
  }, [])

  return {
    // 状态
    isLoading,
    error,

    // 方法
    loginByPassword: loginByPasswordHandler,
    loginBySms: loginBySmsHandler,
    getCaptcha: getCaptchaHandler,
    sendSmsCode: sendSmsCodeHandler,

    // 工具方法
    clearError: () => setError(null)
  }
}
```

- [ ] **Step 3: 更新 api-client/src/index.ts 导出**

编辑 `packages/api-client/src/index.ts`，添加登录 API 导出：

```bash
cat packages/api-client/src/index.ts
```

修改为：

```typescript
/**
 * API 客户端统一导出入口
 */

export { api } from './api/client'
export type { paths, components, operations } from './api/schema'

// 便捷类型导出
export type ApiError = {
  error?: {
    status: number
    message: string
  }
}

// 重新导出手动类型
export type { ApiResponse, FieldError, PageResponse } from './api/types'

// 导出登录相关 API
export * from './auth/login'
```

- [ ] **Step 4: 更新 shared/src/index.ts 导出**

编辑 `packages/shared/src/index.ts`，添加 hooks 导出：

```bash
cat packages/shared/src/index.ts
```

修改为：

```typescript
export * from './auth-store'
export * from './hooks/use-login'
```

- [ ] **Step 5: TypeScript 类型检查**

```bash
cd packages/shared && pnpm typecheck
```

Expected: 无错误

- [ ] **Step 6: 提交**

```bash
git add packages/shared/src/
git commit -m "feat(shared): add useLogin hook"
```

---

## Part 6: 前端 - 集成登录页面

### Task 8: 修改登录页面集成登录逻辑

**Files:**
- Modify: `web/src/app/(auth)/login/page.tsx`

由于文件较长，需要完整重写。以下是完整实现：

- [ ] **Step 1: 备份原文件并读取**

```bash
cp web/src/app/\(auth\)/login/page.tsx web/src/app/\(auth\)/login/page.tsx.bak
cat web/src/app/\(auth\)/login/page.tsx | head -50
```

- [ ] **Step 2: 完整替换 page.tsx 内容**

创建新的 `web/src/app/(auth)/login/page.tsx`：

```typescript
'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useLogin } from '@aieducenter/shared'

type LoginType = 'account' | 'sms'

export default function LoginPage() {
  const router = useRouter()
  const {
    loginByPassword,
    loginBySms,
    getCaptcha,
    sendSmsCode,
    isLoading,
    error,
    clearError
  } = useLogin()

  // UI 状态
  const [loginType, setLoginType] = useState<LoginType>('account')
  const [showPassword, setShowPassword] = useState(false)

  // 表单数据 - 密码登录
  const [account, setAccount] = useState('')
  const [password, setPassword] = useState('')

  // 表单数据 - 短信登录
  const [phone, setPhone] = useState('')
  const [smsCode, setSmsCode] = useState('')

  // 验证码相关
  const [captchaId, setCaptchaId] = useState('')
  const [captchaUrl, setCaptchaUrl] = useState('')
  const [captchaCode, setCaptchaCode] = useState('')

  // 短信倒计时
  const [countdown, setCountdown] = useState(0)
  const [smsSent, setSmsSent] = useState(false)

  // 初始化：获取验证码
  useEffect(() => {
    fetchCaptcha()
  }, [])

  // 倒计时逻辑
  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => {
      setCountdown(prev => prev - 1)
    }, 1000)
    return () => clearInterval(timer)
  }, [countdown])

  // 获取图形验证码
  const fetchCaptcha = async () => {
    const result = await getCaptcha()
    if (result) {
      setCaptchaId(result.captchaId)
      setCaptchaUrl(result.image)
      setCaptchaCode('')
    }
  }

  // 发送短信验证码
  const handleSendSms = async () => {
    if (countdown > 0) return
    if (!phone || phone.length !== 11) {
      return
    }

    const result = await sendSmsCode(phone, captchaId, captchaCode)
    if (result.success) {
      setSmsSent(true)
      setCountdown(60)
    } else {
      // 验证码可能错误，刷新
      fetchCaptcha()
    }
  }

  // 处理登录
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    clearError()

    if (loginType === 'account') {
      if (!account || !password || !captchaCode) {
        return
      }
      const result = await loginByPassword({ account, password, captchaId, captchaCode })
      if (result.success) {
        // 登录成功，已在 hook 中处理跳转
      } else {
        // 刷新验证码
        fetchCaptcha()
      }
    } else {
      if (!phone || !smsCode || !captchaCode) {
        return
      }
      const result = await loginBySms({ phone, code: smsCode, captchaId, captchaCode })
      if (result.success) {
        // 登录成功，已在 hook 中处理跳转
      } else {
        // 刷新验证码
        fetchCaptcha()
      }
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-background-light dark:bg-background-dark font-display">
      <div className="flex-grow flex items-center justify-center p-4 sm:p-8">
        <div className="max-w-[1200px] w-full grid lg:grid-cols-2 bg-white dark:bg-slate-900 rounded-xl shadow-xl overflow-hidden min-h-[700px]">
          {/* Left Side: Hero/Branding */}
          <div className="hidden lg:flex flex-col justify-between p-12 bg-primary/5 relative overflow-hidden">
            <div className="relative z-10">
              <div className="flex items-center gap-3 mb-12">
                <img src="/logo-light-200.png" alt="Logo" className="h-10 w-auto" />
                <h1 className="text-2xl font-bold text-slate-900 dark:text-white">海创元智研云平台</h1>
              </div>
              <div className="space-y-6">
                <h2 className="text-4xl font-extrabold text-slate-900 dark:text-white leading-tight">
                  连接智慧 <br />
                  <span className="text-primary">驱动科研创新</span>
                </h2>
                <p className="text-lg text-slate-600 dark:text-slate-400 max-w-md">
                  面向教育与企业的统一 AI 智研入口，集成先进算法与海量算力，助力您的研究更进一步。
                </p>
              </div>
            </div>
            <div className="relative z-10 mt-auto">
              <div className="flex items-center gap-4 p-4 bg-white/80 dark:bg-slate-800/80 backdrop-blur rounded-lg border border-primary/10">
                <div className="size-12 rounded-full bg-primary/20 flex items-center justify-center">
                  <span className="material-symbols-outlined text-primary">verified_user</span>
                </div>
                <div>
                  <p className="text-sm font-semibold text-slate-900 dark:text-white">企业级安全保障</p>
                  <p className="text-xs text-slate-500">端到端加密与数据隔离技术</p>
                </div>
              </div>
            </div>
            <div className="absolute -bottom-20 -left-20 size-80 bg-primary/10 rounded-full blur-3xl"></div>
            <div className="absolute -top-20 -right-20 size-96 bg-primary/5 rounded-full blur-3xl"></div>
          </div>

          {/* Right Side: Login Form */}
          <div className="flex flex-col justify-center p-8 sm:p-16">
            <div className="w-full max-w-md mx-auto">
              {/* Mobile Logo */}
              <div className="lg:hidden flex items-center gap-2 mb-8">
                <img src="/logo-light-200.png" alt="Logo" className="h-8 w-auto" />
                <h2 className="text-xl font-bold text-slate-900 dark:text-white">海创元智研</h2>
              </div>

              <div className="mb-8">
                <h3 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">欢迎回来</h3>
                <p className="text-slate-500 dark:text-slate-400">请选择登录方式进入您的智研空间</p>
              </div>

              {/* Tabs */}
              <div className="flex border-b border-slate-200 dark:border-slate-700 mb-8">
                <button
                  onClick={() => { setLoginType('account'); clearError() }}
                  className={`px-6 py-3 text-sm font-bold border-b-2 transition-colors ${
                    loginType === 'account'
                      ? 'text-primary border-primary'
                      : 'text-slate-500 border-transparent hover:text-slate-700 dark:hover:text-slate-300'
                  }`}
                >
                  账号登录
                </button>
                <button
                  onClick={() => { setLoginType('sms'); clearError() }}
                  className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
                    loginType === 'sms'
                      ? 'text-primary border-primary'
                      : 'text-slate-500 border-transparent hover:text-slate-700 dark:hover:text-slate-300'
                  }`}
                >
                  手机验证码
                </button>
              </div>

              {/* Error Message */}
              {error && (
                <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                  <p className="text-sm text-red-600 dark:text-red-400 flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm">error</span>
                    {error}
                  </p>
                </div>
              )}

              <form onSubmit={handleLogin} className="space-y-5">
                {loginType === 'account' ? (
                  <>
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        账号
                      </label>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          person
                        </span>
                        <input
                          type="text"
                          placeholder="用户名/邮箱/手机号"
                          value={account}
                          onChange={(e) => setAccount(e.target.value)}
                          className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <div className="flex justify-between">
                        <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                          登录密码
                        </label>
                      </div>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          lock
                        </span>
                        <input
                          type={showPassword ? 'text' : 'password'}
                          placeholder="请输入密码"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          className="w-full pl-10 pr-12 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400"
                        >
                          <span className="material-symbols-outlined text-xl">
                            {showPassword ? 'visibility' : 'visibility_off'}
                          </span>
                        </button>
                      </div>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        手机号
                      </label>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                          smartphone
                        </span>
                        <input
                          type="tel"
                          placeholder="请输入手机号"
                          value={phone}
                          onChange={(e) => setPhone(e.target.value)}
                          maxLength={11}
                          className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                        短信验证码
                      </label>
                      <div className="flex gap-3">
                        <div className="relative flex-1">
                          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                            verified
                          </span>
                          <input
                            type="text"
                            placeholder="请输入验证码"
                            value={smsCode}
                            onChange={(e) => setSmsCode(e.target.value)}
                            maxLength={6}
                            className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                          />
                        </div>
                        <button
                          type="button"
                          onClick={handleSendSms}
                          disabled={countdown > 0 || !phone || phone.length !== 11}
                          className="px-4 py-3 whitespace-nowrap rounded-lg border border-primary text-primary font-medium hover:bg-primary/5 transition-colors disabled:opacity-50 disabled:cursor-not-allowed min-w-[100px]"
                        >
                          {countdown > 0 ? `${countdown}s` : '获取验证码'}
                        </button>
                      </div>
                    </div>
                  </>
                )}

                {/* 图形验证码 */}
                <div className="space-y-2">
                  <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                    图形验证码
                  </label>
                  <div className="flex gap-3">
                    <div className="relative flex-1">
                      <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
                        captcha
                      </span>
                      <input
                        type="text"
                        placeholder="请输入验证码"
                        value={captchaCode}
                        onChange={(e) => setCaptchaCode(e.target.value)}
                        className="w-full pl-10 pr-4 py-3 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all"
                      />
                    </div>
                    <div
                      className="w-32 h-12 rounded-lg border border-slate-200 dark:border-slate-700 overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                      onClick={fetchCaptcha}
                    >
                      {captchaUrl ? (
                        <img
                          src={captchaUrl}
                          alt="验证码"
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <div className="w-full h-full bg-slate-200 dark:bg-slate-700 animate-pulse" />
                      )}
                    </div>
                  </div>
                  <p className="text-xs text-slate-500">点击图片刷新验证码</p>
                </div>

                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-full py-3 bg-primary hover:bg-primary/90 text-white font-bold rounded-lg transition-colors shadow-lg shadow-primary/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  {isLoading ? (
                    <>
                      <span className="material-symbols-outlined animate-spin">refresh</span>
                      登录中...
                    </>
                  ) : (
                    '立即登录'
                  )}
                </button>
              </form>

              <div className="mt-8">
                <div className="relative">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-slate-200 dark:border-slate-700"></div>
                  </div>
                  <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-white dark:bg-slate-900 px-2 text-slate-500">其他方式登录</span>
                  </div>
                </div>

                <div className="mt-6 grid grid-cols-2 gap-4">
                  <button type="button" className="flex items-center justify-center gap-2 py-2 px-4 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                    <div className="size-5 rounded-full bg-[#07C160] flex items-center justify-center text-white">
                      <span className="material-symbols-outlined text-[14px]">chat</span>
                    </div>
                    <span className="text-sm text-slate-600 dark:text-slate-300">微信登录</span>
                  </button>
                  <button type="button" className="flex items-center justify-center gap-2 py-2 px-4 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                    <div className="size-5 rounded-full bg-[#0089FF] flex items-center justify-center text-white">
                      <span className="material-symbols-outlined text-[14px]">business</span>
                    </div>
                    <span className="text-sm text-slate-600 dark:text-slate-300">钉钉登录</span>
                  </button>
                </div>
              </div>

              <p className="mt-8 text-center text-sm text-slate-500">
                还没有账号？{' '}
                <a className="text-primary font-semibold hover:underline cursor-pointer" onClick={() => router.push('/register')}>
                  立即注册
                </a>
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="p-6 text-center">
        <div className="flex flex-wrap justify-center gap-6 text-sm text-slate-500 mb-4">
          <a className="hover:text-primary transition-colors" href="#">
            关于我们
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            服务协议
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            隐私政策
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            联系支持
          </a>
          <a className="hover:text-primary transition-colors" href="#">
            官方博客
          </a>
        </div>
        <p className="text-xs text-slate-400">
          © 2024 海创元 (Hai Chuang Yuan). All rights reserved. 京ICP备XXXXXXXX号
        </p>
      </footer>
    </div>
  )
}
```

- [ ] **Step 3: TypeScript 类型检查**

```bash
cd web && pnpm typecheck
```

Expected: 无错误

- [ ] **Step 4: 提交**

```bash
git add "web/src/app/(auth)/login/page.tsx"
git commit -m "feat(web): integrate login API with page"
```

---

## Part 7: 验收测试

### Task 9: 后端验收测试

- [ ] **Step 1: 运行所有后端测试**

```bash
cd server && ./gradlew test --tests "*AccountLogin*"
```

Expected: 所有测试通过

- [ ] **Step 2: 手动测试 API（可选）**

启动服务器后使用 curl 测试：

```bash
# 获取验证码
curl http://localhost:8080/api/captcha

# 密码登录（需要先注册用户）
curl -X POST http://localhost:8080/api/account/login \
  -H "Content-Type: application/json" \
  -d '{"account":"testuser","password":"Password123","captchaId":"xxx","captchaCode":"1234"}'
```

---

### Task 10: 前端验收测试

- [ ] **Step 1: 启动前端开发服务器**

```bash
cd web && pnpm dev
```

- [ ] **Step 2: 手动测试登录功能**

1. 访问 http://localhost:3000/login
2. 检查图形验证码是否显示
3. 测试密码登录：
   - 输入已注册的账号和密码
   - 输入图形验证码
   - 点击登录
   - 验证登录成功后跳转
4. 测试短信验证码登录：
   - 切换到短信验证码 tab
   - 输入手机号
   - 点击获取验证码
   - 输入短信验证码（固定为 123456）
   - 输入图形验证码
   - 点击登录
5. 测试错误场景：
   - 错误的密码
   - 错误的验证码
   - 不存在的账号

- [ ] **Step 3: 检查浏览器控制台**

确认没有错误信息

---

## 完成检查清单

### 后端

- [ ] LoginByPasswordCommand 添加了 captchaId 和 captchaCode 字段
- [ ] LoginBySmsCommand 添加了 captchaId 和 captchaCode 字段
- [ ] AccountLoginAppService.loginByPassword 添加了验证码校验
- [ ] AccountLoginAppService.loginBySms 添加了验证码校验
- [ ] AccountLoginAppServiceTest 更新并通过
- [ ] AccountLoginIntegrationTest 更新并通过

### 前端

- [ ] packages/api-client/src/auth/login.ts 创建完成
- [ ] packages/api-client/src/auth/index.ts 导出登录 API
- [ ] packages/shared/src/hooks/use-login.ts 创建完成
- [ ] packages/shared/src/index.ts 导出 useLogin hook
- [ ] web/src/app/(auth)/login/page.tsx 集成登录逻辑
- [ ] 登录页面可以正常获取和显示图形验证码
- [ ] 密码登录功能正常
- [ ] 短信验证码登录功能正常
- [ ] 登录成功后 Token 正确存储
- [ ] 登录成功后正确跳转
- [ ] 错误提示友好
- [ ] Loading 状态正常

---

## 相关文档

- 设计文档: [docs/superpowers/specs/2026-03-21-login-feature-design.md](../specs/2026-03-21-login-feature-design.md)
- Cartisan Boot 使用手册: [docs/guide/cartisan-boot-使用手册.md](../../guide/cartisan-boot-使用手册.md)
- 常见问题: [docs/PITFALLS.md](../../PITFALLS.md)
