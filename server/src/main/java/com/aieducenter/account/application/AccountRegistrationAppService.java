package com.aieducenter.account.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.application.dto.RegisterCommand;
import com.aieducenter.account.application.dto.RegisterResult;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.event.UserRegisteredEvent;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifySmsCodeCommand;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;

@Service
public class AccountRegistrationAppService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final ApplicationEventPublisher applicationEventPublisher;
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
            new VerifySmsCodeCommand(command.phone(), command.verificationCode(), "REGISTER"));

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
}
