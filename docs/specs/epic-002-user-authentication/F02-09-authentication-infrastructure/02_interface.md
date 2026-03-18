# Feature: F02-09 认证基础设施 — 接口契约

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 2 - Design

---

## 接口定义

### 测试专用接口（TestAuthController）

**路径前缀：** `/test/auth`
**说明：** 仅用于测试验证认证配置，生产环境不暴露

#### POST /test/auth/login
**描述：** 测试登录，创建 Sa-Token 会话

**Request:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |

**Response (成功):**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "uuid-string",
    "loginId": 123456,
    "expireTime": "2026-03-25T12:00:00Z"
  }
}
```

#### GET /test/auth/check
**描述：** 测试 @RequireAuth 注解

**Request Headers:**
| Header | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |

**Response (成功):**
```json
{
  "code": 200,
  "message": "success",
  "data": "authenticated"
}
```

**错误码：**
| 错误码 | HTTP Status | 触发条件 |
|--------|-----------|---------|
| - | 401 | 未登录或 Token 无效 |

#### GET /test/auth/user-id
**描述：** 测试 @CurrentUser 注解

**Request Headers:**
| Header | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |

**Response (成功):**
```json
{
  "code": 200,
  "message": "success",
  "data": 123456
}
```

#### GET /test/auth/user-id-optional
**描述：** 测试 @CurrentUser Optional<Long>，允许匿名访问

**Response (成功，已登录):**
```json
{
  "code": 200,
  "message": "success",
  "data": 123456
}
```

**Response (成功，未登录):**
```json
{
  "code": 200,
  "message": "success",
  "data": -1
}
```

#### POST /test/auth/logout
**描述：** 测试登出

**Response (成功):**
```json
{
  "code": 200,
  "message": "success"
}
```

---

## 领域接口描述（伪代码）

### SaTokenConfig
```java
@Configuration
class SaTokenConfig {
    // 配置 Sa-Token 参数（预留扩展点）
    // 当前配置在 application.yml 中完成
}
```

### TestAuthController
```java
@RestController
@RequestMapping("/test/auth")
class TestAuthController {
    - login(userId: Long) → TokenInfo
    - checkAuth() → String（@RequireAuth）
    - getCurrentUserId(userId: Long) → Long（@CurrentUser）
    - getUserIdOptional(userId: Optional<Long>) → Long（@CurrentUser）
    - logout() → Void
}
```

---

## 配置接口

### application.yml 新增配置

```yaml
# cartisan-security 拦截器配置
cartisan:
  security:
    interceptor:
      path-patterns:
        - "/api/**"
        - "/admin/**"
      exclude-path-patterns:
        - "/api/account/login"
        - "/api/account/register/**"
        - "/api/account/verification-code/**"
        - "/error"
        - "/actuator/**"

# Sa-Token 配置
sa-token:
  token-name: Authorization
  timeout: 604800              # 7 天
  active-timeout: -1           # 不自动滚动过期
  is-concurrent: true          # 允许并发登录
  is-share: false              # 不共享 Session
  token-style: uuid
  is-log: false
```

---

## 数据结构

### TokenInfo（来自 cartisan-security）
| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | Token 值 |
| loginId | Long | 用户标识 |
| expireTime | Instant | 过期时间 |

---

## 错误码

| 错误码 | HTTP Status | 触发条件 | 响应示例 |
|--------|-----------|---------|---------|
| - | 401 | 未登录或 Token 无效 | `{"code": 401, "message": "未登录"}` |
| - | 403 | 无权限访问 | `{"code": 403, "message": "无权限"}` |

**注意：** 错误码由 `SecurityExceptionHandler` 自动转换，业务代码无需处理。
