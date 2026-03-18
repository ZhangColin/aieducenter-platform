package com.aieducenter.verification.application;

import com.aieducenter.verification.application.dto.SendCodeResponse;
import com.aieducenter.verification.application.dto.SendEmailCodeCommand;
import com.aieducenter.verification.application.dto.VerifyCodeCommand;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.aieducenter.verification.domain.error.VerificationCodeError;
import com.aieducenter.verification.domain.model.VerificationCode;
import com.aieducenter.verification.domain.model.VerificationPurpose;
import com.aieducenter.verification.domain.model.VerificationType;
import com.aieducenter.verification.domain.repository.VerificationCodeRepository;
import com.aieducenter.verification.domain.service.VerificationCodeGenerator;
import com.aieducenter.verification.domain.port.MessageSender;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;

import org.springframework.stereotype.Service;

/**
 * 验证码应用服务。
 *
 * @since 0.1.0
 */
@Service
public class VerificationCodeApplicationService {

    private final VerificationCodeRepository repository;
    private final VerificationCodeGenerator generator;
    private final MessageSender messageSender;

    public VerificationCodeApplicationService(
            VerificationCodeRepository repository,
            VerificationCodeGenerator generator,
            MessageSender messageSender) {
        this.repository = repository;
        this.generator = generator;
        this.messageSender = messageSender;
    }

    /**
     * 发送邮箱验证码。
     *
     * @param command 命令
     * @param ip 客户端IP
     * @return 发送结果
     * @throws DomainException 邮箱格式错误、限流触发
     */
    public SendCodeResponse sendEmailVerificationCode(SendEmailCodeCommand command, String ip) {
        // 1. 校验邮箱格式
        validateEmailFormat(command.email());

        // 2. 校验 purpose
        VerificationPurpose purpose = validatePurpose(command.purpose());

        // 3. 检查邮箱限流
        Assertions.require(
            !repository.isEmailRateLimited(command.email(), command.purpose()),
            VerificationCodeError.RATE_LIMIT_EMAIL
        );

        // 4. 检查IP限流
        Assertions.require(
            !repository.isIpRateLimited(ip),
            VerificationCodeError.RATE_LIMIT_IP
        );

        // 5. 生成验证码
        String code = generator.generate();

        // 6. 创建验证码实体
        VerificationCode verificationCode = VerificationCode.create(
            VerificationType.EMAIL,
            command.email(),
            code,
            purpose
        );

        // 7. 保存到 Redis
        repository.save(verificationCode);

        // 8. 更新限流计数
        repository.incrementEmailCount(command.email(), command.purpose());
        repository.incrementIpCount(ip);

        // 9. 发送邮件
        messageSender.send(command.email(), code, purpose);

        return new SendCodeResponse(300, 60);
    }

    /**
     * 校验验证码。
     *
     * @param command 命令
     * @return 校验结果
     * @throws DomainException 验证码错误、已过期、已使用
     */
    public VerifyCodeResult verifyCode(VerifyCodeCommand command) {
        // 1. 校验邮箱格式
        validateEmailFormat(command.email());

        // 2. 校验 purpose
        VerificationPurpose purpose = validatePurpose(command.purpose());

        // 3. 从 Redis 获取验证码
        String id = command.email() + ":" + command.purpose();
        var verificationCode = repository.findById(id)
            .orElseThrow(() -> new DomainException(VerificationCodeError.CODE_INVALID));

        // 4. 校验验证码
        if (!verificationCode.isValid(command.code())) {
            if (verificationCode.isUsed()) {
                throw new DomainException(VerificationCodeError.CODE_ALREADY_USED);
            }
            // 检查是否过期
            if (java.time.Instant.now().isAfter(verificationCode.getExpireAt())) {
                throw new DomainException(VerificationCodeError.CODE_EXPIRED);
            }
            throw new DomainException(VerificationCodeError.CODE_INVALID);
        }

        // 5. 标记已使用
        verificationCode.markAsUsed();
        repository.save(verificationCode);

        return new VerifyCodeResult(true, "验证码正确");
    }

    /**
     * 校验邮箱格式。
     */
    private void validateEmailFormat(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new DomainException(VerificationCodeError.EMAIL_INVALID);
        }
    }

    /**
     * 校验并转换 purpose。
     */
    private VerificationPurpose validatePurpose(String purpose) {
        try {
            return VerificationPurpose.valueOf(purpose.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException(VerificationCodeError.PURPOSE_INVALID);
        }
    }
}
