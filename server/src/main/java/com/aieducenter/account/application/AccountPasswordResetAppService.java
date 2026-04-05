package com.aieducenter.account.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.application.dto.command.ResetPasswordCommand;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.port.SessionManagementPort;
import com.aieducenter.account.domain.port.VerificationCodePort;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.account.domain.service.AccountPasswordEncoderService;
import com.aieducenter.verification.domain.error.VerificationCodeError;
import com.cartisan.core.exception.DomainException;

/**
 * 账号密码重置应用服务。
 *
 * <p>支持邮箱验证码重置和手机短信验证码重置两种方式，重置后踢出所有已登录会话。</p>
 *
 * @since 0.1.0
 */
@Service
@Transactional
public class AccountPasswordResetAppService {

    private static final Logger log = LoggerFactory.getLogger(AccountPasswordResetAppService.class);

    private final UserRepository userRepository;
    private final VerificationCodePort verificationCodePort;
    private final SessionManagementPort sessionManagementPort;
    private final AccountPasswordEncoderService accountPasswordEncoderService;

    public AccountPasswordResetAppService(
            UserRepository userRepository,
            VerificationCodePort verificationCodePort,
            SessionManagementPort sessionManagementPort,
            AccountPasswordEncoderService accountPasswordEncoderService) {
        this.userRepository = userRepository;
        this.verificationCodePort = verificationCodePort;
        this.sessionManagementPort = sessionManagementPort;
        this.accountPasswordEncoderService = accountPasswordEncoderService;
    }

    /**
     * 重置密码。
     *
     * <p>根据 account 是否包含 "@" 判断邮箱或手机号，校验验证码后查找用户，
     * 更新密码并踢出所有会话（踢出失败仅记录警告，不影响主流程）。</p>
     *
     * @param command 重置密码命令
     * @throws DomainException VerificationCodeError.CODE_INVALID（验证码无效或账号不存在）
     */
    public void resetPassword(ResetPasswordCommand command) {
        String account = command.account();
        String verificationCode = command.verificationCode();

        User user;
        if (account.contains("@")) {
            verificationCodePort.verifyCode(account, verificationCode, "RESET_PASSWORD");
            user = userRepository.findByEmail(account)
                .orElseThrow(() -> new DomainException(VerificationCodeError.CODE_INVALID));
        } else {
            verificationCodePort.verifyPhoneCode(account, verificationCode, "RESET_PASSWORD");
            user = userRepository.findByPhoneNumber(account)
                .orElseThrow(() -> new DomainException(VerificationCodeError.CODE_INVALID));
        }

        // 加密新密码
        String encodedPassword = accountPasswordEncoderService.encodePassword(command.newPassword());
        user.resetPassword(encodedPassword);
        userRepository.save(user);

        try {
            sessionManagementPort.kickout(user.getId());
        } catch (Exception e) {
            log.warn("kickout failed userId={}", user.getId(), e);
        }
    }
}
