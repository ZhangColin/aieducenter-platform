# 注册功能调整实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 简化注册流程，移除邮箱字段，添加图形验证码防刷，实现用户名/手机号实时查重。

**Architecture:** 在 Verification Context 新增图形验证码能力，Account Context 调整注册接口和查重接口，短信验证码固定为 123456（开发期）。

**Tech Stack:** Java 21, Spring Boot 3.4, hutool-captcha 5.8.29, Redis, Next.js 15

---

## 文件结构总览

### 后端新增/修改

```
server/src/main/java/com/aieducenter/
├── verification/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── VerificationType.java           [修改] 添加 CAPTCHA
│   │   │   └── CaptchaCode.java                [新增] 图形验证码值对象
│   │   ├── service/
│   │   │   └── CaptchaGenerationService.java   [新增] 图形验证码生成
│   │   ├── repository/
│   │   │   └── CaptchaRepository.java          [新增] 图形验证码存储接口
│   │   └── error/
│   │       └── CaptchaError.java               [新增] 错误码定义
│   ├── application/
│   │   ├── dto/
│   │   │   ├── CreateCaptchaResponse.java      [新增] 创建验证码响应
│   │   │   ├── VerifyCaptchaCommand.java       [新增] 校验验证码命令
│   │   │   └── SendSmsCodeCommand.java         [修改] 添加图形验证码字段
│   │   └── CaptchaAppService.java              [新增] 图形验证码应用服务
│   ├── infrastructure/
│   │   └── redis/
│   │       └── RedisCaptchaRepository.java     [新增] Redis 实现
│   ├── web/
│   │   └── CaptchaController.java              [新增] 图形验证码控制器
│   └── application/
│       └── VerificationCodeAppService.java     [修改] 短信发送逻辑
│
├── account/
│   ├── application/
│   │   ├── dto/
│   │   │   └── RegisterCommand.java            [修改] 移除 email，添加 verificationCode
│   │   └── AccountRegistrationAppService.java  [修改] 注册逻辑
│   └── web/
│       └── AccountController.java              [修改] 添加查重接口
│
└── build.gradle.kts                             [修改] 添加 hutool-captcha 依赖
```

### 前端新增/修改

```
web/src/
├── app/(auth)/register/
│   └── page.tsx                                 [修改] 注册页面
├── components/
│   └── CaptchaModal.tsx                         [新增] 图形验证码弹窗
└── lib/
    └── api.ts                                   [修改] API 接口定义
```

---

## Task 1: 添加 hutool-captcha 依赖

**Files:**
- Modify: `server/build.gradle.kts`

- [ ] **Step 1: 修改 build.gradle.kts，添加 hutool-captcha 依赖**

```kotlin
dependencies {
    implementation("cn.hutool:hutool-captcha:5.8.29")
}
```

- [ ] **Step 2: 运行 gradle sync 验证依赖**

Run: `cd server && ./gradlew dependencies --configuration runtimeClasspath | grep captcha`
Expected: 看到 `cn.hutool:hutool-captcha:5.8.29`

- [ ] **Step 3: 提交**

```bash
git add server/build.gradle.kts
git commit -m "deps: add hutool-captcha dependency"
```

---

## Task 2: 定义图形验证码错误码

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/domain/error/CaptchaError.java`

- [ ] **Step 1: 创建 CaptchaError 错误码枚举**

```java
package com.aieducenter.verification.domain.error;

import com.cartisan.core.exception.CodeMessage;

/**
 * 图形验证码相关错误码。
 */
public enum CaptchaError implements CodeMessage {
    /**
     * 图形验证码错误。
     */
    CAPTCHA_INVALID("CAPTCHA_INVALID", "图形验证码错误", 400),

    /**
     * 图形验证码已过期。
     */
    CAPTCHA_EXPIRED("CAPTCHA_EXPIRED", "图形验证码已过期", 400);

    private final String code;
    private final String message;
    private final int httpStatus;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }

    CaptchaError(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/domain/error/CaptchaError.java
git commit -m "feat(verification): add CaptchaError enum"
```

---

## Task 3: 创建 CaptchaCode 值对象

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/domain/model/CaptchaCode.java`

- [ ] **Step 1: 创建 CaptchaCode 值对象**

```java
package com.aieducenter.verification.domain.model;

import java.util.Objects;

/**
 * 图形验证码值对象。
 */
public class CaptchaCode {

    private final String id;
    private final String code;

    public CaptchaCode(String id, String code) {
        this.id = Objects.requireNonNull(id, "captchaId cannot be null");
        this.code = Objects.requireNonNull(code, "code cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/domain/model/CaptchaCode.java
git commit -m "feat(verification): add CaptchaCode value object"
```

---

## Task 4: 创建 CaptchaGenerationService

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/domain/service/CaptchaGenerationService.java`

- [ ] **Step 1: 创建 CaptchaGenerationService**

```java
package com.aieducenter.verification.domain.service;

import cn.hutool.captcha.LineCaptcha;
import com.cartisan.core.stereotype.DomainService;

/**
 * 图形验证码生成领域服务。
 *
 * <h3>规则</h3>
 * <ul>
 *   <li>使用 hutool-captcha LineCaptcha</li>
 *   <li>130x40 尺寸，4 位字符</li>
 *   <li>返回 base64 编码的图片</li>
 * </ul>
 */
@DomainService
public class CaptchaGenerationService {

    private static final int WIDTH = 130;
    private static final int HEIGHT = 40;
    private static final int CODE_COUNT = 4;
    private static final int LINE_COUNT = 20;

    /**
     * 生成图形验证码。
     *
     * @return 验证码结果（图片 base64 和验证码文本）
     */
    public CaptchaResult generate() {
        LineCaptcha captcha = new LineCaptcha(WIDTH, HEIGHT, CODE_COUNT, LINE_COUNT);

        String image = captcha.getImageBase64();
        String code = captcha.getCode();

        return new CaptchaResult(image, code);
    }

    /**
     * 图形验证码生成结果。
     *
     * @param image base64 编码的图片（带 data:image/png;base64, 前缀）
     * @param code 验证码文本
     */
    public record CaptchaResult(String image, String code) {}
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/domain/service/CaptchaGenerationService.java
git commit -m "feat(verification): add CaptchaGenerationService"
```

---

## Task 5: 创建 CaptchaRepository 接口

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/domain/repository/CaptchaRepository.java`

- [ ] **Step 1: 创建 CaptchaRepository 接口**

```java
package com.aieducenter.verification.domain.repository;

import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 图形验证码存储端口。
 */
@Port(PortType.REPOSITORY)
public interface CaptchaRepository {

    /**
     * 保存图形验证码。
     *
     * @param captchaId 验证码 ID
     * @param code 验证码文本
     * @param expireSeconds 过期时间（秒）
     */
    void save(String captchaId, String code, long expireSeconds);

    /**
     * 校验并删除图形验证码。
     *
     * @param captchaId 验证码 ID
     * @param code 用户输入的验证码
     * @return 校验是否通过
     */
    boolean verifyAndDelete(String captchaId, String code);
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/domain/repository/CaptchaRepository.java
git commit -m "feat(verification): add CaptchaRepository interface"
```

---

## Task 6: 实现 RedisCaptchaRepository

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/infrastructure/redis/RedisCaptchaRepository.java`

- [ ] **Step 1: 创建 RedisCaptchaRepository 实现**

```java
package com.aieducenter.verification.infrastructure.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.aieducenter.verification.domain.repository.CaptchaRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;

/**
 * 图形验证码 Redis 存储适配器。
 */
@Adapter(PortType.REPOSITORY)
@Repository
public class RedisCaptchaRepository implements CaptchaRepository {

    private static final String KEY_PREFIX = "captcha:";
    private static final long EXPIRE_SECONDS = 180; // 3 分钟

    private final RedisTemplate<String, String> redisTemplate;

    public RedisCaptchaRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String captchaId, String code, long expireSeconds) {
        String key = KEY_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, code, expireSeconds, TimeUnit.SECONDS);
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

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/infrastructure/redis/RedisCaptchaRepository.java
git commit -m "feat(verification): add RedisCaptchaRepository implementation"
```

---

## Task 7: 创建 CreateCaptchaResponse DTO

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/application/dto/CreateCaptchaResponse.java`

- [ ] **Step 1: 创建 CreateCaptchaResponse**

```java
package com.aieducenter.verification.application.dto;

/**
 * 创建图形验证码响应。
 */
public record CreateCaptchaResponse(
    /**
     * base64 编码的图片（带 data:image/png;base64, 前缀）。
     */
    String image,

    /**
     * 验证码 ID，用于后续校验。
     */
    String captchaId
) {}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/application/dto/CreateCaptchaResponse.java
git commit -m "feat(verification): add CreateCaptchaResponse DTO"
```

---

## Task 8: 创建 VerifyCaptchaCommand DTO

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/application/dto/VerifyCaptchaCommand.java`

- [ ] **Step 1: 创建 VerifyCaptchaCommand**

```java
package com.aieducenter.verification.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 校验图形验证码命令。
 */
public record VerifyCaptchaCommand(
    /**
     * 验证码 ID。
     */
    @NotBlank
    String captchaId,

    /**
     * 用户输入的验证码。
     */
    @NotBlank
    String code
) {}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/application/dto/VerifyCaptchaCommand.java
git commit -m "feat(verification): add VerifyCaptchaCommand DTO"
```

---

## Task 9: 创建 CaptchaAppService

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/application/CaptchaAppService.java`

- [ ] **Step 1: 创建 CaptchaAppService**

```java
package com.aieducenter.verification.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.aieducenter.verification.application.dto.CreateCaptchaResponse;
import com.aieducenter.verification.domain.error.CaptchaError;
import com.aieducenter.verification.domain.repository.CaptchaRepository;
import com.aieducenter.verification.domain.service.CaptchaGenerationService;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;

/**
 * 图形验证码应用服务。
 */
@Service
public class CaptchaAppService {

    private final CaptchaGenerationService generator;
    private final CaptchaRepository repository;

    public CaptchaAppService(
            CaptchaGenerationService generator,
            CaptchaRepository repository) {
        this.generator = generator;
        this.repository = repository;
    }

    /**
     * 创建图形验证码。
     *
     * @return 验证码响应（图片和 ID）
     */
    public CreateCaptchaResponse createCaptcha() {
        var result = generator.generate();

        String captchaId = UUID.randomUUID().toString();

        // 保存到 Redis，3 分钟过期
        repository.save(captchaId, result.code(), 180);

        return new CreateCaptchaResponse(result.image(), captchaId);
    }

    /**
     * 校验图形验证码。
     *
     * @param captchaId 验证码 ID
     * @param code 用户输入的验证码
     * @throws DomainException 验证码错误或已过期
     */
    public void verifyCaptcha(String captchaId, String code) {
        boolean verified = repository.verifyAndDelete(captchaId, code);
        Assertions.require(verified, CaptchaError.CAPTCHA_INVALID);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/application/CaptchaAppService.java
git commit -m "feat(verification): add CaptchaAppService"
```

---

## Task 10: 创建 CaptchaController

**Files:**
- Create: `server/src/main/java/com/aieducenter/verification/web/CaptchaController.java`

- [ ] **Step 1: 创建 CaptchaController**

```java
package com.aieducenter.verification.web;

import com.aieducenter.verification.application.CaptchaAppService;
import com.aieducenter.verification.application.dto.CreateCaptchaResponse;
import com.cartisan.web.response.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码控制器。
 */
@RestController
@RequestMapping("/api")
public class CaptchaController {

    private final CaptchaAppService captchaAppService;

    public CaptchaController(CaptchaAppService captchaAppService) {
        this.captchaAppService = captchaAppService;
    }

    /**
     * 获取图形验证码。
     *
     * @return 验证码响应（图片 base64 和 ID）
     */
    @GetMapping("/captcha")
    public ApiResponse<CreateCaptchaResponse> getCaptcha() {
        return ApiResponse.ok(captchaAppService.createCaptcha());
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/web/CaptchaController.java
git commit -m "feat(verification): add CaptchaController"
```

---

## Task 11: 修改 VerificationType 添加 CAPTCHA

**Files:**
- Modify: `server/src/main/java/com/aieducenter/verification/domain/model/VerificationType.java`

- [ ] **Step 1: 添加 CAPTCHA 类型**

```java
public enum VerificationType {
    EMAIL,
    SMS,
    /**
     * 图形验证码。
     */
    CAPTCHA
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/domain/model/VerificationType.java
git commit -m "feat(verification): add CAPTCHA to VerificationType"
```

---

## Task 12: 修改 SendSmsCodeCommand 添加图形验证码字段

**Files:**
- Modify: `server/src/main/java/com/aieducenter/verification/application/dto/SendSmsCodeCommand.java`

- [ ] **Step 1: 读取当前文件内容**

Run: `cat server/src/main/java/com/aieducenter/verification/application/dto/SendSmsCodeCommand.java`

- [ ] **Step 2: 添加图形验证码字段**

```java
package com.aieducenter.verification.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送短信验证码命令。
 */
public record SendSmsCodeCommand(
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    String phone,

    @NotBlank
    String purpose,

    /**
     * 图形验证码 ID。
     */
    @NotBlank
    String captchaId,

    /**
     * 用户输入的图形验证码。
     */
    @NotBlank
    String captchaCode
) {}
```

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/application/dto/SendSmsCodeCommand.java
git commit -m "feat(verification): add captcha fields to SendSmsCodeCommand"
```

---

## Task 13: 修改 VerificationCodeAppService 添加图形验证码校验和固定验证码

**Files:**
- Modify: `server/src/main/java/com/aieducenter/verification/application/VerificationCodeAppService.java`

- [ ] **Step 1: 读取当前文件内容**

Run: `cat server/src/main/java/com/aieducenter/verification/application/VerificationCodeAppService.java`

- [ ] **Step 2: 添加 CaptchaAppService 依赖**

```java
private final CaptchaAppService captchaAppService;

public VerificationCodeAppService(
        VerificationCodeRepository repository,
        VerificationCodeGenerationService generator,
        MessageSender messageSender,
        VerificationCodeProperties properties,
        CaptchaAppService captchaAppService) {  // 新增参数
    this.repository = repository;
    this.generator = generator;
    this.messageSender = messageSender;
    this.properties = properties;
    this.captchaAppService = captchaAppService;  // 新增赋值
}
```

- [ ] **Step 3: 在 sendSmsVerificationCode 方法中添加图形验证码校验**

在 `validatePhoneFormat(command.phone());` 之后添加：

```java
// 校验图形验证码
captchaAppService.verifyCaptcha(command.captchaId(), command.captchaCode());
```

- [ ] **Step 4: 在生成验证码后添加固定值替换**

在 `String code = generator.generate();` 之后添加：

```java
code = "123456";  // TODO: 短信接口对接后删除此行
```

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/application/VerificationCodeAppService.java
git commit -m "feat(verification): add captcha verification and fixed SMS code"
```

---

## Task 14: 修改 RegisterCommand

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/application/dto/RegisterCommand.java`

- [ ] **Step 1: 读取当前文件内容**

Run: `cat server/src/main/java/com/aieducenter/account/application/dto/RegisterCommand.java`

- [ ] **Step 2: 移除 email，添加 verificationCode**

```java
package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCommand(
    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$")
    String username,

    @NotBlank
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$")
    String password,

    @Size(max = 50)
    String nickname,

    @Pattern(regexp = "^1[3-9]\\d{9}$")
    String phone,

    /**
     * 短信验证码。
     */
    @NotBlank
    String verificationCode
) {}
```

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/account/application/dto/RegisterCommand.java
git commit -m "feat(account): update RegisterCommand - remove email, add verificationCode"
```

---

## Task 15: 修改 AccountRegistrationAppService

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/application/AccountRegistrationAppService.java`

- [ ] **Step 1: 读取当前文件内容**

Run: `cat server/src/main/java/com/aieducenter/account/application/AccountRegistrationAppService.java`

- [ ] **Step 2: 添加 VerificationCodeAppService 依赖**

```java
private final VerificationCodeAppService verificationCodeAppService;

public AccountRegistrationAppService(
        UserRepository userRepository,
        AuthenticationService authenticationService,
        ApplicationEventPublisher applicationEventPublisher,
        VerificationCodeAppService verificationCodeAppService) {
    this.userRepository = userRepository;
    this.authenticationService = authenticationService;
    this.applicationEventPublisher = applicationEventPublisher;
    this.verificationCodeAppService = verificationCodeAppService;
}
```

- [ ] **Step 3: 添加导入**

```java
import com.aieducenter.verification.application.dto.VerifySmsCodeCommand;
import com.aieducenter.verification.application.VerificationCodeAppService;
```

- [ ] **Step 4: 修改 register 方法**

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

    // 6. 发布用户注册事件
    applicationEventPublisher.publishEvent(new UserRegisteredEvent(
        user.getId(), user.getUsername(), null, user.getNickname(), Instant.now()));

    return new RegisterResult(tokenInfo.token());
}
```

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/account/application/AccountRegistrationAppService.java
git commit -m "feat(account): update registration logic - phone verification required"
```

---

## Task 16: 修改 AccountController 添加查重接口

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/web/AccountController.java`

- [ ] **Step 1: 读取当前文件内容**

Run: `cat server/src/main/java/com/aieducenter/account/web/AccountController.java`

- [ ] **Step 2: 添加查重接口**

```java
@GetMapping("/check-username")
public ApiResponse<Boolean> checkUsername(
        @RequestParam
        @NotBlank
        @Size(min = 3, max = 20)
        String username) {
    boolean available = !userRepository.existsByUsername(username);
    return ApiResponse.ok(available);
}

@GetMapping("/check-phone")
public ApiResponse<Boolean> checkPhone(
        @RequestParam
        @NotBlank
        @Pattern(regexp = "^1[3-9]\\d{9}$")
        String phone) {
    boolean available = !userRepository.existsByPhoneNumber(phone);
    return ApiResponse.ok(available);
}
```

- [ ] **Step 3: 添加导入**

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
```

- [ ] **Step 4: 添加 UserRepository 依赖注入（如果没有）**

```java
private final UserRepository userRepository;

public AccountController(
        AccountRegistrationAppService registrationAppService,
        AccountLoginAppService loginAppService,
        AccountPasswordResetAppService passwordResetAppService,
        UserRepository userRepository) {
    this.registrationAppService = registrationAppService;
    this.loginAppService = loginAppService;
    this.passwordResetAppService = passwordResetAppService;
    this.userRepository = userRepository;
}
```

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/account/web/AccountController.java
git commit -m "feat(account): add username and phone availability check endpoints"
```

---

## Task 17: 编写 CaptchaAppServiceTest

**Files:**
- Create: `server/src/test/java/com/aieducenter/verification/application/CaptchaAppServiceTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.aieducenter.verification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.verification.domain.error.CaptchaError;
import com.aieducenter.verification.domain.repository.CaptchaRepository;
import com.aieducenter.verification.domain.service.CaptchaGenerationService;
import com.aieducenter.verification.domain.service.CaptchaGenerationService.CaptchaResult;
import com.cartisan.core.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class CaptchaAppServiceTest {

    @Mock
    private CaptchaGenerationService generator;

    @Mock
    private CaptchaRepository repository;

    private CaptchaAppService appService;

    @BeforeEach
    void setUp() {
        appService = new CaptchaAppService(generator, repository);
    }

    @Test
    void given_validGenerator_when_createCaptcha_then_returnResponseWithImageAndId() {
        // Given
        String expectedImage = "data:image/png;base64,iVBORw0KGgo...";
        String expectedCode = "A1B2";
        when(generator.generate()).thenReturn(new CaptchaResult(expectedImage, expectedCode));

        // When
        var response = appService.createCaptcha();

        // Then
        assertThat(response.image()).isEqualTo(expectedImage);
        assertThat(response.captchaId()).isNotNull();
        assertThat(response.captchaId()).hasSize(36); // UUID format

        verify(repository).save(anyString(), eq(expectedCode), eq(180L));
    }

    @Test
    void given_correctCaptcha_when_verifyCaptcha_then_success() {
        // Given
        String captchaId = "test-captcha-id";
        String code = "A1B2";
        when(repository.verifyAndDelete(captchaId, code)).thenReturn(true);

        // When/Then
        appService.verifyCaptcha(captchaId, code);
    }

    @Test
    void given_incorrectCaptcha_when_verifyCaptcha_then_throwException() {
        // Given
        String captchaId = "test-captcha-id";
        String wrongCode = "WRONG";
        when(repository.verifyAndDelete(captchaId, wrongCode)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> appService.verifyCaptcha(captchaId, wrongCode))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(CaptchaError.CAPTCHA_INVALID.message());
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew test --tests "*.CaptchaAppServiceTest"`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/verification/application/CaptchaAppServiceTest.java
git commit -m "test(verification): add CaptchaAppServiceTest"
```

---

## Task 18: 编写 CaptchaControllerTest

**Files:**
- Create: `server/src/test/java/com/aieducenter/verification/web/CaptchaControllerTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.aieducenter.verification.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.verification.application.CaptchaAppService;
import com.aieducenter.verification.application.dto.CreateCaptchaResponse;

@WebMvcTest(CaptchaController.class)
class CaptchaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CaptchaAppService captchaAppService;

    @Test
    void given_validCaptcha_when_getCaptcha_then_returnResponse() throws Exception {
        // Given
        String expectedImage = "data:image/png;base64,iVBORw0KGgo...";
        String expectedId = "uuid-123";
        when(captchaAppService.createCaptcha())
            .thenReturn(new CreateCaptchaResponse(expectedImage, expectedId));

        // When/Then
        mvc.perform(get("/api/captcha"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.image").value(expectedImage))
            .andExpect(jsonPath("$.data.captchaId").value(expectedId));
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew test --tests "*.CaptchaControllerTest"`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/verification/web/CaptchaControllerTest.java
git commit -m "test(verification): add CaptchaControllerTest"
```

---

## Task 19: 运行后端全量测试

**Files:**
- None

- [ ] **Step 1: 运行所有测试**

Run: `cd server && ./gradlew test`
Expected: 全部 PASS

- [ ] **Step 2: 运行 ArchUnit 检查**

Run: `cd server && ./gradlew check`
Expected: 全部通过

- [ ] **Step 3: 启动应用验证**

Run: `cd server && ./gradlew bootRun`
Expected: 应用成功启动，可以访问 `/api/captcha`

- [ ] **Step 4: 提交（如有修复）**

```bash
git add .
git commit -m "test: fix issues found during full test run"
```

---

## Task 20: 修改前端注册页面

**Files:**
- Modify: `web/src/app/(auth)/register/page.tsx`

- [ ] **Step 1: 读取当前文件内容**

Run: `cat web/src/app/\(auth\)/register/page.tsx`

- [ ] **Step 2: 修改注册表单**

```tsx
'use client'

import { useState } from 'react'

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    username: '',
    phone: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    verificationCode: '',
  })
  const [usernameStatus, setUsernameStatus] = useState<'checking' | 'available' | 'taken'>()
  const [phoneStatus, setPhoneStatus] = useState<'checking' | 'available' | 'taken'>()
  const [showCaptchaModal, setShowCaptchaModal] = useState(false)
  const [captchaUrl, setCaptchaUrl] = useState('')
  const [captchaId, setCaptchaId] = useState('')
  const [captchaCode, setCaptchaCode] = useState('')
  const [countdown, setCountdown] = useState(0)

  // 用户名查重
  const checkUsername = async (username: string) => {
    if (!username || username.length < 3) return
    setUsernameStatus('checking')
    try {
      const res = await fetch(`/api/account/check-username?username=${encodeURIComponent(username)}`)
      const data = await res.json()
      setUsernameStatus(data.data ? 'available' : 'taken')
    } catch {
      setUsernameStatus('taken')
    }
  }

  // 手机号查重
  const checkPhone = async (phone: string) => {
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) return
    setPhoneStatus('checking')
    try {
      const res = await fetch(`/api/account/check-phone?phone=${phone}`)
      const data = await res.json()
      setPhoneStatus(data.data ? 'available' : 'taken')
    } catch {
      setPhoneStatus('taken')
    }
  }

  // 获取图形验证码
  const getCaptcha = async () => {
    try {
      const res = await fetch('/api/captcha')
      const data = await res.json()
      setCaptchaUrl(data.data.image)
      setCaptchaId(data.data.captchaId)
    } catch (error) {
      console.error('Failed to get captcha:', error)
    }
  }

  // 发送短信验证码
  const sendSmsCode = async () => {
    // 先展示图形验证码弹窗
    setShowCaptchaModal(true)
    await getCaptcha()
  }

  // 确认发送短信
  const confirmSendSms = async () => {
    try {
      const res = await fetch('/api/account/verification-code/sms', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          phone: formData.phone,
          purpose: 'REGISTER',
          captchaId,
          captchaCode,
        }),
      })
      if (res.ok) {
        setShowCaptchaModal(false)
        setCountdown(60)
        const timer = setInterval(() => {
          setCountdown((prev) => {
            if (prev <= 1) {
              clearInterval(timer)
              return 0
            }
            return prev - 1
          })
        }, 1000)
      }
    } catch (error) {
      console.error('Failed to send SMS:', error)
    }
  }

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: 实现注册逻辑
    console.log('Register:', formData)
  }

  return (
    <div className="min-h-screen flex flex-col bg-background-light dark:bg-background-dark font-display">
      {/* ... 保持原有的页面布局 ... */}

      {/* 图形验证码弹窗 */}
      {showCaptchaModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-slate-900 rounded-lg p-6 max-w-sm w-full mx-4">
            <h3 className="text-lg font-bold mb-4">请输入图形验证码</h3>
            {captchaUrl && (
              <img
                src={captchaUrl}
                alt="验证码"
                className="mb-4 cursor-pointer"
                onClick={getCaptcha}
              />
            )}
            <input
              type="text"
              placeholder="请输入验证码"
              className="w-full px-4 py-2 border rounded-lg mb-4 dark:bg-slate-800"
              value={captchaCode}
              onChange={(e) => setCaptchaCode(e.target.value)}
            />
            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => setShowCaptchaModal(false)}
                className="flex-1 py-2 border rounded-lg"
              >
                取消
              </button>
              <button
                type="button"
                onClick={confirmSendSms}
                className="flex-1 py-2 bg-primary text-white rounded-lg"
              >
                确认
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 注册表单 */}
      <form onSubmit={handleRegister} className="space-y-5">
        {/* 用户名 */}
        <div className="space-y-2">
          <label className="text-sm font-medium">用户名</label>
          <input
            type="text"
            placeholder="3-20位，字母开头，允许字母/数字/下划线"
            className="w-full px-4 py-3 rounded-lg border"
            value={formData.username}
            onChange={(e) => {
              setFormData({ ...formData, username: e.target.value })
              checkUsername(e.target.value)
            }}
          />
          {usernameStatus === 'available' && (
            <p className="text-xs text-green-500">用户名可用</p>
          )}
          {usernameStatus === 'taken' && (
            <p className="text-xs text-red-500">用户名已存在</p>
          )}
        </div>

        {/* 手机号 */}
        <div className="space-y-2">
          <label className="text-sm font-medium">手机号</label>
          <input
            type="tel"
            placeholder="请输入手机号"
            className="w-full px-4 py-3 rounded-lg border"
            value={formData.phone}
            onChange={(e) => {
              setFormData({ ...formData, phone: e.target.value })
              checkPhone(e.target.value)
            }}
          />
          {phoneStatus === 'available' && (
            <p className="text-xs text-green-500">手机号可用</p>
          )}
          {phoneStatus === 'taken' && (
            <p className="text-xs text-red-500">手机号已注册</p>
          )}
        </div>

        {/* 密码 */}
        <div className="space-y-2">
          <label className="text-sm font-medium">密码</label>
          <input
            type="password"
            placeholder="8-20位，包含字母和数字"
            className="w-full px-4 py-3 rounded-lg border"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          />
        </div>

        {/* 昵称（可选） */}
        <div className="space-y-2">
          <label className="text-sm font-medium">昵称（可选）</label>
          <input
            type="text"
            placeholder="不填则默认为用户名"
            className="w-full px-4 py-3 rounded-lg border"
            value={formData.nickname}
            onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
          />
        </div>

        {/* 短信验证码 */}
        <div className="space-y-2">
          <label className="text-sm font-medium">验证码</label>
          <div className="flex gap-3">
            <input
              type="text"
              placeholder="请输入验证码"
              className="flex-1 px-4 py-3 rounded-lg border"
              value={formData.verificationCode}
              onChange={(e) => setFormData({ ...formData, verificationCode: e.target.value })}
            />
            <button
              type="button"
              onClick={sendSmsCode}
              disabled={countdown > 0 || phoneStatus !== 'available'}
              className="px-4 py-3 rounded-lg border border-primary text-primary font-medium whitespace-nowrap min-w-[100px] disabled:opacity-50"
            >
              {countdown > 0 ? `${countdown}s` : '获取验证码'}
            </button>
          </div>
        </div>

        <button
          type="submit"
          className="w-full bg-primary text-white py-3.5 rounded-lg font-bold"
        >
          立即注册
        </button>
      </form>
    </div>
  )
}
```

- [ ] **Step 3: 提交**

```bash
git add web/src/app/\(auth\)/register/page.tsx
git commit -m "feat(web): update registration page with phone verification"
```

---

## Task 21: 前后端联调测试

**Files:**
- None

- [ ] **Step 1: 启动后端**

Run: `cd server && ./gradlew bootRun`

- [ ] **Step 2: 启动前端**

Run: `cd web && pnpm dev`

- [ ] **Step 3: 手动测试流程**

1. 访问 `http://localhost:3000/register`
2. 输入用户名，查看实时查重提示
3. 输入手机号，查看实时查重提示
4. 点击「获取验证码」，弹出图形验证码
5. 输入图形验证码，点击确认
6. 输入短信验证码 123456
7. 提交注册

- [ ] **Step 4: 验证后端日志**

确认：
- 图形验证码生成和校验正常
- 短信验证码固定为 123456
- 注册成功后用户和租户创建正常

- [ ] **Step 5: 提交（如有修复）**

```bash
git add .
git commit -m "fix: issues found during integration testing"
```

---

## 验收标准

完成后应满足：

1. ✅ 用户可通过用户名 + 手机号 + 密码注册
2. ✅ 用户名和手机号实时查重
3. ✅ 图形验证码防刷机制正常工作
4. ✅ 短信验证码固定为 123456（开发期）
5. ✅ 注册成功后自动创建单人租户
6. ✅ 所有测试通过
7. ✅ 前后端联调成功
