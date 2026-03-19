package com.aieducenter.account.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.application.dto.RegisterByEmailCommand;
import com.aieducenter.account.application.dto.RegisterResult;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.event.UserRegisteredEvent;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifyCodeCommand;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;

@Service
public class AccountRegistrationAppService {

    private final UserRepository userRepository;
    private final VerificationCodeAppService verificationCodeAppService;
    private final AuthenticationService authenticationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountRegistrationAppService(
            UserRepository userRepository,
            VerificationCodeAppService verificationCodeAppService,
            AuthenticationService authenticationService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.userRepository = userRepository;
        this.verificationCodeAppService = verificationCodeAppService;
        this.authenticationService = authenticationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public RegisterResult registerByEmail(RegisterByEmailCommand command) {
        verificationCodeAppService.verifyCode(new VerifyCodeCommand(command.email(), command.verificationCode(), "REGISTER"));

        if (userRepository.existsByUsername(command.username())) {
            throw new DomainException(UserError.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(command.email())) {
            throw new DomainException(UserError.EMAIL_ALREADY_EXISTS);
        }

        User user = User.registerByEmail(command.username(), command.email(), command.password(), command.nickname());
        userRepository.save(user);

        var tokenInfo = authenticationService.login(user.getId());

        applicationEventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(), user.getUsername(), user.getEmail().orElse(null), user.getNickname(), Instant.now()));

        return new RegisterResult(tokenInfo.token());
    }
}
