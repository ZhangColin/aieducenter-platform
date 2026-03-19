package com.aieducenter.account.application;

import org.springframework.stereotype.Service;

import com.aieducenter.account.application.dto.LoginByPasswordCommand;
import com.aieducenter.account.application.dto.LoginBySmsCommand;
import com.aieducenter.account.application.dto.LoginResult;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifySmsCodeCommand;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;

/**
 * 账号登录应用服务。
 *
 * <p>支持密码登录和短信验证码登录两种方式，无 DB 写入，不需要事务。</p>
 *
 * @since 0.1.0
 */
@Service
public class AccountLoginAppService {

    private final UserRepository userRepository;
    private final VerificationCodeAppService verificationCodeAppService;
    private final AuthenticationService authenticationService;

    public AccountLoginAppService(
            UserRepository userRepository,
            VerificationCodeAppService verificationCodeAppService,
            AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.verificationCodeAppService = verificationCodeAppService;
        this.authenticationService = authenticationService;
    }

    /**
     * 密码登录。
     *
     * <p>account 可以是用户名、邮箱或手机号，依次尝试查找；全部未命中则抛出 ACCOUNT_NOT_FOUND (401)。</p>
     *
     * @param command 登录命令
     * @return 登录结果（含 token）
     * @throws DomainException ACCOUNT_NOT_FOUND (401) / LOGIN_PASSWORD_INCORRECT (401)
     */
    public LoginResult loginByPassword(LoginByPasswordCommand command) {
        String account = command.account();

        User user = userRepository.findByUsername(account)
            .or(() -> userRepository.findByEmail(account))
            .or(() -> userRepository.findByPhoneNumber(account))
            .orElseThrow(() -> new DomainException(UserError.ACCOUNT_NOT_FOUND));

        if (!user.matchesPassword(command.password())) {
            throw new DomainException(UserError.LOGIN_PASSWORD_INCORRECT);
        }

        var tokenInfo = authenticationService.login(user.getId());
        return new LoginResult(tokenInfo.token());
    }

    /**
     * 短信验证码登录。
     *
     * <p>先校验验证码（失败则直接向上抛出），再查找账号。</p>
     *
     * @param command 登录命令
     * @return 登录结果（含 token）
     * @throws DomainException ACCOUNT_NOT_FOUND (401)，或 VerificationCodeError（验证码无效/过期/已用）
     */
    public LoginResult loginBySms(LoginBySmsCommand command) {
        verificationCodeAppService.verifyPhoneCode(
            new VerifySmsCodeCommand(command.phone(), command.code(), "LOGIN"));

        User user = userRepository.findByPhoneNumber(command.phone())
            .orElseThrow(() -> new DomainException(UserError.ACCOUNT_NOT_FOUND));

        var tokenInfo = authenticationService.login(user.getId());
        return new LoginResult(tokenInfo.token());
    }
}
