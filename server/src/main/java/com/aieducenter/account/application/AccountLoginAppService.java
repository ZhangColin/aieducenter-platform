package com.aieducenter.account.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.application.dto.command.LoginByPasswordCommand;
import com.aieducenter.account.application.dto.command.LoginBySmsCommand;
import com.aieducenter.account.application.dto.response.LoginResult;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.port.VerificationCodePort;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.account.domain.service.AccountPasswordEncoderService;
import com.aieducenter.verification.application.CaptchaAppService;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;

/**
 * 账号登录应用服务。
 *
 * <p>支持密码登录和短信验证码登录两种方式，登录时会产生会话状态。</p>
 *
 * @since 0.1.0
 */
@Service
public class AccountLoginAppService {

    private final UserRepository userRepository;
    private final VerificationCodePort verificationCodePort;
    private final AuthenticationService authenticationService;
    private final CaptchaAppService captchaAppService;
    private final AccountPasswordEncoderService accountPasswordEncoderService;

    public AccountLoginAppService(
            UserRepository userRepository,
            VerificationCodePort verificationCodePort,
            AuthenticationService authenticationService,
            CaptchaAppService captchaAppService,
            AccountPasswordEncoderService accountPasswordEncoderService) {
        this.userRepository = userRepository;
        this.verificationCodePort = verificationCodePort;
        this.authenticationService = authenticationService;
        this.captchaAppService = captchaAppService;
        this.accountPasswordEncoderService = accountPasswordEncoderService;
    }

    /**
     * 密码登录。
     *
     * <p>account 可以是用户名、邮箱或手机号，依次尝试查找；全部未命中则抛出 ACCOUNT_NOT_FOUND (401)。</p>
     *
     * @param command 登录命令
     * @return 登录结果（含 token）
     * @throws DomainException ACCOUNT_NOT_FOUND (401) / LOGIN_PASSWORD_INCORRECT (401) / CAPTCHA_INVALID (400)
     */
    @Transactional  // 会产生会话状态，非只读
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
        if (!accountPasswordEncoderService.verifyPassword(command.password(), user.getPassword())) {
            throw new DomainException(UserError.LOGIN_PASSWORD_INCORRECT);
        }

        // 4. 生成 Token
        var tokenInfo = authenticationService.login(user.getId());
        return new LoginResult(tokenInfo.token());
    }

    /**
     * 短信验证码登录。
     *
     * <p>图形验证码已在发送短信时校验，这里只校验短信验证码，然后查找账号。</p>
     *
     * @param command 登录命令
     * @return 登录结果（含 token）
     * @throws DomainException ACCOUNT_NOT_FOUND (401)，或 VerificationCodeError（验证码无效/过期/已用）
     */
    @Transactional  // 会产生会话状态，非只读
    public LoginResult loginBySms(LoginBySmsCommand command) {
        // 注意：图形验证码已在发送短信验证码时校验过，这里不再重复校验

        // 1. 校验短信验证码
        verificationCodePort.verifyPhoneCode(command.phone(), command.code(), "LOGIN");

        // 2. 查找用户
        User user = userRepository.findByPhoneNumber(command.phone())
            .orElseThrow(() -> new DomainException(UserError.ACCOUNT_NOT_FOUND));

        // 3. 生成 Token
        var tokenInfo = authenticationService.login(user.getId());
        return new LoginResult(tokenInfo.token());
    }

    /**
     * 退出登录。
     *
     * <p>调用 Sa-Token 的退出接口，清除服务端会话。</p>
     */
    @Transactional  // 会清除会话状态
    public void logout() {
        authenticationService.logout();
    }
}
