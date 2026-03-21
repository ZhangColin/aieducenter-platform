# 注册功能调整设计

> 版本：v1.0 | 日期：2026-03-21
> 状态：待评审
> 相关 Epic：Epic 2 - 用户与登录

---

## 一、背景

原设计区分邮箱注册和手机号注册，实际使用中过于复杂。简化为单一注册流程，移除邮箱字段（后续后台提供邮箱绑定功能）。

### 变更原因

1. **简化用户流程** — 用户只需填写用户名 + 手机号即可注册
2. **降低开发成本** — 前后端只需维护一套注册逻辑
3. **后续扩展** — 邮箱绑定作为独立功能在个人中心实现

---

## 二、需求

### 2.1 注册字段

| 字段 | 必填 | 说明 |
|------|------|------|
| username | 是 | 3-20 位，字母开头，允许字母/数字/下划线 |
| phone | 是 | 11 位手机号，1 开头 |
| password | 是 | 8-20 位，必须包含字母和数字 |
| nickname | 否 | 可选，不填则默认等于用户名 |
| verificationCode | 是 | 短信验证码，固定值 123456（开发期） |

### 2.2 注册流程

```
1. 用户填写表单（用户名、手机号、密码、昵称）
2. blur 事件触发实时查重
3. 点击「获取验证码」：
   - 展示图形验证码弹窗
   - 用户输入图形验证码
   - 后端校验图形验证码
   - 发送短信验证码（固定 123456）
   - 前端开始 60 秒倒计时
4. 用户输入短信验证码
5. 提交注册 → 创建用户 + 单人租户 → 返回 Token
```

### 2.3 防刷机制

| 阶段 | 措施 | 参数 |
|------|------|------|
| 图形验证码 | hutool-captcha 生成 | 3 分钟过期 |
| 短信发送 | 同一手机号 60 秒冷却 | |
| 短信发送 | 同一 IP 每小时最多 10 次 | |
| 注册接口 | IP 限流（可选） | |

---

## 三、后端设计

### 3.1 Verification Context 扩展

#### 新增图形验证码类型

```java
// VerificationType.java
public enum VerificationType {
    EMAIL,
    SMS,
    CAPTCHA  // 新增
}
```

#### 新增 CaptchaGenerationService

```java
@DomainService
public class CaptchaGenerationService {

    /**
     * 生成图形验证码。
     *
     * @return 验证码图片（base64）和验证码文本
     */
    public CaptchaResult generate() {
        // 使用 hutool-captcha LineCaptcha
        // 130x40 尺寸，4 位字符
        LineCaptcha captcha = new LineCaptcha(130, 40, 4, 20);

        String image = captcha.getImageBase64();
        String code = captcha.getCode();

        return new CaptchaResult(image, code);
    }
}

record CaptchaResult(String image, String code) {}
```

#### 新增 CaptchaRepository

```java
@Port(PortType.REPOSITORY)
public interface CaptchaRepository {

    /**
     * 保存图形验证码。
     */
    void save(CaptchaCode captcha);

    /**
     * 校验并删除图形验证码。
     *
     * @return 校验是否通过
     */
    boolean verifyAndDelete(String captchaId, String code);
}
```

#### Redis 实现

```java
@Adapter(PortType.REPOSITORY)
public class RedisCaptchaRepository implements CaptchaRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "captcha:";
    private static final long EXPIRE_SECONDS = 180; // 3 分钟

    @Override
    public void save(CaptchaCode captcha) {
        String key = KEY_PREFIX + captcha.getId();
        redisTemplate.opsForValue().set(key, captcha.getCode(), EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public boolean verifyAndDelete(String captchaId, String code) {
        String key = KEY_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode != null && storedCode.equalsIgnoreCase(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
```

#### CaptchaAppService

```java
@Service
public class CaptchaAppService {

    private final CaptchaGenerationService generator;
    private final CaptchaRepository repository;

    public CreateCaptchaResponse createCaptcha() {
        CaptchaResult result = generator.generate();

        String captchaId = UUID.randomUUID().toString();
        CaptchaCode captcha = new CaptchaCode(captchaId, result.code());

        repository.save(captcha);

        return new CreateCaptchaResponse(result.image(), captchaId);
    }

    public void verifyCaptcha(String captchaId, String code) {
        boolean verified = repository.verifyAndDelete(captchaId, code);
        Assertions.require(verified, VerificationCodeError.CAPTCHA_INVALID);
    }
}
```

#### CaptchaController

```java
@RestController
@RequestMapping("/api")
public class CaptchaController {

    private final CaptchaAppService captchaAppService;

    @GetMapping("/captcha")
    public ApiResponse<CreateCaptchaResponse> getCaptcha() {
        return ApiResponse.ok(captchaAppService.createCaptcha());
    }
}
```

### 3.2 修改短信验证码

#### VerificationCodeAppService.sendSmsVerificationCode()

```java
// 第 163 行后添加一行
String code = generator.generate();
code = "123456";  // TODO: 短信接口对接后删除此行
```

#### 修改 sendSmsVerificationCode 接口

```java
// SendSmsCodeCommand 新增字段
public record SendSmsCodeCommand(
    @NotBlank String phone,
    @NotBlank String purpose,
    @NotBlank String captchaId,    // 新增：图形验证码 ID
    @NotBlank String captchaCode   // 新增：图形验证码
) {}

// sendSmsVerificationCode 方法新增参数
public SendCodeResponse sendSmsVerificationCode(
    SendSmsCodeCommand command,
    String ip
) {
    // ... 原有逻辑

    // 1. 先校验图形验证码
    captchaAppService.verifyCaptcha(command.captchaId(), command.captchaCode());

    // 2. 再执行后续限流和发送逻辑
    // ...
}
```

### 3.3 Account Context 调整

#### 修改 RegisterCommand

```java
public record RegisterCommand(
    @NotBlank @Size(min = 3, max = 20) String username,
    @NotBlank @Size(min = 8, max = 20) String password,
    @Size(max = 50) String nickname,
    @Pattern(regexp = "^1[3-9]\\d{9}$") String phone,
    @NotBlank String verificationCode  // 新增
) {}
```

#### 修改 AccountRegistrationAppService

```java
@Transactional
public RegisterResult register(RegisterCommand command) {
    // 1. 用户名查重
    if (userRepository.existsByUsername(command.username())) {
        throw new DomainException(UserError.USERNAME_ALREADY_EXISTS);
    }

    // 2. 手机号查重
    if (userRepository.existsByPhoneNumber(command.phone())) {
        throw new DomainException(UserError.PHONE_NUMBER_ALREADY_EXISTS);
    }

    // 3. 校验短信验证码
    verificationCodeAppService.verifyPhoneCode(
        new VerifySmsCodeCommand(command.phone(), "REGISTER", command.verificationCode())
    );

    // 4. 创建用户（移除 email 参数）
    User user = User.register(command.username(), command.password(),
                              command.nickname(), null, command.phone());
    userRepository.save(user);

    // 5. 登录并返回 Token
    var tokenInfo = authenticationService.login(user.getId());

    return new RegisterResult(tokenInfo.token());
}
```

#### 新增查重接口

```java
// AccountController
@GetMapping("/check-username")
public ApiResponse<Boolean> checkUsername(@RequestParam @NotBlank String username) {
    boolean available = !userRepository.existsByUsername(username);
    return ApiResponse.ok(available);
}

@GetMapping("/check-phone")
public ApiResponse<Boolean> checkPhone(@RequestParam @NotBlank String phone) {
    boolean available = !userRepository.existsByPhoneNumber(phone);
    return ApiResponse.ok(available);
}
```

### 3.4 依赖调整

```kotlin
// build.gradle.kts
dependencies {
    implementation("cn.hutool:hutool-captcha:5.8.29")
}
```

---

## 四、前端设计

### 4.1 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/captcha | 获取图形验证码 |
| POST | /api/account/verification-code/sms | 发送短信验证码 |
| GET | /api/account/check-username | 用户名查重 |
| GET | /api/account/check-phone | 手机号查重 |
| POST | /api/account/register | 注册 |

### 4.2 请求/响应格式

#### 获取图形验证码

**请求**
```
GET /api/captcha
```

**响应**
```json
{
  "code": 200,
  "data": {
    "image": "data:image/png;base64,iVBORw0KGgo...",
    "captchaId": "uuid-string"
  }
}
```

#### 发送短信验证码

**请求**
```json
POST /api/account/verification-code/sms
{
  "phone": "13800138000",
  "purpose": "REGISTER",
  "captchaId": "uuid-string",
  "captchaCode": "A1B2"
}
```

**响应**
```json
{
  "code": 200,
  "data": {
    "expireSeconds": 300,
    "cooldownSeconds": 60
  }
}
```

#### 用户名查重

**请求**
```
GET /api/account/check-username?username=zhangsan
```

**响应**
```json
{
  "code": 200,
  "data": true
}
```

#### 注册

**请求**
```json
POST /api/account/register
{
  "username": "zhangsan",
  "password": "Password123",
  "nickname": "张三",
  "phone": "13800138000",
  "verificationCode": "123456"
}
```

**响应**
```json
{
  "code": 200,
  "data": {
    "token": "xxx"
  }
}
```

### 4.3 注册页面组件结构

```
web/src/app/(auth)/register/
├── page.tsx                    // 注册页面主组件
├── components/
│   ├── RegistrationForm.tsx    // 注册表单
│   ├── CaptchaModal.tsx        // 图形验证码弹窗
│   └── PasswordStrength.tsx    // 密码强度指示器
└── hooks/
    └── useRegistration.ts      // 注册逻辑 Hook
```

### 4.4 表单校验规则

| 字段 | 校验规则 |
|------|----------|
| username | 3-20 位，字母开头，字母/数字/下划线 |
| phone | 11 位手机号，1 开头 |
| password | 8-20 位，包含字母和数字 |
| verificationCode | 6 位数字 |

---

## 五、错误码定义

| 错误码 | HTTP Status | 说明 |
|--------|-------------|------|
| USERNAME_INVALID | 400 | 用户名格式错误 |
| USERNAME_ALREADY_EXISTS | 409 | 用户名已存在 |
| PHONE_INVALID | 400 | 手机号格式错误 |
| PHONE_NUMBER_ALREADY_EXISTS | 409 | 手机号已注册 |
| PASSWORD_WEAK | 400 | 密码强度不足 |
| CAPTCHA_INVALID | 400 | 图形验证码错误 |
| CAPTCHA_EXPIRED | 400 | 图形验证码已过期 |
| RATE_LIMIT_PHONE | 429 | 手机号发送过于频繁 |
| RATE_LIMIT_IP | 429 | IP 发送过于频繁 |
| VERIFICATION_CODE_INVALID | 400 | 短信验证码错误 |
| VERIFICATION_CODE_EXPIRED | 400 | 短信验证码已过期 |
| VERIFICATION_CODE_ALREADY_USED | 400 | 短信验证码已使用 |

---

## 六、测试策略

### 6.1 单元测试

| 类 | 测试内容 |
|----|----------|
| CaptchaGenerationServiceTest | 生成验证码、base64 格式 |
| CaptchaRepositoryTest | 保存、校验、过期 |
| CaptchaAppServiceTest | 创建、校验流程 |
| AccountRegistrationAppServiceTest | 注册成功、用户名重复、手机号重复、验证码错误 |

### 6.2 集成测试

| 测试 | 内容 |
|------|------|
| 注册流程完整测试 | 图形验证码 → 短信验证码 → 注册 |
| 限流测试 | 手机号冷却、IP 限流 |
| 查重接口测试 | 用户名/手机号可用/不可用 |

### 6.3 测试命名规范

遵循 `given_{条件}_when_{操作}_then_{预期结果}` 格式：

```java
@Test
void given_validCredentials_when_register_then_returnToken() {}

@Test
void given_duplicateUsername_when_register_then_throwException() {}

@Test
void given_invalidCaptcha_when_sendSms_then_throwException() {}
```

---

## 七、实施顺序

1. **后端 - 图形验证码**（0.5 天）
   - CaptchaGenerationService
   - CaptchaRepository + Redis 实现
   - CaptchaAppService + Controller
   - 单元测试

2. **后端 - 注册调整**（0.5 天）
   - 修改 RegisterCommand
   - 修改 AccountRegistrationAppService
   - 新增查重接口
   - 修改短信验证码（添加图形验证码校验、固定值）
   - 集成测试

3. **前端 - 注册页面**（1 天）
   - 修改注册表单
   - 实现图形验证码弹窗
   - 实现查重功能
   - 实现短信验证码倒计时
   - 联调测试

**总计预估：2 天**

---

## 八、后续扩展

- [ ] 短信接口对接：删除固定验证码代码
- [ ] 个人中心邮箱绑定功能
- [ ] 密码强度实时提示
- [ ] 注册日志和审计
