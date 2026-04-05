package com.aieducenter.account.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.application.dto.command.RegisterCommand;
import com.aieducenter.account.application.dto.response.RegisterResult;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.event.UserRegisteredEvent;
import com.aieducenter.account.domain.port.VerificationCodePort;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.account.domain.service.AccountPasswordEncoderService;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;

@Service
public class AccountRegistrationAppService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final VerificationCodePort verificationCodePort;
    private final AccountPasswordEncoderService accountPasswordEncoderService;

    public AccountRegistrationAppService(
            UserRepository userRepository,
            AuthenticationService authenticationService,
            ApplicationEventPublisher applicationEventPublisher,
            VerificationCodePort verificationCodePort,
            AccountPasswordEncoderService accountPasswordEncoderService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.verificationCodePort = verificationCodePort;
        this.accountPasswordEncoderService = accountPasswordEncoderService;
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
        verificationCodePort.verifyPhoneCode(command.phone(), command.verificationCode(), "REGISTER");

        // 4. 加密密码
        String encodedPassword = accountPasswordEncoderService.encodePassword(command.password());

        // 5. 创建用户
        User user = User.register(command.username(), encodedPassword,
                                  command.nickname(), null, command.phone());
        userRepository.save(user);

        // 6. 登录并返回 Token
        var tokenInfo = authenticationService.login(user.getId());

        // 7. 发布用户注册事件
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(), user.getUsername(), null, user.getNickname(), Instant.now()));

        return new RegisterResult(tokenInfo.token());
    }
}
